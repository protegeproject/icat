/**
 * 
 */
package edu.stanford.bmir.protege.web.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class contains methods to authenticate user securely with Hashing using MD5
 * 
 * @author z.khan
 * 
 */
public class AuthenticatinUtil {

    public boolean verifyChallengedHash(String storedHashedPswd, String response, String challenge) {
        if (storedHashedPswd == null) {
            return false;
        }

        AuthenticatinUtil authenticatinUtil = new AuthenticatinUtil();
        String challengedStoredPass = authenticatinUtil.makeDigestAddChallenge(storedHashedPswd, challenge);
        return response.equals(challengedStoredPass);
    }

    public String makeDigestAddChallenge(String hashedSaltedPassword, String challenge) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Did not have MD5 algorithm");
        }
        messageDigest.update(challenge.getBytes());
        messageDigest.update(hashedSaltedPassword.getBytes());
        String digest = encodeBytes(messageDigest.digest());
        return digest;
    }

    private String encodeBytes(byte[] bytes) {
        int stringLength = 2 * bytes.length;
        BigInteger bi = new BigInteger(1, bytes);
        String encoded = bi.toString(16);
        while (encoded.length() < stringLength) {
            encoded = "0" + encoded;
        }
        return encoded;
    }
}
