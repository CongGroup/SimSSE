package test;

import java.security.Key;

import tool.AESCoder;
import tool.BaseTool;
import tool.MathTool;
import tool.PRF;

/**
 * Created by HarryC on 8/5/14.
 *
 * This class is used to test atom function.
 */
public class functionTest {

	public static void main(String args[]) {

		String s = PRF.SHA256("http://www.convertstring.com/zh_CN/Hash/SHA256", 256);
		System.out.println(s);
		String ss = PRF.SHA256("http://www.convertstring.com/zh_CN/Hash/SHA2560", 256);
		System.out.println(ss);
		long s2 = PRF.SHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA256");
		System.out.println(s2 % 11119);
		long ss2 = PRF.SHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA2560");
		System.out.println(ss2 % 11119);
		long sss2 = PRF.SHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA2561");
		System.out.println(sss2 % 11119);

		int prime = MathTool.getUpperPrimeNumber(32);

		System.out.println("Prime number is " + prime);

		long _s2 = PRF.HMACSHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA256", "harry");
		System.out.println(_s2);
		long _ss2 = PRF.HMACSHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA256", "harr");
		System.out.println(_ss2 % 11119);
		long _sss2 = PRF.HMACSHA256ToUnsignedInt("http://www.convertstring.com/zh_CN/Hash/SHA256", "har");
		System.out.println(_sss2 % 11119);

		byte[] b = PRF.HMACSHA256("http://www.convertstring.com/zh_CN/Hash/SHA256", "harry");
		System.out.println(b.toString());
		System.out.println(BaseTool.bytesToUnsignedInt(b));

		System.out.println(BaseTool.flod256Bytes(b));

		long cc = encryptValue("cityu", 2753929794L, 1, 21419);

		System.out.println(cc);

		int id = decryptValue("cityu", 2753929794L, 1, cc);

		System.out.println(id);

		////////////////////////// 2014-6-20

		//byte[] key = AESCoder.initSecretKey();

		Key k = AESCoder.toKey(PRF.SHA256("cityu", 64).getBytes());

		Long data = 4294967296L;
		System.out.println("before encryption: string:" + data);
		System.out.println();
		byte[] encryptData = new byte[0];
		try {
			encryptData = AESCoder.encrypt(data.toString().getBytes(), k);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println();
		byte[] decryptData = new byte[0];
		try {
			decryptData = AESCoder.decrypt(encryptData, k);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("after encryption: long:" + Long.parseLong(new String(decryptData)));
	}

	public static long encryptValue(String key2, long lshValue, int position, int id) {

		long k2Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);

		System.out.println("k2Vj = " + k2Vj);

		long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(position), String.valueOf(k2Vj)));

		System.out.println("r = " + r);

		return (long) id ^ r;
	}

	public static int decryptValue(String key2, long lshValue, int position, long c) {

		long k2Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);

		System.out.println("k2Vj = " + k2Vj);

		long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(position), String.valueOf(k2Vj)));

		System.out.println("r = " + r);

		long mid = c ^ r;

		return (int) (mid);
	}
}
