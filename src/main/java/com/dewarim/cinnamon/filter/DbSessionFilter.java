package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.*;
import com.dewarim.cinnamon.dao.DeletionDao;
import com.dewarim.cinnamon.model.Deletion;
import com.dewarim.cinnamon.provider.ContentProviderService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER;
import static com.dewarim.cinnamon.api.Constants.CONTENT_PROVIDER_SERVICE;

/**
 *
 */
public class DbSessionFilter implements Filter {

    private static final Logger log = LogManager.getLogger(DbSessionFilter.class);

    private final DeletionDao  deletionDao = new DeletionDao();
    private ContentProviderService contentProviderService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try(SqlSession sqlSession = ThreadLocalSqlSession.refreshSession()) {
            log.debug("DbSessionFilter: before");
            chain.doFilter(request, response);
            log.debug("DbSessionFilter: after");

            if (ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK) {
                log.debug("commit changes (if any)");
                try {
                    // TODO: RequestResponseFilter also commits... due to http5client being so fast.
                    sqlSession.commit();
                }
                catch (Exception e){
                    log.warn("Failed to commit DB session: ",e);
                    throw ErrorCode.COMMIT_TO_DATABASE_FAILED.exception();
                }
                // TODO: maybe check if an idling background thread uses less resources
                List<Deletion> deletions = deletionDao.listPendingDeletions();
                if (deletions.size() > 0) {
                    CinnamonServer.executorService.submit(new DeletionTask(deletions, contentProviderService));
                }
            }
            else{
                log.debug("TransactionStatus is not OK -> do not commit changes, roll back the sqlSession.");
                sqlSession.rollback();
            }

        } catch (Exception e) {
            log.warn("Caught unexpected exception -> rollback:", e);
            ThreadLocalSqlSession.getSqlSession().rollback();
            ErrorResponseGenerator.generateErrorMessage((HttpServletRequest) request, (HttpServletResponse) response, INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER, e.getMessage());
        }
        finally {
            ThreadLocalSqlSession.setCurrentUser(null);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        contentProviderService = (ContentProviderService) filterConfig.getServletContext().getAttribute(CONTENT_PROVIDER_SERVICE);
    }
}
