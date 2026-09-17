package vn.iotstar.config;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
