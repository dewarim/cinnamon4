package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DeletionTask;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;
import com.dewarim.cinnamon.dao.DeletionDao;
import com.dewarim.cinnamon.model.Deletion;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

/**
 *
 */
public class DbSessionFilter implements Filter {

    private static final Logger log = LogManager.getLogger(DbSessionFilter.class);

    private final ObjectMapper xmlMapper   = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
    private final DeletionDao  deletionDao = new DeletionDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try {
            log.debug("DbSessionFilter: before");
            chain.doFilter(request, response);
            log.debug("DbSessionFilter: after");
        } catch (Exception e) {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
            log.warn("Caught unexpected exception:", e);
            CinnamonError error = new CinnamonError(ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER.getCode(), e.getMessage());
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(CONTENT_TYPE_XML);
            response.setCharacterEncoding("UTF-8");
            xmlMapper.writeValue(response.getWriter(), error);
        } finally {
            SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
            if (ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK) {
                log.debug("commit changes (if any)");
                sqlSession.commit();
                // TODO: maybe check if an idling background thread uses less resources
                List<Deletion> deletions = deletionDao.listPendingDeletions();
                if (deletions.size() > 0) {
                    CinnamonServer.executorService.submit(new DeletionTask(deletions));
                }
            } else {
                log.debug("rollback changes");
                sqlSession.rollback();
            }
            sqlSession.close();
            ThreadLocalSqlSession.refreshSession();
        }

    }

    @Override
    public void destroy() {

    }
}
