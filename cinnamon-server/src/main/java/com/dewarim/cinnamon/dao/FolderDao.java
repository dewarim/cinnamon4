package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.model.Folder;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.Constants.ROOT_FOLDER_NAME;

public class FolderDao {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    // note: almost same code as OsdDao, although you could argue that fetching > 10K folders is a pathological case.
    public List<Folder> getFoldersById(List<Long> ids, boolean includeSummary) {
        SqlSession          sqlSession  = ThreadLocalSqlSession.getSqlSession();
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
            List<Folder> batch = sqlSession.selectList("com.dewarim.cinnamon.FolderMapper.getFoldersById", params);
            results.addAll(batch);
            rowCount += BATCH_SIZE;
        }
        return results;
    }

    public List<Folder> getFolderByIdWithAncestors(Long id, boolean includeSummary) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("includeSummary", includeSummary);
        params.put("id", id);
        return sqlSession.selectList("com.dewarim.cinnamon.FolderMapper.getFolderByIdWithAncestors", params);
    }

    public Folder getRootFolder(boolean includeSummary) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("includeSummary", includeSummary);
        params.put("rootFolderName", ROOT_FOLDER_NAME);
        return sqlSession.selectOne("com.dewarim.cinnamon.FolderMapper.getRootFolder", params);
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
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        List<String> pathElements = new ArrayList<>(Arrays.asList(path.substring(1).split("/")));
        List<String> names        = pathElements.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> params         = new HashMap<>();
        Folder              rootFolder     = getRootFolder(false);
        Folder              currentFolder  = null;
        Folder              targetFolder   = null;
        boolean             firstIteration = true;

        for (String name : pathElements) {
            List<Folder> targetFolders;
            if (firstIteration) {
                params.put("id", rootFolder.getId());
                params.put("name", name);
                firstIteration = false;
                targetFolders = sqlSession.selectList("com.dewarim.cinnamon.FolderMapper.getChildFolderOfRootByName", params);
            } else {
                params.put("parentId", currentFolder.getId());
                params.put("childName", name);
                targetFolders = sqlSession.selectList("com.dewarim.cinnamon.FolderMapper.getFolderByParentIdAndChildName", params);
            }

            if (targetFolders.size() != 1) {
                // complete path was not found: return empty list
                return Collections.emptyList();
            }
            targetFolder = targetFolders.get(0);
            currentFolder = targetFolder;
        }


        if (targetFolder == null) {
            return Collections.emptyList();
        }
        return getFolderByIdWithAncestors(targetFolder.getId(), includeSummary);

    }


    public Optional<Folder> getFolderById(long id) {
        List<Folder> folders = getFoldersById(Collections.singletonList(id), false);
        if (folders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(folders.get(0));
    }

    public void updateFolder(Folder folder) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.FolderMapper.updateFolder", folder);
    }
}
