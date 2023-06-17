package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 */
public class ThreadLocalSqlSession {

    private  static final Logger log = LogManager.getLogger(ThreadLocalSqlSession.class);
    
    static DbSessionFactory dbSessionFactory;
    static TransactionIsolationLevel transactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED;
    
    private static final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>() {
        @Override
        protected SqlSession initialValue() {
            return dbSessionFactory.getSqlSessionFactory().openSession(transactionIsolationLevel);
        }
    };
    
    private static final ThreadLocal<TransactionStatus> transactionStatus = ThreadLocal.withInitial(() -> TransactionStatus.OK);
    
    public static SqlSession getSqlSession(){
        return localSqlSession.get();
    }

    /**
     * After the current request is finished, create a new session for this thread.
     */
    public static void refreshSession(){
        log.debug("Refresh session for thread "+ Thread.currentThread().getName());

        if(localSqlSession.get() != null){
            try{
                localSqlSession.get().close();
            }
            catch (SqlSessionException e){
                log.debug("Closing sql session resulted in: ",e);
            }
        }

        localSqlSession.set(dbSessionFactory.getSqlSessionFactory().openSession(transactionIsolationLevel));
        setTransactionStatus(TransactionStatus.OK);
    }

    public static SqlSession getNewSession(TransactionIsolationLevel isolationLevel){
        if(isolationLevel != null){
            return dbSessionFactory.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, isolationLevel);
        }
        return dbSessionFactory.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, transactionIsolationLevel);
    }
    
    public static TransactionStatus getTransactionStatus(){
        return transactionStatus.get();
    }
    
    public static void setTransactionStatus(TransactionStatus status){
        transactionStatus.set(status);
    }
    
    // UserAccount of the currently connected user.
    private static final ThreadLocal<UserAccount> currentUser = ThreadLocal.withInitial(() -> null);
    
    public static UserAccount getCurrentUser(){
        return currentUser.get();
    }
    
    public static void setCurrentUser(UserAccount user){
        currentUser.set(user);
    }

    public static void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        ThreadLocalSqlSession.dbSessionFactory = dbSessionFactory;
    }
}
