package com.dewarim.cinnamon.configuration;

public class SecurityConfig {

    private int     passwordRounds        = 10;
    private int     minimumPasswordLength = 8;
    private Boolean transferAssetsAllowed = true;
    private Boolean deleteUserAllowed     = true;
    private long    sessionLengthInMillis = 3600_000;

    private LdapConfig ldapConfig = new LdapConfig();

    public Boolean getTransferAssetsAllowed() {
        return transferAssetsAllowed;
    }

    public void setTransferAssetsAllowed(Boolean transferAssetsAllowed) {
        this.transferAssetsAllowed = transferAssetsAllowed;
    }

    public Boolean getDeleteUserAllowed() {
        return deleteUserAllowed;
    }

    public void setDeleteUserAllowed(Boolean deleteUserAllowed) {
        this.deleteUserAllowed = deleteUserAllowed;
    }

    public int getPasswordRounds() {
        return passwordRounds;
    }

    public void setPasswordRounds(int passwordRounds) {
        this.passwordRounds = passwordRounds;
    }

    public int getMinimumPasswordLength() {
        return minimumPasswordLength;
    }

    public void setMinimumPasswordLength(int minimumPasswordLength) {
        this.minimumPasswordLength = minimumPasswordLength;
    }

    public long getSessionLengthInMillis() {
        return sessionLengthInMillis;
    }

    public void setSessionLengthInMillis(long sessionLengthInMillis) {
        this.sessionLengthInMillis = sessionLengthInMillis;
    }

    public LdapConfig getLdapConfig() {
        return ldapConfig;
    }

    public void setLdapConfig(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    @Override
    public String toString() {
        return "SecurityConfig{" +
                "passwordRounds=" + passwordRounds +
                ", minimumPasswordLength=" + minimumPasswordLength +
                ", transferAssetsAllowed=" + transferAssetsAllowed +
                ", deleteUserAllowed=" + deleteUserAllowed +
                ", sessionLengthInMillis=" + sessionLengthInMillis +
                ", ldapConfig=" + ldapConfig +
                '}';
    }
}
