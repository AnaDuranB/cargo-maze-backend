package com.cargomaze.cargo_maze.config;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class Encryption {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "clave-super-secreta";
    private static final String IV = "1234567890123456";

    private static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(key.getBytes());
        byte[] finalKey = new byte[16];  // Tomamos solo los primeros 16 bytes
        System.arraycopy(hashedKey, 0, finalKey, 0, 16);
        return new SecretKeySpec(finalKey, "AES");
    }

    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = generateKey(SECRET_KEY);  // Generamos una clave válida
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = generateKey(SECRET_KEY);  // Generamos una clave válida
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decodedData));
    }
}
