package com.dewarim.cinnamon.model.configuration;

public class LdapConfig {

    /**
     * The LDAP server host's address. 
     */
    private String host;
    
    /**
     * Port on which to reach the LDAP server. The default (for testing) is 10389,
     * production port is usually 389.
     */
    private int port = 10389;
    
    /**
     * internal String.format string for bindDN.
     * Example: 
     * cn=%s,cn=Users,dc=cinnamon,dc=dewarim,dc=com
     * 
     * %s will be replaced by the user name.
     */
    private String bindDnFormatstring = "cn=%s,cn=Users,dc=localhost";

    /**
     * Search query.
     * Example:
     * cn=retrieval-users,cn=Users,dc=cinnamon,dc=dewarim,dc=coom
     */
    private String searchBaseDn = "cn=retrieval-users,cn=Users,dc=localhost";

    /**
     * Search filter to fetch the list of users allowed to login to the Cinnamon server.
     */
    private String searchFilter = "(&(objectclass=*))";

    /**
     * Name of the attribute which contains the user list.
     * Note: the expectation is currently that the returned searchResultEntry contains a list of
     * attribute "member" which is a String starting with CN=$username, for example: "CN=John Doe" 
     */
    private String searchAttribute = "member";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindDnFormatstring() {
        return bindDnFormatstring;
    }

    public void setBindDnFormatstring(String bindDnFormatstring) {
        this.bindDnFormatstring = bindDnFormatstring;
    }

    public String getSearchBaseDn() {
        return searchBaseDn;
    }

    public void setSearchBaseDn(String searchBaseDn) {
        this.searchBaseDn = searchBaseDn;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }
}
