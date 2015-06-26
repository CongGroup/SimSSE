package tool;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static tool.BaseTool.bytes2Hex;

/**
 * Created by HarryC on 7/5/14.
 * <p/>
 * This class is used to implement pseudo-random function.
 */
public class PRF {

    public static final String SHA256 = "SHA-256";

    public static byte[] SHA256(byte[] msg) {

        try {

            MessageDigest md = MessageDigest.getInstance(SHA256);

            md.update(msg);

            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String SHA256(String msg) {

        return SHA256(msg, 256);
    }

    /**
     * @param msg   input
     * @param limit limit the output size, should be bigger than 8
     * @return a limited digest
     */
    public static String SHA256(String msg, int limit) {

        String digest;

        byte[] bt;

        if (limit < 8) {
            System.err.println("The limit size should be bigger than 8!");
            return null;
        } else {
            limit = limit / 8;
        }

        try {

            MessageDigest md = MessageDigest.getInstance(SHA256);

            md.update(msg.getBytes());

            bt = md.digest();

            String oriDigest = bytes2Hex(bt);
            if (limit < 32) {
                digest = oriDigest.substring(0, 2 * limit);
            } else if (limit > 32) {

                StringBuilder buf = new StringBuilder(2 * limit);

                while (limit > 32) {
                    buf.append(oriDigest);
                    limit -= 32;
                }
                buf.append(oriDigest, 0, 2 * limit);

                digest = buf.toString();
            } else {
                digest = oriDigest;
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return digest;
    }

    public static long SHA256ToUnsignedInt(String msg) {

        long digest;

        try {

            MessageDigest md = MessageDigest.getInstance(SHA256);

            md.update(msg.getBytes());

            digest = BaseTool.bytesToUnsignedInt(md.digest());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }

        return digest;
    }

    public static long HMACSHA256ToUnsignedInt(String msg, String key) {

        long digest;

        try {

            Charset asciiCs = Charset.forName("US-ASCII");

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKey secretKey = new SecretKeySpec(asciiCs.encode(key).array(), "HmacSHA256");

            sha256_HMAC.init(secretKey);

            sha256_HMAC.update(msg.getBytes());

            digest = BaseTool.bytesToUnsignedInt(sha256_HMAC.doFinal());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return -2;
        }

        return digest;
    }

    public static long HMACSHA1ToUnsignedInt(String msg, String key) {

        long digest;

        try {

            Charset asciiCs = Charset.forName("US-ASCII");

            Mac sha128_HMAC = Mac.getInstance("HmacSHA1");

            SecretKey secretKey = new SecretKeySpec(asciiCs.encode(key).array(), "HmacSHA1");

            sha128_HMAC.init(secretKey);

            sha128_HMAC.update(msg.getBytes());

            digest = BaseTool.bytesToUnsignedInt(sha128_HMAC.doFinal());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return -2;
        }

        return digest;
    }

    public static byte[] HMACSHA256(String msg, String key) {

        byte[] digest;

        try {

            Charset asciiCs = Charset.forName("US-ASCII");

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKey secretKey = new SecretKeySpec(asciiCs.encode(key).array(), "HmacSHA256");

            sha256_HMAC.init(secretKey);

            sha256_HMAC.update(msg.getBytes());

            digest = sha256_HMAC.doFinal();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }

        return digest;
    }
}
