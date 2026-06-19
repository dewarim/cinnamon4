package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.service.FolderService;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.*;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.WriterOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "Ui", urlPatterns = "/ui/*")
public class UiServlet extends HttpServlet {

    private static final Logger        log           = LogManager.getLogger(UiServlet.class);
    private static final FolderService folderService = new FolderService();

    private static TemplateEngine templateEngine;

    public static TemplateEngine getTemplateEngine() {
        if (templateEngine == null) {
            templateEngine = createTemplateEngine();
        }
        return templateEngine;
    }

    private static TemplateEngine createTemplateEngine() {
        // Use live templates from source tree when available (development), otherwise use precompiled (production JAR)
        Path jteDir = Path.of("src/main/jte");
        if (jteDir.toFile().exists()) {
            log.info("Using JTE directory code resolver from {}", jteDir.toAbsolutePath());
            return TemplateEngine.create(new DirectoryCodeResolver(jteDir), ContentType.Html);
        }
        log.info("Using JTE precompiled templates");
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/folders"         -> handleFolderView(request, response);
            case "/folders/tree"    -> handleFolderTree(request, response);
            case "/folders/content" -> handleFolderContent(request, response);
            case "/osd/meta"        -> handleOsdMeta(request, response);
            case "/logout"          -> UiLoginServlet.handleLogout(request, response);
            default -> {
                // Default: redirect to folder view
                response.sendRedirect("/ui/folders");
            }
        }
    }

    private void handleFolderView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderService.ensureHomeFolderExists(user);
            folderPath = folderService.homeFolderPath(user);
        }

        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> params = new HashMap<>();
        params.put("folderPath", folderPath);
        params.put("username", user.getName());
        render("folders/view.jte", params, response);
    }

    private void handleFolderTree(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = folderService.homeFolderPath(user);
        }

        String homePath = folderService.homeFolderPath(user);
        Folder homeFolder;
        try {
            homeFolder = folderService.ensureHomeFolderExists(user);
        } catch (Exception e) {
            log.warn("Home folder not found for user {}: {}", user.getName(), e.getMessage());
            renderFragment("folders/tree-error.jte", Map.of("message", "Home folder not found."), response);
            return;
        }

        List<Folder> subFolders = folderService.getSubFolders(homeFolder.getId(), user);

        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> params = new HashMap<>();
        params.put("homeFolder", homeFolder);
        params.put("homePath", homePath);
        params.put("subFolders", subFolders);
        params.put("currentFolderPath", folderPath);
        renderFragment("folders/tree.jte", params, response);
    }

    private void handleFolderContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = folderService.homeFolderPath(user);
        }

        String filterParam = request.getParameter("filter");
        boolean latestHead = !"all".equals(filterParam);

        Folder currentFolder;
        try {
            currentFolder = folderService.resolvePath(folderPath, user);
        } catch (Exception e) {
            log.warn("Folder not found or not accessible: {}", folderPath);
            renderFragment("folders/content-error.jte", Map.of("message", "Folder not found or access denied."), response);
            return;
        }

        List<Folder>          subFolders = folderService.getSubFolders(currentFolder.getId(), user);
        List<ObjectSystemData> osds      = folderService.getFolderContent(currentFolder.getId(), latestHead, user);

        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> params = new HashMap<>();
        params.put("folder", currentFolder);
        params.put("folderPath", folderPath);
        params.put("subFolders", subFolders);
        params.put("osds", osds);
        params.put("latestHead", latestHead);
        renderFragment("folders/content.jte", params, response);
    }

    private void handleOsdMeta(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user = RequestScope.getCurrentUser();
        String osdIdParam = request.getParameter("osdId");
        if (osdIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        long osdId;
        try {
            osdId = Long.parseLong(osdIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        com.dewarim.cinnamon.dao.OsdDao osdDao = new com.dewarim.cinnamon.dao.OsdDao();
        ObjectSystemData osd = osdDao.getObjectById(osdId).orElse(null);
        if (osd == null) {
            renderFragment("folders/meta-empty.jte", Map.of(), response);
            return;
        }

        UserAccountDao userAccountDao = new UserAccountDao();
        String ownerName    = userAccountDao.getUserAccountById(osd.getOwnerId()).map(UserAccount::getName).orElse("unknown");
        String modifierName = osd.getModifierId() != null
                ? userAccountDao.getUserAccountById(osd.getModifierId()).map(UserAccount::getName).orElse("unknown")
                : "—";

        AclDao    aclDao    = new AclDao();
        FormatDao formatDao = new FormatDao();
        Acl    acl    = osd.getAclId() != null ? aclDao.getCachedVersion(osd.getAclId()) : null;
        Format format = osd.getFormatId() != null ? formatDao.getCachedVersion(osd.getFormatId()) : null;

        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> params = new HashMap<>();
        params.put("osd", osd);
        params.put("ownerName", ownerName);
        params.put("modifierName", modifierName);
        params.put("aclName", acl != null ? acl.getName() : "—");
        params.put("formatName", format != null ? format.getName() : "—");
        renderFragment("folders/osd-meta.jte", params, response);
    }

    private void render(String template, Map<String, Object> params, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        getTemplateEngine().render(template, params, new WriterOutput(response.getWriter()));
    }

    private void renderFragment(String template, Map<String, Object> params, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        getTemplateEngine().render(template, params, new WriterOutput(response.getWriter()));
    }
}
