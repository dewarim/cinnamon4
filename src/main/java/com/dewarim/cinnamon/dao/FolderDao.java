package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderPath;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.ROOT_FOLDER_NAME;

/*
 * note: calling default update() implementation on Folder will not update changeTracking.
 */
public class FolderDao implements CrudDao<Folder> {

    private              SqlSession sqlSession;
    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int        BATCH_SIZE = 10000;

    public FolderDao() {
    }

    public FolderDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // note: almost same code as OsdDao, although you could argue that fetching > 10K folders is a pathological case.
    public List<Folder> getFoldersById(List<Long> ids, boolean includeSummary) {
        SqlSession          sqlSession  = getSqlSession();
        List<Folder>        results     = new ArrayList<>(ids.size());
        int                 requestSize = ids.size();
        int                 rowCount    = 0;
        Map<String, Object> params      = new HashMap<>();
        params.put("includeSummary", includeSummary);
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if (lastIndex > requestSize) {
                lastIndex = requestSize;
            }
            List<Long> partialList = ids.subList(rowCount, lastIndex);
            params.put("idList", partialList);
            List<Folder> batch = sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFoldersById", params);
            results.addAll(batch);
            rowCount += BATCH_SIZE;
        }
        return results;
    }

    public List<Folder> getFolderByIdWithAncestors(Long id, boolean includeSummary) {
        if (id == null) {
            // root has no parent, so looking up it's ancestors would otherwise fail.
            return List.of();
        }
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("includeSummary", includeSummary, "id", id);
        return sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFolderByIdWithAncestors", params);
    }

    public String getFolderPath(Long id) {
        if (id == null) {
            // root has no parent, so looking up it's ancestors would otherwise fail.
            return "/";
        }
        SqlSession   sqlSession = getSqlSession();
        List<String> paths      = sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFolderPath", id);
        return "/" + String.join("/", paths);
    }

    public Map<Long,String> getFolderPaths(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            // root has no parent, so looking up it's ancestors would otherwise fail.
            return Collections.emptyMap();
        }
        SqlSession       sqlSession = getSqlSession();
        List<FolderPath> paths      = sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFolderPaths", ids);
        return paths.stream().collect(Collectors.toMap(FolderPath::getId, FolderPath::getPath));
    }

    public Folder getRootFolder(boolean includeSummary) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("includeSummary", includeSummary, "rootFolderName", ROOT_FOLDER_NAME);
        return sqlSession.selectOne("com.dewarim.cinnamon.model.Folder.getRootFolder", params);
    }

    public Optional<Folder> getFolderByParentAndName(Long parentId, String name, boolean includeSummary) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("includeSummary", includeSummary, "name", name, "parentId", parentId);
        Folder              folder     = sqlSession.selectOne("com.dewarim.cinnamon.model.Folder.getFolderByParentAndName", params);
        return Optional.ofNullable(folder);
    }

    public List<Folder> getDirectSubFolders(Long id, boolean includeSummary) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("includeSummary", includeSummary, "id", id);
        return sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getDirectSubFolders", params);
    }

    public Folder saveFolder(Folder folder) {
        SqlSession sqlSession = getSqlSession();
        int        resultRows = sqlSession.insert("com.dewarim.cinnamon.model.Folder.insertFolder", folder);
        if (resultRows != 1) {
            ErrorCode.DB_INSERT_FAILED.throwUp();
        }
        new IndexJobDao(getSqlSession()).insertIndexJob(new IndexJob(IndexJobType.FOLDER, folder.getId(), IndexJobAction.CREATE));
        return folder;
    }


    /**
     * @param path           a slash '/'-separated list of folder names, starting with /, not including
     *                       the root folder.
     *                       Example:
     *                       "/home/admin/documents", not: "/root/home/admin/documents".
     * @param includeSummary true if summary should be returned
     * @return the requested folder and its ancestors
     */
    public List<Folder> getFolderByPathWithAncestors(String path, boolean includeSummary) {
        if (path.contains("//") || path.lastIndexOf('/') == path.length() - 1) {
            throw new BadArgumentException(ErrorCode.INVALID_FOLDER_PATH_STRUCTURE);
        }
        SqlSession   sqlSession   = getSqlSession();
        List<String> pathElements = new ArrayList<>(Arrays.asList(path.substring(1).split("/")));
        List<String> names        = pathElements.stream().filter(Objects::nonNull).toList();
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        Folder  rootFolder     = getRootFolder(false);
        Folder  currentFolder  = null;
        Folder  targetFolder   = null;
        boolean firstIteration = true;

        for (String name : pathElements) {
            List<Folder> targetFolders;
            if (firstIteration) {
                Map<String, Object> params = Map.of("id", rootFolder.getId(), "name", name);
                firstIteration = false;
                targetFolders  = sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getChildFolderOfRootByName", params);
            }
            else {
                Map<String, Object> params = Map.of("parentId", currentFolder.getId(), "childName", name);
                targetFolders = sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFolderByParentIdAndChildName", params);
            }

            if (targetFolders.size() != 1) {
                // complete path was not found: return empty list
                return Collections.emptyList();
            }
            targetFolder  = targetFolders.get(0);
            currentFolder = targetFolder;
        }


        if (targetFolder == null) {
            return Collections.emptyList();
        }
        return getFolderByIdWithAncestors(targetFolder.getId(), includeSummary);

    }


    public Optional<Folder> getFolderById(long id, boolean includeSummary) {
        List<Folder> folders = getFoldersById(Collections.singletonList(id), includeSummary);
        if (folders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(folders.get(0));
    }

    public Optional<Folder> getFolderById(long id) {
        List<Folder> folders = getFoldersById(Collections.singletonList(id), false);
        if (folders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(folders.get(0));
    }

    public void updateFolder(Folder folder) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.Folder.updateFolder", folder);
        new IndexJobDao(getSqlSession()).insertIndexJob(new IndexJob(IndexJobType.FOLDER, folder.getId(), IndexJobAction.UPDATE));
    }

    public boolean hasContent(List<Long> ids) {
        SqlSession sqlSession = getSqlSession();
        Long       count      = sqlSession.selectOne("com.dewarim.cinnamon.model.Folder.countContent", ids);
        return count > 0;
    }

    public boolean hasSubfolders(List<Long> ids) {
        SqlSession sqlSession = getSqlSession();
        Long       count      = sqlSession.selectOne("com.dewarim.cinnamon.model.Folder.countSubfolders", ids);
        return count > 0;
    }

    @Override
    public String getTypeClassName() {
        return Folder.class.getName();
    }

    public FolderDao setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    public List<Long> getRecursiveSubFolderIds(Long folderId) {
        return getSqlSession().selectList("com.dewarim.cinnamon.model.Folder.getRecursiveSubFolderIds", folderId);
    }

    public void updateOwnership(Long userId, Long assetReceiverId) {
        List<Folder> folders = getFoldersByOwner(userId);
        folders.forEach(folder -> {
            folder.setOwnerId(assetReceiverId);
            updateFolder(folder);
        });
    }

    private List<Folder> getFoldersByOwner(Long userId) {
        return getSqlSession().selectList("com.dewarim.cinnamon.model.Folder.getFoldersByOwnerId", userId);
    }

    @Override
    public int delete(List<Long> ids) {
        IndexJobDao jobDao = new IndexJobDao(getSqlSession());
        ids.forEach(id -> jobDao.insertIndexJob(new IndexJob(IndexJobType.FOLDER, id, IndexJobAction.DELETE)));
        return CrudDao.super.delete(ids);
    }

    public List<Ownable> getFoldersAsOwnables(Set<Long> folderIds) {
        SqlSession sqlSession = getSqlSession();
        if (folderIds == null || folderIds.isEmpty()) {
            return List.of();
        }
        return sqlSession.selectList("com.dewarim.cinnamon.model.Folder.getFoldersAsOwnables", folderIds.stream().toList());
    }

    public Set<Long> findKnownIds(List<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return Set.of();
        }

        List<List<Long>> partitions = CrudDao.partitionLongList(ids.stream().toList());
        SqlSession       sqlSession = getSqlSession();
        HashSet<Long>          results    = new HashSet<>(ids.size());
        partitions.forEach(partition -> {
            if (!partition.isEmpty()) {
                results.addAll(sqlSession.selectList("com.dewarim.cinnamon.model.Folder.findKnownIds", partition));
            }
        });

        return results;
    }
}
