package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.FolderService;
import com.dewarim.cinnamon.application.service.OsdService;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.ContentProviderService;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.WriterOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dewarim.cinnamon.api.Constants.CONTENT_PROVIDER_SERVICE;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@MultipartConfig
@WebServlet(name = "Ui", urlPatterns = "/ui/*")
public class UiServlet extends HttpServlet {

    private static final Logger        log           = LogManager.getLogger(UiServlet.class);
    private static final FolderService folderService = new FolderService();

    private static TemplateEngine templateEngine;
    private        OsdService     osdService;

    public static TemplateEngine getTemplateEngine() {
        if (templateEngine == null) {
            templateEngine = createTemplateEngine();
        }
        return templateEngine;
    }

    private static TemplateEngine createTemplateEngine() {
        Path jteDir = Path.of("src/main/jte");
        if (jteDir.toFile().exists()) {
            log.info("Using JTE directory code resolver from {}", jteDir.toAbsolutePath());
            return TemplateEngine.create(new DirectoryCodeResolver(jteDir), Path.of("target/jte-classes"), ContentType.Html);
        }
        log.info("Using JTE precompiled templates");
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }

    @Override
    public void init() {
        ContentProviderService cps = (ContentProviderService) getServletContext().getAttribute(CONTENT_PROVIDER_SERVICE);
        osdService = new OsdService(cps);
    }

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        if (pathInfo.startsWith("/content/")) {
            handleContentStream(request, response);
            return;
        }

        switch (pathInfo) {
            case "/folders" -> handleFolderView(request, response);
            case "/folders/tree" -> handleFolderTree(request, response);
            case "/folders/navigate" -> handleFolderNavigate(request, response);
            case "/folders/content" -> handleFolderContent(request, response);
            case "/osd/meta" -> handleOsdMeta(request, response);
            case "/folder/create" -> handleFolderCreateForm(request, response);
            case "/folder/edit" -> handleFolderEditForm(request, response);
            case "/osd/create" -> handleOsdCreateForm(request, response);
            case "/osd/view" -> handleOsdView(request, response);
            case "/osd/edit" -> handleOsdEditForm(request, response);
            case "/logout" -> UiLoginServlet.handleLogout(request, response);
            default -> response.sendRedirect("/ui/folders");
        }
    }

    // ─── POST ─────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        switch (pathInfo) {
            case "/folder/create" -> handleFolderCreate(request, response);
            case "/folder/edit" -> handleFolderEdit(request, response);
            case "/folder/delete" -> handleFolderDelete(request, response);
            case "/osd/create" -> handleOsdCreate(request, response);
            case "/osd/edit" -> handleOsdEdit(request, response);
            case "/osd/delete" -> handleOsdDelete(request, response);
            case "/osd/copy" -> handleOsdCopy(request, response);
            case "/osd/version" -> handleOsdVersion(request, response);
            default -> response.sendRedirect("/ui/folders");
        }
    }

    // ─── Folder browse ────────────────────────────────────────────────────────

    private void handleFolderView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderService.ensureHomeFolderExists(user);
            folderPath = folderService.homeFolderPath(user);
        }
        String              filter = request.getParameter("filter");
        Map<String, Object> params = new HashMap<>();
        params.put("folderPath", folderPath);
        params.put("filter", filter != null ? filter : "");
        params.put("username", user.getName());
        render("folders/view.jte", params, response);
    }

    private void handleFolderTree(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = folderService.homeFolderPath(user);
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(buildTreePanelHtml(user, folderPath));
    }

    private void handleFolderNavigate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        String      filter     = request.getParameter("filter");
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = folderService.homeFolderPath(user);
        }

        String treeHtml    = buildTreePanelHtml(user, folderPath);
        String contentHtml = buildContentPanelHtml(user, folderPath, filter);

        response.setContentType("text/html;charset=UTF-8");
        var writer = response.getWriter();
        writer.write(treeHtml);
        writer.write("<div id=\"folder-content-panel\" hx-swap-oob=\"innerHTML\">");
        writer.write(contentHtml);
        writer.write("</div>");
    }

    private String buildTreePanelHtml(UserAccount user, String folderPath) throws IOException {
        Folder homeFolder;
        try {
            homeFolder = folderService.ensureHomeFolderExists(user);
        } catch (Exception e) {
            log.warn("Home folder not found for user {}: {}", user.getName(), e.getMessage());
            StringWriter sw = new StringWriter();
            getTemplateEngine().render("folders/tree-error.jte", Map.of("message", "Home folder not found."), new WriterOutput(sw));
            return sw.toString();
        }
        String      homePath = folderService.homeFolderPath(user);
        UiTreeNode  root     = buildTreeNode(homeFolder, homePath, folderPath, user);
        return renderTreeHtml(root);
    }

    private String buildContentPanelHtml(UserAccount user, String folderPath, String filter) throws IOException {
        boolean latestHead = !"all".equals(filter);
        Folder  currentFolder;
        try {
            currentFolder = folderService.resolvePath(folderPath, user);
        } catch (Exception e) {
            log.warn("Folder not found or not accessible: {}", folderPath);
            StringWriter sw = new StringWriter();
            getTemplateEngine().render("folders/content-error.jte",
                    Map.of("message", "Folder not found or access denied."), new WriterOutput(sw));
            return sw.toString();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("folder", currentFolder);
        params.put("folderPath", folderPath);
        params.put("subFolders", folderService.getSubFolders(currentFolder.getId(), user));
        params.put("osds", folderService.getFolderContent(currentFolder.getId(), latestHead, user));
        params.put("latestHead", latestHead);
        StringWriter sw = new StringWriter();
        getTemplateEngine().render("folders/content.jte", params, new WriterOutput(sw));
        return sw.toString();
    }

    private UiTreeNode buildTreeNode(Folder folder, String path, String currentPath, UserAccount user) {
        boolean        isCurrent  = path.equals(currentPath);
        boolean        isOnPath   = isCurrent || currentPath.startsWith(path + "/");
        List<Folder>   subFolders = folderService.getSubFolders(folder.getId(), user);
        List<UiTreeNode> children = new ArrayList<>();
        if (isOnPath) {
            for (Folder sub : subFolders) {
                children.add(buildTreeNode(sub, path + "/" + sub.getName(), currentPath, user));
            }
        }
        return new UiTreeNode(folder, path, isCurrent, children, !subFolders.isEmpty());
    }

    private String renderTreeHtml(UiTreeNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("<aside class=\"menu p-2\">");
        sb.append("<p class=\"menu-label\">Folders</p>");
        sb.append("<ul class=\"menu-list\">");
        appendTreeNodeHtml(sb, root);
        sb.append("</ul></aside>");
        return sb.toString();
    }

    private void appendTreeNodeHtml(StringBuilder sb, UiTreeNode node) {
        boolean expanded  = !node.children().isEmpty();
        String  indicator = expanded ? "▼ " : (node.hasChildren() ? "▶ " : "");
        String  emoji     = node.isCurrent() ? "📂 " : "📁 ";
        String  active    = node.isCurrent() ? " class=\"is-active\"" : "";
        String  path      = escapeAttr(node.path());

        sb.append("<li>");
        sb.append("<a href=\"/ui/folders?folderPath=").append(path).append("\"")
          .append(" hx-get=\"/ui/folders/navigate?folderPath=").append(path).append("\"")
          .append(" hx-target=\"#folder-tree-panel\"")
          .append(" hx-swap=\"innerHTML\"")
          .append(" hx-push-url=\"/ui/folders?folderPath=").append(path).append("\"")
          .append(active).append(">")
          .append(indicator).append(emoji).append(escapeHtml(node.folder().getName()))
          .append("</a>");
        if (expanded) {
            sb.append("<ul>");
            for (UiTreeNode child : node.children()) {
                appendTreeNodeHtml(sb, child);
            }
            sb.append("</ul>");
        }
        sb.append("</li>");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void handleFolderContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user       = RequestScope.getCurrentUser();
        String      folderPath = request.getParameter("folderPath");
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = folderService.homeFolderPath(user);
        }

        boolean latestHead = !"all".equals(request.getParameter("filter"));

        Folder currentFolder;
        try {
            currentFolder = folderService.resolvePath(folderPath, user);
        } catch (Exception e) {
            log.warn("Folder not found or not accessible: {}", folderPath);
            renderFragment("folders/content-error.jte", Map.of("message", "Folder not found or access denied."), response);
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("folder", currentFolder);
        params.put("folderPath", folderPath);
        params.put("subFolders", folderService.getSubFolders(currentFolder.getId(), user));
        params.put("osds", folderService.getFolderContent(currentFolder.getId(), latestHead, user));
        params.put("latestHead", latestHead);
        renderFragment("folders/content.jte", params, response);
    }

    private void handleOsdMeta(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        ObjectSystemData osd = new OsdDao().getObjectById(osdId).orElse(null);
        if (osd == null) {
            renderFragment("folders/meta-empty.jte", Map.of(), response);
            return;
        }

        UserAccountDao userAccountDao = new UserAccountDao();
        String         ownerName      = userAccountDao.getUserAccountById(osd.getOwnerId()).map(UserAccount::getName).orElse("unknown");
        String modifierName = osd.getModifierId() != null
                ? userAccountDao.getUserAccountById(osd.getModifierId()).map(UserAccount::getName).orElse("unknown")
                : "—";

        AclDao    aclDao    = new AclDao();
        FormatDao formatDao = new FormatDao();
        Acl       acl       = osd.getAclId() != null ? aclDao.getCachedVersion(osd.getAclId()) : null;
        Format    format    = osd.getFormatId() != null ? formatDao.getCachedVersion(osd.getFormatId()) : null;

        Map<String, Object> params = new HashMap<>();
        params.put("osd", osd);
        params.put("ownerName", ownerName);
        params.put("modifierName", modifierName);
        params.put("aclName", acl != null ? acl.getName() : "—");
        params.put("formatName", format != null ? format.getName() : "—");
        renderFragment("folders/osd-meta.jte", params, response);
    }

    // ─── Folder create ────────────────────────────────────────────────────────

    private void handleFolderCreateForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parentIdParam = request.getParameter("parentId");
        if (parentIdParam == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   parentId = Long.parseLong(parentIdParam);
        Folder parent   = new FolderDao().getFolderById(parentId).orElse(null);
        if (parent == null) {
            response.sendRedirect("/ui/folders");
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        params.put("parentName", parent.getName());
        params.put("username", RequestScope.getCurrentUser().getName());
        params.put("acls", new AclDao().list());
        params.put("folderTypes", new FolderTypeDao().list());
        params.put("lifecycles", new LifecycleDao().list());
        params.put("defaultAclId", parent.getAclId());
        params.put("error", nvl(request.getParameter("error")));
        render("folders/create.jte", params, response);
    }

    private void handleFolderCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user        = RequestScope.getCurrentUser();
        String      parentIdStr = request.getParameter("parentId");
        String      name        = nvl(request.getParameter("name")).strip();
        String      aclIdStr    = request.getParameter("aclId");
        String      typeIdStr   = request.getParameter("typeId");
        String      summary     = nvl(request.getParameter("summary"));

        if (parentIdStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long parentId = Long.parseLong(parentIdStr);

        if (name.isBlank()) {
            response.sendRedirect("/ui/folder/create?parentId=" + parentId + "&error=" + encode("Name is required."));
            return;
        }

        Long aclId  = aclIdStr != null && !aclIdStr.isBlank() ? Long.parseLong(aclIdStr) : null;
        Long typeId = typeIdStr != null && !typeIdStr.isBlank() ? Long.parseLong(typeIdStr) : null;

        try {
            Folder newFolder = folderService.createFolder(name, parentId, aclId, typeId, summary.isBlank() ? null : summary, user.getId(), user);
            String path      = new FolderDao().getFolderPath(newFolder.getId()).replace("/root", "");
            response.sendRedirect("/ui/folders?folderPath=" + encode(path));
        } catch (Exception e) {
            log.warn("Folder creation failed", e);
            response.sendRedirect("/ui/folder/create?parentId=" + parentId + "&error=" + encode(e.getMessage()));
        }
    }

    // ─── Folder edit ──────────────────────────────────────────────────────────

    private void handleFolderEditForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   folderId = Long.parseLong(idParam);
        Folder folder   = new FolderDao().getFolderById(folderId).orElse(null);
        if (folder == null) {
            response.sendRedirect("/ui/folders");
            return;
        }

        String folderPath = new FolderDao().getFolderPath(folderId).replace("/root", "");

        Map<String, Object> params = new HashMap<>();
        params.put("folder", folder);
        params.put("folderPath", folderPath);
        params.put("username", RequestScope.getCurrentUser().getName());
        params.put("acls", new AclDao().list());
        params.put("folderTypes", new FolderTypeDao().list());
        params.put("users", new UserAccountDao().listUserAccounts());
        params.put("error", nvl(request.getParameter("error")));
        render("folders/edit.jte", params, response);
    }

    private void handleFolderEdit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user  = RequestScope.getCurrentUser();
        String      idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   folderId  = Long.parseLong(idStr);
        String name      = request.getParameter("name");
        String parentStr = request.getParameter("parentId");
        String aclStr    = request.getParameter("aclId");
        String ownerStr  = request.getParameter("ownerId");
        String typeStr   = request.getParameter("typeId");

        Long parentId = parentStr != null && !parentStr.isBlank() ? Long.parseLong(parentStr) : null;
        Long aclId    = aclStr != null && !aclStr.isBlank() ? Long.parseLong(aclStr) : null;
        Long ownerId  = ownerStr != null && !ownerStr.isBlank() ? Long.parseLong(ownerStr) : null;
        Long typeId   = typeStr != null && !typeStr.isBlank() ? Long.parseLong(typeStr) : null;

        try {
            folderService.updateFolder(folderId, name, parentId, aclId, ownerId, typeId, null, user);
            response.sendRedirect("/ui/folder/edit?id=" + folderId);
        } catch (Exception e) {
            log.warn("Folder update failed", e);
            response.sendRedirect("/ui/folder/edit?id=" + folderId + "&error=" + encode(e.getMessage()));
        }
    }

    private void handleFolderDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user              = RequestScope.getCurrentUser();
        String      idStr             = request.getParameter("id");
        boolean     deleteContent     = "on".equals(request.getParameter("deleteContent"));
        boolean     deleteRecursively = "on".equals(request.getParameter("deleteRecursively"));

        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   folderId = Long.parseLong(idStr);
        Folder folder   = new FolderDao().getFolderById(folderId).orElse(null);
        if (folder == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        // get parent path before deleting
        String parentPath = folder.getParentId() != null
                ? new FolderDao().getFolderPath(folder.getParentId()).replace("/root", "")
                : folderService.homeFolderPath(user);

        try {
            folderService.deleteFolder(Set.of(folderId), deleteRecursively, deleteContent, user);
            response.sendRedirect("/ui/folders?folderPath=" + encode(parentPath));
        } catch (Exception e) {
            log.warn("Folder deletion failed", e);
            response.sendRedirect("/ui/folder/edit?id=" + folderId + "&error=" + encode(e.getMessage()));
        }
    }

    // ─── OSD create ───────────────────────────────────────────────────────────

    private void handleOsdCreateForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parentIdParam = request.getParameter("parentId");
        if (parentIdParam == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   parentId = Long.parseLong(parentIdParam);
        Folder parent   = new FolderDao().getFolderById(parentId).orElse(null);
        if (parent == null) {
            response.sendRedirect("/ui/folders");
            return;
        }

        String parentPath = new FolderDao().getFolderPath(parentId).replace("/root", "");

        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        params.put("parentPath", parentPath);
        params.put("username", RequestScope.getCurrentUser().getName());
        params.put("acls", new AclDao().list());
        params.put("objectTypes", new ObjectTypeDao().list());
        params.put("formats", new FormatDao().list());
        params.put("lifecycles", new LifecycleDao().list());
        params.put("defaultAclId", parent.getAclId());
        params.put("error", nvl(request.getParameter("error")));
        render("osds/create.jte", params, response);
    }

    private void handleOsdCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user         = RequestScope.getCurrentUser();
        String      parentIdStr  = request.getParameter("parentId");
        String      name         = nvl(request.getParameter("name")).strip();
        String      aclIdStr     = request.getParameter("aclId");
        String      typeIdStr    = request.getParameter("typeId");
        String      formatIdStr  = request.getParameter("formatId");
        String      lifecycleStr = request.getParameter("lifecycleStateId");
        String      summary      = nvl(request.getParameter("summary"));
        String      textContent  = nvl(request.getParameter("textContent"));

        if (parentIdStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long parentId = Long.parseLong(parentIdStr);

        if (name.isBlank()) {
            response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode("Name is required."));
            return;
        }

        Long aclId            = aclIdStr != null && !aclIdStr.isBlank() ? Long.parseLong(aclIdStr) : null;
        Long typeId           = typeIdStr != null && !typeIdStr.isBlank() ? Long.parseLong(typeIdStr) : null;
        Long formatId         = formatIdStr != null && !formatIdStr.isBlank() ? Long.parseLong(formatIdStr) : null;
        Long lifecycleStateId = lifecycleStr != null && !lifecycleStr.isBlank() ? Long.parseLong(lifecycleStr) : null;

        if (aclId == null) {
            response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode("ACL is required."));
            return;
        }
        if (typeId == null) {
            response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode("Object type is required."));
            return;
        }

        try {
            InputStream content      = null;
            Long        usedFormatId = formatId;

            Part filePart = request.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                if (formatId == null) {
                    response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode("Format is required when uploading a file."));
                    return;
                }
                content = filePart.getInputStream();
            } else if (!textContent.isBlank()) {
                if (formatId == null) {
                    response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode("Format is required when providing text content."));
                    return;
                }
                content = new java.io.ByteArrayInputStream(textContent.getBytes(StandardCharsets.UTF_8));
            }

            ObjectSystemData osd = osdService.createOsd(name, parentId, aclId, typeId, usedFormatId,
                    lifecycleStateId, summary.isBlank() ? null : summary, content, user);
            ThreadLocalSqlSession.getSqlSession().commit();
            response.sendRedirect("/ui/osd/view?id=" + osd.getId());
        } catch (Exception e) {
            log.warn("OSD creation failed", e);
            response.sendRedirect("/ui/osd/create?parentId=" + parentId + "&error=" + encode(e.getMessage()));
        }
    }

    // ─── OSD view ─────────────────────────────────────────────────────────────

    private void handleOsdView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long        osdId = Long.parseLong(idParam);
        UserAccount user  = RequestScope.getCurrentUser();

        ObjectSystemData osd;
        try {
            osd = osdService.getOsd(osdId, user);
        } catch (Exception e) {
            response.sendRedirect("/ui/folders");
            return;
        }

        UserAccountDao userAccountDao = new UserAccountDao();
        String         ownerName      = userAccountDao.getUserAccountById(osd.getOwnerId()).map(UserAccount::getName).orElse("unknown");
        Format         format         = osd.getFormatId() != null ? new FormatDao().getObjectById(osd.getFormatId()).orElse(null) : null;
        String folderPath = osd.getParentId() != null
                ? new FolderDao().getFolderPath(osd.getParentId()).replace("/root", "")
                : folderService.homeFolderPath(user);

        Map<String, Object> params = new HashMap<>();
        params.put("osd", osd);
        params.put("ownerName", ownerName);
        params.put("format", format);
        params.put("folderPath", folderPath);
        params.put("username", user.getName());
        params.put("error", nvl(request.getParameter("error")));
        render("osds/view.jte", params, response);
    }

    // ─── OSD edit ─────────────────────────────────────────────────────────────

    private void handleOsdEditForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long        osdId = Long.parseLong(idParam);
        UserAccount user  = RequestScope.getCurrentUser();

        ObjectSystemData osd;
        try {
            osd = osdService.getOsd(osdId, user);
        } catch (Exception e) {
            response.sendRedirect("/ui/folders");
            return;
        }

        UserAccountDao userAccountDao = new UserAccountDao();
        String         ownerName      = userAccountDao.getUserAccountById(osd.getOwnerId()).map(UserAccount::getName).orElse("unknown");
        Format         format         = osd.getFormatId() != null ? new FormatDao().getObjectById(osd.getFormatId()).orElse(null) : null;
        String folderPath = osd.getParentId() != null
                ? new FolderDao().getFolderPath(osd.getParentId()).replace("/root", "")
                : folderService.homeFolderPath(user);

        List<Meta>     metas;
        List<Relation> relations;
        try {
            metas = osdService.getMeta(osdId, user);
            relations = osdService.getRelations(osdId, user);
        } catch (Exception e) {
            metas = List.of();
            relations = List.of();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("osd", osd);
        params.put("ownerName", ownerName);
        params.put("format", format);
        params.put("folderPath", folderPath);
        params.put("username", user.getName());
        params.put("acls", new AclDao().list());
        params.put("objectTypes", new ObjectTypeDao().list());
        params.put("formats", new FormatDao().list());
        params.put("users", new UserAccountDao().listUserAccounts());
        params.put("metas", metas);
        params.put("relations", relations);
        params.put("error", nvl(request.getParameter("error")));
        render("osds/edit.jte", params, response);
    }

    private void handleOsdEdit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user  = RequestScope.getCurrentUser();
        String      idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long   osdId     = Long.parseLong(idStr);
        String name      = request.getParameter("name");
        String parentStr = request.getParameter("parentId");
        String aclStr    = request.getParameter("aclId");
        String ownerStr  = request.getParameter("ownerId");
        String typeStr   = request.getParameter("typeId");

        Long parentId = parentStr != null && !parentStr.isBlank() ? Long.parseLong(parentStr) : null;
        Long aclId    = aclStr != null && !aclStr.isBlank() ? Long.parseLong(aclStr) : null;
        Long ownerId  = ownerStr != null && !ownerStr.isBlank() ? Long.parseLong(ownerStr) : null;
        Long typeId   = typeStr != null && !typeStr.isBlank() ? Long.parseLong(typeStr) : null;

        try {
            osdService.updateOsd(osdId, name, parentId, aclId, ownerId, typeId, user);
            response.sendRedirect("/ui/osd/edit?id=" + osdId);
        } catch (Exception e) {
            log.warn("OSD update failed", e);
            response.sendRedirect("/ui/osd/edit?id=" + osdId + "&error=" + encode(e.getMessage()));
        }
    }

    private void handleOsdDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user  = RequestScope.getCurrentUser();
        String      idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long             osdId = Long.parseLong(idStr);
        ObjectSystemData osd   = new OsdDao().getObjectById(osdId).orElse(null);
        String parentPath = osd != null && osd.getParentId() != null
                ? new FolderDao().getFolderPath(osd.getParentId()).replace("/root", "")
                : folderService.homeFolderPath(user);

        try {
            osdService.deleteOsd(osdId, user);
            response.sendRedirect("/ui/folders?folderPath=" + encode(parentPath));
        } catch (Exception e) {
            log.warn("OSD deletion failed", e);
            response.sendRedirect("/ui/osd/view?id=" + osdId + "&error=" + encode(e.getMessage()));
        }
    }

    private void handleOsdCopy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user            = RequestScope.getCurrentUser();
        String      idStr           = request.getParameter("id");
        String      targetFolderStr = request.getParameter("targetFolderId");
        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long osdId          = Long.parseLong(idStr);
        long targetFolderId = targetFolderStr != null ? Long.parseLong(targetFolderStr) : osdId; // fallback: same folder

        // determine target folder: use same folder as source if not specified
        if (targetFolderStr == null || targetFolderStr.isBlank()) {
            ObjectSystemData src = new OsdDao().getObjectById(osdId).orElse(null);
            if (src != null) targetFolderId = src.getParentId();
        }

        try {
            ObjectSystemData copy = osdService.copyOsd(osdId, targetFolderId, user);
            response.sendRedirect("/ui/osd/edit?id=" + copy.getId());
        } catch (Exception e) {
            log.warn("OSD copy failed", e);
            response.sendRedirect("/ui/osd/view?id=" + osdId + "&error=" + encode(e.getMessage()));
        }
    }

    private void handleOsdVersion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAccount user  = RequestScope.getCurrentUser();
        String      idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect("/ui/folders");
            return;
        }
        long osdId = Long.parseLong(idStr);
        try {
            ObjectSystemData newVersion = osdService.newVersion(osdId, user);
            response.sendRedirect("/ui/osd/edit?id=" + newVersion.getId());
        } catch (Exception e) {
            log.warn("OSD versioning failed", e);
            response.sendRedirect("/ui/osd/view?id=" + osdId + "&error=" + encode(e.getMessage()));
        }
    }

    // ─── Content streaming ────────────────────────────────────────────────────

    private void handleContentStream(HttpServletRequest request, HttpServletResponse response) {
        String   pathInfo = request.getPathInfo(); // e.g. /content/42
        String[] parts    = pathInfo.split("/");
        if (parts.length < 3) {
            response.setStatus(SC_NOT_FOUND);
            return;
        }
        long osdId;
        try {
            osdId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            response.setStatus(SC_NOT_FOUND);
            return;
        }

        UserAccount user = RequestScope.getCurrentUser();
        try {
            OsdService.ContentResult result = osdService.getContent(osdId, user);
            response.setContentType(result.format().getContentType());
            response.setStatus(HttpServletResponse.SC_OK);
            result.stream().transferTo(response.getOutputStream());
        } catch (Exception e) {
            log.warn("Content streaming failed for OSD {}: {}", osdId, e.getMessage());
            response.setStatus(SC_NOT_FOUND);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void render(String template, Map<String, Object> params, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        getTemplateEngine().render(template, params, new WriterOutput(response.getWriter()));
    }

    private void renderFragment(String template, Map<String, Object> params, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        getTemplateEngine().render(template, params, new WriterOutput(response.getWriter()));
    }

    /**
     * Null-safe String value.
     */
    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String encode(String s) {
        return URLEncoder.encode(s != null ? s : "", StandardCharsets.UTF_8);
    }
}
