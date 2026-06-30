package fr.alex96x2.admin.api.util;

import fr.alex96x2.admin.api.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class IpCryptoService {

    private final AppProperties properties;

    public IpCryptoService(AppProperties properties) {
        this.properties = properties;
    }

    public String decrypt(byte[] data) {
        if (data == null) return null;
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(properties.ip().encryptionKey().getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[12];
            System.arraycopy(data, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[data.length - iv.length];
            System.arraycopy(data, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
