package com.dewarim.cinnamon.application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 */
public class CinnamonServer {

    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(9090);

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(CinnamonServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
