package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.application.CinnamonServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Create hash digests of a string, salted with a salt value from the config.  
 *
 */
public class HashMaker {

	static Log log = LogFactory.getLog(HashMaker.class);
	
	/**
	 * Use Bcrypt to create a hashed string from a given text.
	 * Use before storing a password in the database.<br>
	 * Used by server.UserAccount.
     * @param text the password string from which a digest will be created.
     * @return a digest based upon the configured number of hash generation rounds and the specified plain-text.
	 */
	public static String createDigest(String text){
		Integer rounds = CinnamonServer.config.getSecurityConfig().getPasswordRounds();
		String digest = BCrypt.hashpw(text, BCrypt.gensalt(rounds));
		log.debug("digest:"+digest);
		return digest;
	}

    /**
     * Compare the hashed version of a given text with a specified hash string.
     * @param text the plain text password
     * @param hash the password hash
     * @return true if the hash is neither null nor different from the hashed text.
     */
	public static Boolean compareWithHash(String text, String hash){
        try{
            return hash != null && BCrypt.checkpw(text, hash);
        }
        catch (Exception e){
            log.warn("An exception occurred - failed to check password. Access denied!",e);
            return false;
        }
	}
}
