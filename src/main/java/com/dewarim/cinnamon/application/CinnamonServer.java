package com.dewarim.cinnamon.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.service.IndexService;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.application.servlet.*;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.filter.AuthenticationFilter;
import com.dewarim.cinnamon.filter.ChangeTriggerFilter;
import com.dewarim.cinnamon.filter.DbSessionFilter;
import com.dewarim.cinnamon.filter.RequestResponseFilter;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.HashMaker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dewarim.cinnamon.api.Constants.*;

/**
 *
 */
public class CinnamonServer {

    private static final Logger log = LogManager.getLogger(CinnamonServer.class);

    public static final String           VERSION       = "0.5.7";
    private final       int              port;
    private             Server           server;
    private             DbSessionFactory dbSessionFactory;
    private final       WebAppContext    webAppContext = new WebAppContext();
    public static       CinnamonConfig   config        = new CinnamonConfig();
    public static       ExecutorService  executorService;
    public static       CinnamonStats    cinnamonStats = new CinnamonStats();
    private             IndexService     indexService;
    private             SearchService    searchService;
    private TikaService tikaService;
    private static      Thread           indexServiceThread;

    public CinnamonServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        webAppContext.setContextPath("/");
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        // set the resource to a non-existent root directory
        webAppContext.setResourceBase(HashMaker.createDigest("cinnamon"));
        webAppContext.getObjectFactory().addDecorator(new AnnotationDecorator(webAppContext));

        addFilters(webAppContext);
        addServlets(webAppContext);
        server = new Server(port);
        server.setHandler(webAppContext);
        log.info("Starting CinnamonServer.");
        server.start();

        addSingletons();

        // start executorService for background threads
        // TODO: make number of threads and timeout configurable
        executorService = new ThreadPoolExecutor(4, 16, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));

        log.info("Server is running at port " + config.getServerConfig().getPort());
    }

    public void startIndexService() {
        indexService = new IndexService(config.getLuceneConfig());
        indexServiceThread = new Thread(indexService);
        indexServiceThread.setName("Index-Service");
        indexServiceThread.start();
    }

    public void stop() throws Exception {
        // TODO: maybe wait for indexService to be fully stopped?
        indexService.setStopped(true);
        server.stop();
    }

    private void addSingletons() throws IOException, InterruptedException {

        // initialize mybatis:
        if (dbSessionFactory == null) {
            log.info("Create new database session factory");
            dbSessionFactory = new DbSessionFactory(null);
        }
        ThreadLocalSqlSession.dbSessionFactory = dbSessionFactory;
        // TODO: unused?
        server.setAttribute(DEFAULT_DATABASE_SESSION_FACTORY, dbSessionFactory);

        startIndexService();

        searchService = new SearchService(config.getLuceneConfig());
        tikaService = new TikaService(config.getCinnamonTikaConfig());

        webAppContext.setAttribute(TIKA_SERVICE, tikaService);
        webAppContext.setAttribute(SEARCH_SERVICE, searchService);
        webAppContext.setAttribute(INDEX_SERVICE, indexService);
        webAppContext.setAttribute(CINNAMON_CONFIG, config);

        // test query:
        // add DAOs
        UserAccountDao userAccountDao = new UserAccountDao();
        // TODO: unused?
        server.setAttribute(DAO_USER_ACCOUNT, userAccountDao);
        List<UserAccount> userAccounts = userAccountDao.listUserAccounts();
        log.info("Test query: database contains " + userAccounts.size() + " user accounts.");
    }

    private void addFilters(WebAppContext handler) {
        handler.addFilter(DbSessionFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(AuthenticationFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(RequestResponseFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
        // TODO: it would be cleaner to add filter params to ChangeTriggerFilter here instead of fetching
        //  the server config statically.
        handler.addFilter(ChangeTriggerFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(RequestResponseFilter.class, "/cinnamon/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(RequestResponseFilter.class, "/test/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void addServlets(WebAppContext handler) {
        handler.addServlet(AclServlet.class, "/api/acl/*");
        handler.addServlet(AclGroupServlet.class, "/api/aclGroup/*");
        handler.addServlet(CinnamonServlet.class, "/cinnamon/*");
        handler.addServlet(ChangeTriggerServlet.class, "/api/changeTrigger/*");
        handler.addServlet(ConfigServlet.class, "/api/config/*");
        handler.addServlet(ConfigEntryServlet.class, "/api/configEntry/*");
        handler.addServlet(FormatServlet.class, "/api/format/*");
        handler.addServlet(FolderServlet.class, "/api/folder/*");
        handler.addServlet(FolderTypeServlet.class, "/api/folderType/*");
        handler.addServlet(GroupServlet.class, "/api/group/*");
        handler.addServlet(IndexServlet.class, "/api/index/*");
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
        handler.addServlet(SearchServlet.class, "/api/search/*");
        handler.addServlet(StaticServlet.class, "/static/*");
        handler.addServlet(TestServlet.class, "/api/test/*");
        handler.addServlet(UiLanguageServlet.class, "/api/uiLanguage/*");
        handler.addServlet(UserAccountServlet.class, "/api/user/*");
    }

    public static void main(String[] args) throws Exception {
        Args       cliArguments = new Args();
        JCommander commander    = JCommander.newBuilder().addObject(cliArguments).build();
        commander.parse(args);

        if (cliArguments.help) {
            commander.setColumnSize(80);
            commander.usage();
            return;
        }

        if (cliArguments.api) {
            printApi();
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

    private static void printApi() {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        Arrays.stream(UrlMapping.values()).forEach(urlMapping -> {
                    String formatted;
                    if (urlMapping.getRequestClass() != null) {
                        try {

                            String template = """
                                    # __endpoint__
                                    __description__
                                                                        
                                    ## Request
                                                                        
                                    __request__
                                                                        
                                    ## Response

                                    __response__

                                    ---
                                    """;
                            formatted = template
                                    .replace("__endpoint__", urlMapping.getPath())
                                    .replace("__description__", urlMapping.getDescription())
                                    .replace("__request__", requestToExample(mapper, urlMapping.getRequestClass()))
                                    .replace("__response__", responseToExample(mapper, urlMapping.getResponseClass()))
                            ;
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to format API template.", e);
                        }
                    } else {
                        String template = """
                                # __endpoint__
                                __description__
                                                                    
                                ---
                                """;
                        formatted = template
                                .replace("__endpoint__", urlMapping.getPath())
                                .replace("__description__", urlMapping.getDescription());
                    }
                    System.out.println(formatted);
                }
        );
    }

    private static String requestToExample(ObjectMapper mapper, Class<? extends ApiRequest> apiRequestClass) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (apiRequestClass == null) {
            return "";
        }
        ApiRequest request = apiRequestClass.getConstructor().newInstance();
        return exampleToText(mapper, request.examples());
    }

    private static String responseToExample(ObjectMapper mapper, Class<? extends ApiResponse> apiResponseClass) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (apiResponseClass == null) {
            return "";
        }
        ApiResponse response = apiResponseClass.getConstructor().newInstance();
        return exampleToText(mapper, response.examples());
    }

    private static String exampleToText(ObjectMapper mapper, List<Object> examples) throws JsonProcessingException {
        StringBuilder builder = new StringBuilder();
        for (Object example : examples) {
            builder.append("```xml\n");
            builder.append(mapper.writeValueAsString(example));
            builder.append("\n```\n");
        }
        return builder.toString();
    }

    public void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    public Server getServer() {
        return server;
    }

    public static CinnamonConfig getConfig() {
        return config;
    }

    public static void writeConfig(String filename) {
        File configFile = new File(filename);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            ObjectMapper xmlMapper = XML_MAPPER;
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

        @Parameter(names = "--write-config", description = "Write the default configuration to the filename given.")
        String writeConfigFile;

        @Parameter(names = {"--config", "-c"}, description = "Where to load the configuration file from")
        String configFilename;

        @Parameter(names = {"--help", "-h"}, help = true, description = "Display help text.")
        boolean help;

        @Parameter(names = {"--api"}, description = "Print API documentation")
        boolean api;
    }

    public IndexService getIndexService() {
        return indexService;
    }
}
