package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.dao.GroupUserDao;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dewarim.cinnamon.ErrorCode.UI_LANGUAGE_NOT_FOUND;

public class UserService {

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

        return user;
    }


}
