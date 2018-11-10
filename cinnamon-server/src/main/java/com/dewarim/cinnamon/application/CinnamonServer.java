package com.dewarim.cinnamon.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.dewarim.cinnamon.application.servlet.*;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.filter.AuthenticationFilter;
import com.dewarim.cinnamon.filter.DbSessionFilter;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.eclipse.jetty.annotations.AnnotationDecorator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

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

    private int                  port;
    private Server               server;
    private DbSessionFactory     dbSessionFactory;
    private WebAppContext        webAppContext = new WebAppContext();
    public static CinnamonConfig config        = new CinnamonConfig();

    public CinnamonServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        webAppContext.setContextPath("/");
        webAppContext.setResourceBase(".");
        webAppContext.getObjectFactory().addDecorator(new AnnotationDecorator(webAppContext));

        addFilters(webAppContext);
        addServlets(webAppContext);
        server = new Server(port);
        server.setHandler(webAppContext);
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

    private void addFilters(WebAppContext handler) {
        handler.addFilter(DbSessionFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(AuthenticationFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void addServlets(WebAppContext handler) {
        handler.addServlet(AclServlet.class, "/api/acl/*");
        handler.addServlet(AclEntryServlet.class, "/api/aclEntry/*");
        handler.addServlet(CinnamonServlet.class, "/cinnamon/*");
        handler.addServlet(ConfigServlet.class, "/api/config/*");
        handler.addServlet(ConfigEntryServlet.class, "/api/configEntry/*");
        handler.addServlet(FormatServlet.class, "/api/format/*");
        handler.addServlet(FolderServlet.class, "/api/folder/*");
        handler.addServlet(FolderTypeServlet.class, "/api/folderType/*");
        handler.addServlet(GroupServlet.class, "/api/group/*");
        handler.addServlet(IndexItemServlet.class, "/api/indexItem/*");
        handler.addServlet(LanguageServlet.class, "/api/language/*");
        handler.addServlet(LifecycleServlet.class, "/api/lifecycle/*");
        handler.addServlet(LifecycleStateServlet.class, "/api/lifecycleState/*");
        handler.addServlet(LinkServlet.class, "/api/link/*");
        handler.addServlet(MetasetTypeServlet.class, "/api/metasetType/*");
        handler.addServlet(OsdServlet.class, "/api/osd/*");
        handler.addServlet(ObjectTypeServlet.class, "/api/objectType/*");
        handler.addServlet(PermissionServlet.class, "/api/permission/*");
        handler.addServlet(RelationServlet.class, "/api/relation/*");
        handler.addServlet(RelationTypeServlet.class, "/api/relationType/*");
        handler.addServlet(StaticServlet.class, "/static/*");
        handler.addServlet(UiLanguageServlet.class, "/api/uiLanguage/*");
        handler.addServlet(UserServlet.class, "/api/user/*");
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
