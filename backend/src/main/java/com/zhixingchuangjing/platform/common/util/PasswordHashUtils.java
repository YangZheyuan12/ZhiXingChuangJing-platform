package com.zhixingchuangjing.platform.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PasswordHashUtils {

    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");

    private PasswordHashUtils() {
    }

    public static boolean isSha256Hex(String value) {
        return value != null && SHA256_HEX_PATTERN.matcher(value).matches();
    }

    public static String normalizeTransportPassword(String value) {
        if (value == null) {
            return "";
        }
        if (isSha256Hex(value)) {
            return value.toLowerCase(Locale.ROOT);
        }
        return sha256Hex(value);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte current : hash) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
