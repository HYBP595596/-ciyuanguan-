package com.dimension.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加盐哈希（PBKDF2-HmacSHA256）。
 * 存储格式：pbkdf2$<迭代次数>$<saltBase64>$<hashBase64>
 *
 * 兼容历史明文密码：matches() 遇到旧格式会按明文比较，
 * 登录成功后可调用 isLegacy() 判断并就地升级为哈希，避免直接锁死老账号。
 */
public class PasswordUtil {

    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    public static String hash(String raw) {
        if (raw == null) raw = "";
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] key = pbkdf2(raw, salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(key);
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        if (!stored.startsWith(PREFIX + "$")) {
            // 历史明文密码
            return stored.equals(raw);
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(raw, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    /** 是否是尚未哈希的旧密码 */
    public static boolean isLegacy(String stored) {
        return stored != null && !stored.startsWith(PREFIX + "$");
    }

    private static byte[] pbkdf2(String raw, byte[] salt, int iterations) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, iterations, KEY_BITS);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 不可用", e);
        }
    }
}
