package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.dao.UserAccountDao;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import static com.dewarim.cinnamon.Constants.DAO_USER_ACCOUNT;
import static com.dewarim.cinnamon.Constants.DEFAULT_DATABASE_SESSION_FACTORY;

/**
 */
public class CinnamonServer {

    private int port;
    private ServletHandler servletHandler = new ServletHandler();
    private Server server;
    private DbSessionFactory dbSessionFactory;
    
    public CinnamonServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        
        addServlets(servletHandler);
        
        server = new Server(port);
        server.setHandler(servletHandler);
        server.start();

        addSingletons();
    }
    
    private void addSingletons(){
        
        // initialize mybatis:
        if(dbSessionFactory == null){
            dbSessionFactory = new DbSessionFactory(null);
        }
        server.setAttribute(DEFAULT_DATABASE_SESSION_FACTORY, dbSessionFactory);
        
        // add DAOs
        server.setAttribute(DAO_USER_ACCOUNT, new UserAccountDao(dbSessionFactory));
        
    }
    
    private void addServlets(ServletHandler handler){
        handler.addServletWithMapping(CinnamonServlet.class, "/*");
    }

    public static void main(String[] args) throws Exception {
        CinnamonServer server = new CinnamonServer(9090);
        server.start();
        server.getServer().join();
    }

    public void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    public Server getServer() {
        return server;
    }
}
