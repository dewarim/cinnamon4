package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dewarim.cinnamon.ErrorCode.SYSTEM_FOLDER_NOT_FOUND;
import static com.dewarim.cinnamon.ErrorCode.UI_LANGUAGE_NOT_FOUND;

public class UserService {
    private static final Logger log = LogManager.getLogger();

    public UserAccount createOrUpdateUserAccount(String username, List<String> cinnamonGroups, LoginType loginType, String language) {
        UiLanguageDao uiLanguageDao = new UiLanguageDao();
        Optional<UiLanguage> uiLanguageOpt = uiLanguageDao.findByIsoCode(language);
        UiLanguage uiLanguage = uiLanguageOpt.orElseGet(() -> uiLanguageDao.findByIsoCode("und").orElseThrow(UI_LANGUAGE_NOT_FOUND.getException()));
        UserAccount user;
        UserAccountDao userDao = new UserAccountDao();
        Optional<UserAccount> account = userDao.getUserAccountByName(username);
        if (account.isEmpty()) {
            String randomPwd = UUID.randomUUID().toString();
            user = new UserAccount(username, randomPwd, username, username + "@invalid", uiLanguage.getId(), loginType.name(), true, true, true);
            userDao.create(List.of(user));
            user.setNewUser(true);
        } else {
            user = account.get();
        }

        // update the user's groups:
        GroupUserDao groupUserDao = new GroupUserDao();
        Long userId = user.getId();
        groupUserDao.removeUserFromGroups(userId, groupUserDao.listGroupsOfUser(userId).stream().map(GroupUser::getGroupId).toList());
        List<Long> groupIds = new GroupDao().getGroupsByName(cinnamonGroups).stream().map(Group::getId).toList();
        groupUserDao.addUserToGroups(userId, groupIds);

        // create folders for user:
        createFoldersForUser(user.getId(), username);
        return user;
    }

    private void createFoldersForUser(Long userId, String username) {
        FolderDao folderDao = new FolderDao();
        List<Folder> userFolders = folderDao.getFolderByPathWithAncestors("/system/users", false);
        if (userFolders.isEmpty()) {
            log.warn("/system/users not found.");
            return;
        }
        Folder allUsersFolder = userFolders.stream().filter(folder -> folder.getName().equals("users")).findFirst().orElseThrow(SYSTEM_FOLDER_NOT_FOUND.getException());
        Folder userFolder = folderDao.getOrCreateFolder(allUsersFolder.getId(), username, userId);
        final String[] defaultSystemFolders = {"home", "searches", "carts", "config"};
        for (String systemFolder : defaultSystemFolders) {
            folderDao.getOrCreateFolder(userFolder.getId(), systemFolder, userId);
        }
    }

}
