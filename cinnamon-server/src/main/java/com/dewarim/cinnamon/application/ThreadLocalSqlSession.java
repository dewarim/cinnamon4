package com.dewarim.cinnamon.application;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 */
public class ThreadLocalSqlSession {
    
    static DbSessionFactory dbSessionFactory;
    
    private static final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<SqlSession>(){
        @Override
        protected SqlSession initialValue() {
            return dbSessionFactory.getSqlSessionFactory().openSession(TransactionIsolationLevel.SERIALIZABLE);
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
    
    public static TransactionStatus getTransactionStatus(){
        return transactionStatus.get();
    }
    
    public static void setTransactionStatus(TransactionStatus status){
        transactionStatus.set(status);
    }

}
