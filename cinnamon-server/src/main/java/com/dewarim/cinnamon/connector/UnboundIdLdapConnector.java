package com.dewarim.cinnamon.connector;

import com.dewarim.cinnamon.model.configuration.LdapConfig;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.unboundid.ldap.sdk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LDAP connector using the UnboundId LDAP SDK licensed under LGPL 2.1
 * See: https://www.ldap.com/unboundid-ldap-sdk-for-java
 */
public class UnboundIdLdapConnector {

    private static final Logger log = LogManager.getLogger(UnboundIdLdapConnector.class);


    LdapConfig ldapConfig;

    public UnboundIdLdapConnector(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    public LdapResult connect(String username, String password) {
        LDAPConnection conn = null;
        try {
            log.debug("Connecting to {}:{} with '{}' for user '{}'", 
                    ldapConfig.getHost(), ldapConfig.getPort(), getBaseDn(username), username);
            conn = new LDAPConnection(ldapConfig.getHost(), ldapConfig.getPort(), getBaseDn(username), password);
            log.debug("connection: " + conn);
            final LDAPConnection connection = conn;
            List<LdapConfig.GroupMapping> groupMappings = ldapConfig.getGroupMappings().stream()
                    .filter(groupMapping -> searchForGroup(connection, groupMapping.getLdapGroup(), username))
                    .collect(Collectors.toList());

            return new LdapResult(!groupMappings.isEmpty(), groupMappings);

        } catch (Exception e) {
            log.warn("Failed to connect with LDAP server", e);
            // ldap error message is 0 terminated, which upsets the XML serializer for LdapResult.
            String errorMessage = e.getMessage().replace('\u0000', ' ');
            return new LdapResult("Failed to connect with LDAP server: " + errorMessage);
        } finally {
            if (conn != null && conn.isConnected()) {
                conn.close();
            }
        }
    }

    private boolean searchForGroup(LDAPConnection connection, String ldapGroupName, String username) {
        try {
            SearchResultEntry searchResultEntry = connection.searchForEntry(getSearchBaseDn(ldapGroupName),
                    SearchScope.BASE, ldapConfig.getSearchFilter(), ldapConfig.getSearchAttribute());

            String[] attributeValues = searchResultEntry.getAttributeValues(ldapConfig.getSearchAttribute());
            log.debug("looking at group '{}' with attributeValues '{}' starting with 'CN={},'", ldapGroupName, attributeValues, username);
            return Arrays.stream(attributeValues).anyMatch(member -> member.startsWith("CN=" + username + ","));

        } catch (LDAPSearchException e) {
            log.debug(String.format("Failed to search for group %s for user %s", ldapGroupName, username), e);
            return false;
        }
    }

    private String getBaseDn(String username) {
        return String.format(ldapConfig.getBindDnFormatString(), username);
    }

    private String getSearchBaseDn(String groupName) {
        return String.format(ldapConfig.getSearchBaseDnFormatString(), groupName);
    }

    public static void main(String[] args) throws IOException {
        String username = "John Doe";
        String password = "Dohn.Joe_1";
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        }
        XmlMapper mapper = new XmlMapper();
        LdapConfig ldapConfig = mapper.readValue(new File("ldap-config.xml"), LdapConfig.class);

        UnboundIdLdapConnector ldapConnector = new UnboundIdLdapConnector(ldapConfig);
        LdapResult result = ldapConnector.connect(username, password);
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, result);
        System.out.println("\n");
    }

    @JacksonXmlRootElement(localName = "ldapResult")
    public static class LdapResult {
        private String exception;
        private boolean validUser;
        private List<LdapConfig.GroupMapping> groupMappings = Collections.emptyList();

        public LdapResult(String exception) {
            this.exception = exception;
        }

        public LdapResult(boolean validUser, List<LdapConfig.GroupMapping> groupMappings) {
            this.validUser = validUser;
            this.groupMappings = groupMappings;
        }

        public boolean isValidUser() {
            return validUser;
        }

        public void setValidUser(boolean validUser) {
            this.validUser = validUser;
        }

        public List<LdapConfig.GroupMapping> getGroupMappings() {
            return groupMappings;
        }

        public void setGroupMappings(List<LdapConfig.GroupMapping> groupMappings) {
            this.groupMappings = groupMappings;
        }

        public String getException() {
            return exception;
        }

        public void setException(String exception) {
            this.exception = exception;
        }
    }

}
