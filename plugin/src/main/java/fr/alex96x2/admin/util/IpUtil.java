package fr.alex96x2.admin.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class IpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IpUtil() {}

    public static String hashIp(String ip, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + ":" + ip).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur hash IP", e);
        }
    }

    public static byte[] encryptIp(String ip, String key) {
        try {
            byte[] keyBytes = deriveKey(key);
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(ip.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur chiffrement IP", e);
        }
    }

    public static String decryptIp(byte[] data, String key) {
        try {
            byte[] keyBytes = deriveKey(key);
            byte[] iv = new byte[12];
            System.arraycopy(data, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[data.length - iv.length];
            System.arraycopy(data, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur déchiffrement IP", e);
        }
    }

    private static byte[] deriveKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(key.getBytes(StandardCharsets.UTF_8));
    }
}
