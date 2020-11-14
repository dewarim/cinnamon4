package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 *
 */
public class DbSessionFilter implements Filter {

    private static final Logger log = LogManager.getLogger(DbSessionFilter.class);

    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
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
