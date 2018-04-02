package com.dewarim.cinnamon.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.filter.AuthenticationFilter;
import com.dewarim.cinnamon.filter.DbSessionFilter;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.DispatcherType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import static com.dewarim.cinnamon.Constants.DAO_USER_ACCOUNT;
import static com.dewarim.cinnamon.Constants.DEFAULT_DATABASE_SESSION_FACTORY;

/**
 */
public class CinnamonServer {

    private int port;
    private ServletHandler servletHandler = new ServletHandler();
    private Server server;
    private DbSessionFactory dbSessionFactory;
    public static CinnamonConfig config = new CinnamonConfig();

    public CinnamonServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        addFilters(servletHandler);
        addServlets(servletHandler);

        server = new Server(port);
        server.setHandler(servletHandler);
        server.start();

        addSingletons();
    }

    private void addSingletons() {

        // initialize mybatis:
        if (dbSessionFactory == null) {
            dbSessionFactory = new DbSessionFactory(null);
        }
        ThreadLocalSqlSession.dbSessionFactory = dbSessionFactory;
        server.setAttribute(DEFAULT_DATABASE_SESSION_FACTORY, dbSessionFactory);

        // add DAOs
        server.setAttribute(DAO_USER_ACCOUNT, new UserAccountDao());

    }

    private void addFilters(ServletHandler handler) {
        handler.addFilterWithMapping(DbSessionFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilterWithMapping(AuthenticationFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void addServlets(ServletHandler handler) {
        handler.addServletWithMapping(AclServlet.class, "/api/acl/*");
        handler.addServletWithMapping(CinnamonServlet.class, "/cinnamon/*");
        handler.addServletWithMapping(OsdServlet.class, "/api/osd/*");
        handler.addServletWithMapping(PermissionServlet.class, "/api/permission/*");
        handler.addServletWithMapping(UserServlet.class, "/api/user/*");
    }

    public static void main(String[] args) throws Exception {
        Args cliArguments = new Args();
        JCommander commander = JCommander.newBuilder().addObject(cliArguments).build();
        commander.parse(args);
        
        if((cliArguments.help)){
            commander.setColumnSize(80);
            commander.usage();
            return;
        }
        
        if (cliArguments.writeConfigFile != null) {
            writeConfig(cliArguments.writeConfigFile);
            return;
        }
        
        if (cliArguments.configFilename != null) {
            config = readConfig(cliArguments.configFilename);
        }
        
        if (cliArguments.port != null) {
            config.getServerConfig().setPort(cliArguments.port);
        }

        CinnamonServer server = new CinnamonServer(config.getServerConfig().getPort());
        server.start();
        server.getServer().join();
    }

    public void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    public Server getServer() {
        return server;
    }

    public static void writeConfig(String filename) {
        File configFile = new File(filename);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            ObjectMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.writeValue(fos, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static CinnamonConfig readConfig(String filename) {
        File configFile = new File(filename);
        try (FileInputStream fis = new FileInputStream(configFile)) {
            ObjectMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(fis, CinnamonConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Args {
        @Parameter(names = {"--port", "-p"}, description = "Port on which the server listens. Default is 9090.")
        Integer port;

        @Parameter(names = "--write-config", description = "Write the default configuration to this file")
        String writeConfigFile;

        @Parameter(names = {"--config", "-c"}, description = "Where to load the configuration file from")
        String configFilename;

        @Parameter(names = {"--help","-h"}, help = true, description = "Display help text.")
        boolean help;
    }
}
