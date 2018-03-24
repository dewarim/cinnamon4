package com.dewarim.cinnamon.api.login;

import java.util.List;

public interface LoginResult {

    /**
     * @return true if the LoginProvider generating this result allows mapping from external groups
     * to CinnamonGroups. If false, getGroupMappings must return an empty list.
     */
    boolean groupMappingsImplemented();

    /**
     * GroupMappings allow a LoginProvider to map external groups to Cinnamon groups.
     * For example, if all LDAP users in group "retrieval-users" should be members in the Cinnamon group
     * "cinnamon.retrieval.users", the LdapLoginProvider can create a mapping between the two group
     * systems.
     * <p>
     * The Cinnamon server may add or remove a user from his or her groups depending on the group mappings
     * (implementation pending)
     * <p>
     * Note: in case your implementation does not provide a mapping (for example, an OAUTH login service
     * which returns "isValid" or "notValid"), you should return an empty list - and make sure
     * to return false on groupMappingsImplemented().
     *
     * @return a list of GroupMappings
     */
    List<GroupMapping> getGroupMappings();

    /**
     * @return true if the user is known to the LoginProvider and the user's password matches the 
     * stored passwordHash. 
     */
    boolean isValidUser();

    /**
     * @return an error message if something went wrong during login (network issues etc).
     * Must not be null, may be empty String.
     */
    String getErrorMessage();

}
