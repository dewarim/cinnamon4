package com.dewarim.cinnamon.application;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 */
public class ThreadLocalSqlSession {

    private  static final Logger log = LogManager.getLogger(ThreadLocalSqlSession.class);
    
    static DbSessionFactory dbSessionFactory;

    private static final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>() {
        @Override
        protected SqlSession initialValue() {
            return dbSessionFactory.getSqlSessionFactory().openSession();
        }
    };

    private static final ThreadLocal<TransactionStatus> transactionStatus = ThreadLocal.withInitial(() -> TransactionStatus.OK);
    
    public static SqlSession getSqlSession(){
        return localSqlSession.get();
    }
    /**
     * After the current request is finished, create a new session for this thread.
     */
    public static SqlSession refreshSession(){
        log.debug("Refresh session for thread {}", Thread.currentThread().getName());

        if(localSqlSession.get() != null){
            try{
                localSqlSession.get().close();
            }
            catch (SqlSessionException e){
                log.debug("Closing sql session resulted in: ",e);
            }
        }
        SqlSession sqlSession = dbSessionFactory.getSqlSessionFactory().openSession();
        localSqlSession.set(sqlSession);
        setTransactionStatus(TransactionStatus.OK);
        return sqlSession;
    }

    public static TransactionStatus getTransactionStatus(){
        return transactionStatus.get();
    }
    
    public static void setTransactionStatus(TransactionStatus status){
        transactionStatus.set(status);
    }

    public static void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        ThreadLocalSqlSession.dbSessionFactory = dbSessionFactory;
    }

}
