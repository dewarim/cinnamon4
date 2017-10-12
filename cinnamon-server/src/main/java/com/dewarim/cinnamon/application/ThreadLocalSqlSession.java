package com.dewarim.cinnamon.application;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 */
public class ThreadLocalSqlSession {

    private  static final Logger log = LogManager.getLogger(ThreadLocalSqlSession.class);
    
    static DbSessionFactory dbSessionFactory;
    static TransactionIsolationLevel transactionIsolationLevel = TransactionIsolationLevel.SERIALIZABLE;
    
    private static final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<SqlSession>(){
        @Override
        protected SqlSession initialValue() {
            return dbSessionFactory.getSqlSessionFactory().openSession(transactionIsolationLevel);
        }
    };
    
    private static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<TransactionStatus>(){
        @Override
        protected TransactionStatus initialValue() {
            return TransactionStatus.OK;
        }
    };
    
    public static SqlSession getSqlSession(){
        return localSqlSession.get();
    }

    /**
     * After the current request is finished, create a new session for this thread.
     */
    public static void refreshSession(){
        log.debug("Refresh session for thread "+ Thread.currentThread().getName());
        localSqlSession.set(dbSessionFactory.getSqlSessionFactory().openSession(transactionIsolationLevel));
    }
    
    public static TransactionStatus getTransactionStatus(){
        return transactionStatus.get();
    }
    
    public static void setTransactionStatus(TransactionStatus status){
        transactionStatus.set(status);
    }

}
