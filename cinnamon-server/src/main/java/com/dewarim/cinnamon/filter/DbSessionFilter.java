package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

/**
 */
public class DbSessionFilter implements Filter {

    private  static final Logger log = LogManager.getLogger(DbSessionFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            log.debug("DbSessionFilter: before");
            chain.doFilter(request, response);
            log.debug("DbSessionFilter: after");
        }
        catch (Throwable t) {
            // TODO: wrap uncaught exceptions into proper CinnamonError response.
            // TODO: configure logging
            log.debug("exception: ",t);
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
        }
        finally {
            SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession(); 
            if(ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK){
                log.debug("commit changes (if any)");
                sqlSession.commit();
            }
            else{
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
