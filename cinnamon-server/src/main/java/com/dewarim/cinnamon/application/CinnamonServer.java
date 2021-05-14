package com.dewarim.cinnamon.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.dewarim.cinnamon.application.servlet.AclGroupServlet;
import com.dewarim.cinnamon.application.servlet.AclServlet;
import com.dewarim.cinnamon.application.servlet.CinnamonServlet;
import com.dewarim.cinnamon.application.servlet.ConfigEntryServlet;
import com.dewarim.cinnamon.application.servlet.ConfigServlet;
import com.dewarim.cinnamon.application.servlet.FolderServlet;
import com.dewarim.cinnamon.application.servlet.FolderTypeServlet;
import com.dewarim.cinnamon.application.servlet.FormatServlet;
import com.dewarim.cinnamon.application.servlet.GroupServlet;
import com.dewarim.cinnamon.application.servlet.IndexItemServlet;
import com.dewarim.cinnamon.application.servlet.LanguageServlet;
import com.dewarim.cinnamon.application.servlet.LifecycleServlet;
import com.dewarim.cinnamon.application.servlet.LifecycleStateServlet;
import com.dewarim.cinnamon.application.servlet.LinkServlet;
import com.dewarim.cinnamon.application.servlet.MetasetTypeServlet;
import com.dewarim.cinnamon.application.servlet.ObjectTypeServlet;
import com.dewarim.cinnamon.application.servlet.OsdServlet;
import com.dewarim.cinnamon.application.servlet.PermissionServlet;
import com.dewarim.cinnamon.application.servlet.RelationServlet;
import com.dewarim.cinnamon.application.servlet.RelationTypeServlet;
import com.dewarim.cinnamon.application.servlet.StaticServlet;
import com.dewarim.cinnamon.application.servlet.UiLanguageServlet;
import com.dewarim.cinnamon.application.servlet.UserServlet;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.filter.AuthenticationFilter;
import com.dewarim.cinnamon.filter.DbSessionFilter;
import com.dewarim.cinnamon.filter.RequestResponseFilter;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.DispatcherType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationDecorator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.DAO_USER_ACCOUNT;
import static com.dewarim.cinnamon.api.Constants.DEFAULT_DATABASE_SESSION_FACTORY;

/**
 *
 */
public class CinnamonServer {

    private static final Logger log = LogManager.getLogger(CinnamonServer.class);

    public static final String           VERSION       = "0.1.0";
    private             int              port;
    private             Server           server;
    private             DbSessionFactory dbSessionFactory;
    private             WebAppContext    webAppContext = new WebAppContext();
    public static       CinnamonConfig   config        = new CinnamonConfig();
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
        log.info("Starting CinnamonServer.");
        server.start();

        addSingletons();

        log.info("Server is running at port " + config.getServerConfig().getPort());
    }

    public void stop() throws Exception {
        server.stop();
    }

    private void addSingletons() {

        // initialize mybatis:
        if (dbSessionFactory == null) {
            log.info("Create new database session factory");
            dbSessionFactory = new DbSessionFactory(null);
        }
        ThreadLocalSqlSession.dbSessionFactory = dbSessionFactory;
        server.setAttribute(DEFAULT_DATABASE_SESSION_FACTORY, dbSessionFactory);

        // add DAOs
        UserAccountDao userAccountDao = new UserAccountDao();
        server.setAttribute(DAO_USER_ACCOUNT, userAccountDao);

        // test query:
        List<UserAccount> userAccounts = userAccountDao.listUserAccounts();
        log.info("Test query: database contains " + userAccounts.size() + " user accounts.");
    }

    private void addFilters(WebAppContext handler) {
        handler.addFilter(DbSessionFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(AuthenticationFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(RequestResponseFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void addServlets(WebAppContext handler) {
        handler.addServlet(AclServlet.class, "/api/acl/*");
        handler.addServlet(AclGroupServlet.class, "/api/aclGroup/*");
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
        Args       cliArguments = new Args();
        JCommander commander    = JCommander.newBuilder().addObject(cliArguments).build();
        commander.parse(args);

        if ((cliArguments.help)) {
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

        @Parameter(names = {"--help", "-h"}, help = true, description = "Display help text.")
        boolean help;
    }
}
