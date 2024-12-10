package com.cargomaze.cargo_maze.config;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class Encryption {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "clave-super-secreta";

    private static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(key.getBytes());
        byte[] finalKey = new byte[16]; // Usamos los primeros 16 bytes
        System.arraycopy(hashedKey, 0, finalKey, 0, 16);
        System.out.println("Clave final en Hexadecimal:");
        for (byte b : finalKey) {
            System.out.printf("%02x", b);
        }
        System.out.println();

        System.out.println("Clave final en Base64:");
        System.out.println(Base64.getEncoder().encodeToString(finalKey));
        return new SecretKeySpec(finalKey, "AES");
    }

    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = generateKey(SECRET_KEY);

        // Generar un IV aleatorio
        byte[] ivBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Adjuntar el IV al inicio del texto cifrado
        byte[] encryptedWithIv = new byte[ivBytes.length + encryptedData.length];
        System.arraycopy(ivBytes, 0, encryptedWithIv, 0, ivBytes.length);
        System.arraycopy(encryptedData, 0, encryptedWithIv, ivBytes.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = generateKey(SECRET_KEY);

        // Decodificar la entrada base64
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);

        // Extraer el IV del texto cifrado
        byte[] ivBytes = new byte[16];
        System.arraycopy(encryptedWithIv, 0, ivBytes, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Extraer los datos cifrados
        byte[] encryptedBytes = new byte[encryptedWithIv.length - 16];
        System.arraycopy(encryptedWithIv, 16, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData);
    }
}
