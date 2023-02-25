package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.DELETE;
import static com.dewarim.cinnamon.model.links.LinkType.FOLDER;
import static com.dewarim.cinnamon.model.links.LinkType.OBJECT;

public class DeleteLinkService {
    private static final Logger               log                  = LogManager.getLogger(DeleteLinkService.class);
    private final        AuthorizationService authorizationService = new AuthorizationService();

    public void verifyAndDelete(List<Link> links, UserAccount user, LinkDao linkDao) {

        List<Link> folderLinks           = links.stream().filter(link -> link.getType().equals(FOLDER)).collect(Collectors.toList());
        boolean    deleteFolderLinksOkay = deleteOkay(folderLinks, user);
        List<Link> osdLinks              = links.stream().filter(link -> link.getType().equals(OBJECT)).collect(Collectors.toList());
        boolean    deleteObjectLinksOkay = deleteOkay(osdLinks, user);
        if (deleteFolderLinksOkay && deleteObjectLinksOkay) {
            linkDao.delete(folderLinks.stream().map(Link::getId).collect(Collectors.toList()));
            linkDao.delete(osdLinks.stream().map(Link::getId).collect(Collectors.toList()));
        } else {
            log.warn("User does not have permission to delete all requested links.");
            // TODO: refactor to throw new FailedRequestException with list of bad links (like DeleteOsdService)
            throw ErrorCode.NO_DELETE_LINK_PERMISSION.exception();
        }
    }

    private boolean deleteOkay(List<Link> links, UserAccount user) {
        return links.stream().allMatch(link ->
                authorizationService.hasUserOrOwnerPermission(link, DELETE, user)
        );
    }

}
