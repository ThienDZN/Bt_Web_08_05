package vn.iotstar.util;

import java.security.SecureRandom;

public final class OtpGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpGenerator() {
    }

    public static String generate6Digits() {
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }
}
