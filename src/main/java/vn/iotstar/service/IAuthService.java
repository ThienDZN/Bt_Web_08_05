package vn.iotstar.service;

import vn.iotstar.entity.UserAccount;

public interface IAuthService {
    String register(String fullName, String username, String email, String password, String confirmPassword);
    UserAccount login(String usernameOrEmail, String password);
    void verifyRegistrationOtp(String email, String otp);
    String resendRegistrationOtp(String email);
    String sendResetPasswordOtp(String email);
    void verifyResetPasswordOtp(String email, String otp);
    void resetPassword(String email, String newPassword, String confirmPassword);
}
