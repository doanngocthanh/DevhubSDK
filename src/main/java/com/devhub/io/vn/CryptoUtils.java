package com.devhub.io.vn;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

public class CryptoUtils {

	private static final String SECRET_KEY = DevhubSDK.scriptID;
	public static String encrypt(String plainText) {
		try {
			SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] encrypted = cipher.doFinal(plainText.getBytes());
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			System.err.println("Lỗi mã hóa: " + e.getMessage());
			return null;
		}
	}

	public static String decrypt(String encryptedText) {
		try {
			SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decoded = Base64.getDecoder().decode(encryptedText);
			byte[] decrypted = cipher.doFinal(decoded);
			return new String(decrypted);
		} catch (Exception e) {
			System.err.println("Lỗi giải mã: " + e.getMessage());
			return null;
		}
	}

	
}
