package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.api.login.GroupMapping;
import com.dewarim.cinnamon.configuration.LdapConfig;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LDAP connector using the UnboundId LDAP SDK licensed under Apache Artistic License 2.0
 * See: <a href="https://www.ldap.com/unboundid-ldap-sdk-for-java">entry at ldap.com</a> and
 * <a href="https://github.com/pingidentity/ldapsdk">LDAP SDK (Github)</a>
 *
 * <p>
 * Copied from Cinnamon-4 project.
 * This class is licensed under LGPL 2.1 for non-commercial or test use.
 * Commercial use requires separate license if actively used for LDAP login.
 */
public class UnboundIdLdapConnector {

    private static final Logger log = LoggerFactory.getLogger(UnboundIdLdapConnector.class);

    /**
     * Loaded once at start by BootStrap. If no ldap-config.xml is found, this remains an empty object.
     */
    public static LdapConfig config = new LdapConfig();

    private final LdapConfig ldapConfig;

    public UnboundIdLdapConnector() {
        ldapConfig = config;
    }

    public UnboundIdLdapConnector(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    public LdapLoginResult connect(String username, String password) {
        String escapedUsername = escapeUsername(username);
        log.info("LDAP username has been escaped to: " + escapedUsername);
        String actualPassword = password;
        if (ldapConfig.useStaticBindPassword()) {
            actualPassword = ldapConfig.getStaticBindPassword();
            log.info("Using staticBindPassword.");
        } else {
            log.info("staticBindPassword not found or empty - using user supplied password.");
        }

        LDAPConnection conn = null;
        try {
            log.info("Connecting to {}:{} with '{}' for user '{}'",
                    ldapConfig.getHost(), ldapConfig.getPort(), getBaseDn(escapedUsername), escapedUsername);
            conn = new LDAPConnection(ldapConfig.getHost(), ldapConfig.getPort(), getBaseDn(escapedUsername), actualPassword);
            log.info("connection: {}", conn);
            final LDAPConnection connection = conn;
            List<GroupMapping> groupMappings = ldapConfig.getGroupMappings().stream()
                    .filter(groupMapping -> searchForGroup(connection, groupMapping.getExternalGroup(), escapedUsername))
                    .collect(Collectors.toList());

            if (!groupMappings.isEmpty()) {
                // get distinguished name and try to connect anew with the given user's password.
                log.debug("Found group mappings for user {}, now trying to extract DN", escapedUsername);
                Optional<String> dnOpt = searchForDistinguishedName(connection, ldapConfig.getSearchAttributeForDn(), escapedUsername);
                if (dnOpt.isEmpty()) {
                    return new LdapLoginResult("Could not find distinguishedName for user.");
                }
                String distinguishedName = dnOpt.get();
                log.info("Trying to login with the user {} and DN {}", escapedUsername, distinguishedName);
                try (LDAPConnection dnConnection = new LDAPConnection(ldapConfig.getHost(), ldapConfig.getPort(), distinguishedName, password)) {
                    if (dnConnection.isConnected()) {
                        return new LdapLoginResult(true, groupMappings, ldapConfig.getDefaultLanguageCode());
                    }
                }
            }

            return new LdapLoginResult(!groupMappings.isEmpty(), groupMappings, ldapConfig.getDefaultLanguageCode());

        } catch (Exception e) {
            log.warn("Failed to connect with LDAP server", e);
            // ldap error message is 0 terminated, which upsets the XML serializer for LdapResult.
            String errorMessage = e.getMessage().replace('\u0000', ' ');
            return new LdapLoginResult("Failed to connect with LDAP server: " + errorMessage);
        } finally {
            if (conn != null && conn.isConnected()) {
                conn.close();
            }
        }
    }

    private Optional<String> searchForDistinguishedName(LDAPConnection connection, String ldapGroupName, String username) {
        try {
            String searchAttributeDn = ldapConfig.getSearchAttributeForDn();
            String searchFilter      = String.format(ldapConfig.getSearchFilter(), username);
            log.info("found group {} for {}, now looking for DN with searchAttributeDn {} and filter {}", ldapGroupName, username, searchAttributeDn, searchFilter);
            SearchResultEntry dnSearchResult = connection.searchForEntry(getSearchBaseDn(ldapGroupName), SearchScope.SUB, searchFilter);
            if (dnSearchResult == null) {
                log.warn("No result found while searching for distinguishedName '{}' for {} with searchFilter {}", ldapGroupName, username, ldapConfig.getSearchAttributeForDn());
                return Optional.empty();
            }
            StringBuilder buffer = new StringBuilder();
            dnSearchResult.toString(buffer);
            log.debug("Search for DN returned: {}", buffer);

            String[] dnAttributeValues = dnSearchResult.getAttributeValues(searchAttributeDn);
            return switch (dnAttributeValues.length) {
                case 0 -> {
                    log.info("Failed login - could not find DN for user {}", username);
                    yield Optional.empty();
                }
                case 1 -> {
                    log.info("Success - Found DN '{}' for user {}", dnAttributeValues[0], username);
                    yield Optional.of(dnAttributeValues[0]);
                }
                default -> {
                    log.info("Found more than one DN, will not proceed:\n {}", String.join("\n", dnAttributeValues));
                    yield Optional.empty();
                }
            };
        } catch (LDAPSearchException e) {
            log.warn(String.format("Failed to search for DN %s for user %s", ldapGroupName, username), e);
            return Optional.empty();
        }
    }

    private boolean searchForGroup(LDAPConnection connection, String ldapGroupName, String username) {
        try {
            String searchBaseDn            = getSearchBaseDn(ldapGroupName);
            String searchFilter            = String.format(ldapConfig.getSearchFilter(), username);
            String searchAttributeForGroup = ldapConfig.getSearchAttributeForGroup();

            log.info("Searching for user {} with searchBaseDn {} and searchFilter {}", username, searchBaseDn, searchFilter);
            SearchResultEntry searchResultEntry = connection.searchForEntry(searchBaseDn, SearchScope.SUB, searchFilter);
            if (searchResultEntry == null) {
                log.warn("No result found ");
                return false;
            }
            StringBuilder buffer = new StringBuilder();
            searchResultEntry.toString(buffer);
            log.debug("Search for group returned: {}", buffer);

            log.debug("Found searchResultEntry. Now reading attributeValues with searchAttributeForGroup: {}", searchAttributeForGroup);
            String[] attributeValues = searchResultEntry.getAttributeValues(searchAttributeForGroup);
            log.debug("looking at group '{}' with attributeValues '{}' starting with 'CN={},'", ldapGroupName, attributeValues, ldapGroupName);
            return Arrays.stream(attributeValues).anyMatch(member -> {
                log.debug("Check if {} startsWith CN={}", member, ldapGroupName);
                return member.startsWith("CN=" + ldapGroupName + ",");
            });
        } catch (LDAPSearchException e) {
            log.warn(String.format("Failed to search for group %s for user %s", ldapGroupName, username), e);
            return false;
        }
    }

    private String getBaseDn(String username) {
        return String.format(ldapConfig.getBindDnFormatString(), username);
    }

    private String escapeUsername(String username) {
        return username.replace(",", "\\,");
    }

    private String getSearchBaseDn(String groupName) {
        return String.format(ldapConfig.getSearchBaseDnFormatString(), groupName);
    }

    public boolean isInitialized() {
        return ldapConfig != null && ldapConfig.getHost() != null && ldapConfig.getPort() > 0;
    }

    public static void main(String[] args) throws IOException {
        String username = "John Doe";
        String password = "Dohn.Joe_1";
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        }
        XmlMapper  mapper     = new XmlMapper();
        LdapConfig ldapConfig = mapper.readValue(new File("temp/ldap-config.xml"), LdapConfig.class);

        UnboundIdLdapConnector ldapConnector = new UnboundIdLdapConnector(ldapConfig);
        LdapLoginResult result        = ldapConnector.connect(username, password);
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, result);
        System.out.println("\n");
    }

    public String getDefaultLanguageCode() {
        return config.getDefaultLanguageCode();
    }

}
