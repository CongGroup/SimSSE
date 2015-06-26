package tool;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by HarryC on 20/6/14.
 */
public class AESCoder {

	/**
	 * encryption algorithm
	 */
	private static final String KEY_ALGORITHM = "AES";

	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * initialize the key
	 *
	 * @return byte[] key
	 * @throws Exception
	 */
	public static byte[] initSecretKey() {
		// return KeyGenerator object
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}
		// AES key length is 128
		kg.init(128);

		// generate a key
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	public static Key toKey(byte[] key) {

		return new SecretKeySpec(key, KEY_ALGORITHM);
	}

	public static byte[] encrypt(byte[] data, Key key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
		// recover the key
		Key k = toKey(key);
		return encrypt(data, k, cipherAlgorithm);
	}

	public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {

		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static byte[] decrypt(byte[] data, Key key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {

		Key k = toKey(key);
		return decrypt(data, k, cipherAlgorithm);
	}

	public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {

		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.DECRYPT_MODE, key);

		return cipher.doFinal(data);
	}

	private static String showByteArray(byte[] data) {
		if (null == data) {
			return null;
		}
		StringBuilder sb = new StringBuilder("{");
		for (byte b : data) {
			sb.append(b).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}

	/*
	 * public static void main(String[] args) throws Exception { byte[] key =
	 * initSecretKey(); System.out.println("keyï¼š" + showByteArray(key));
	 * 
	 * Key k = toKey(key);
	 * 
	 * String data = "AESæ•°æ�®"; System.out.println("åŠ å¯†å‰�æ•°æ�®: string:"
	 * + data); System.out.println("åŠ å¯†å‰�æ•°æ�®: byte[]:" +
	 * showByteArray(data.getBytes())); System.out.println(); byte[] encryptData
	 * = encrypt(data.getBytes(), k); System.out.println(
	 * "åŠ å¯†å�Žæ•°æ�®: byte[]:" + showByteArray(encryptData));
	 * System.out.println("åŠ å¯†å�Žæ•°æ�®: hexStr:" +
	 * Hex.encodeHexStr(encryptData)); System.out.println(); byte[] decryptData
	 * = decrypt(encryptData, k); System.out.println("è§£å¯†å�Žæ•°æ�®: byte[]:"
	 * + showByteArray(decryptData)); System.out.println(
	 * "è§£å¯†å�Žæ•°æ�®: string:" + new String(decryptData));
	 * 
	 * }
	 */
}
