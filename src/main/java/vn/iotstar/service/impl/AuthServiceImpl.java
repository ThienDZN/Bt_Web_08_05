package vn.iotstar.service.impl;

import java.time.LocalDateTime;

import vn.iotstar.config.OtpPurpose;
import vn.iotstar.config.PasswordUtils;
import vn.iotstar.dao.IOtpVerificationDao;
import vn.iotstar.dao.IUserAccountDao;
import vn.iotstar.dao.impl.OtpVerificationDao;
import vn.iotstar.dao.impl.UserAccountDao;
import vn.iotstar.entity.OtpVerification;
import vn.iotstar.entity.UserAccount;
import vn.iotstar.service.IAuthService;
import vn.iotstar.util.OtpGenerator;

public class AuthServiceImpl implements IAuthService {
    private final IUserAccountDao userAccountDao = new UserAccountDao();
    private final IOtpVerificationDao otpVerificationDao = new OtpVerificationDao();
    private final MailService mailService = new MailService();

    @Override
    public String register(String fullName, String username, String email, String password, String confirmPassword) {
        validateRegisterInput(fullName, username, email, password, confirmPassword);
        ensureUniqueUser(username, email);

        UserAccount user = new UserAccount();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordUtils.encode(password));
        user.setRoleName("USER");
        user.setEnabled(false);
        user.setStatus(1);
        userAccountDao.insert(user);

        return createAndSendOtp(user, OtpPurpose.REGISTER);
    }

    @Override
    public UserAccount login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Please enter both username/email and password.");
        }
        UserAccount user = userAccountDao.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase());
        if (user == null || !PasswordUtils.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("The account or password is incorrect.");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("This account has not been activated by OTP yet.");
        }
        if (user.getStatus() != 1) {
            throw new IllegalArgumentException("This account is currently disabled.");
        }
        return user;
    }

    @Override
    public void verifyRegistrationOtp(String email, String otp) {
        OtpVerification otpVerification = validateOtp(email, otp, OtpPurpose.REGISTER);
        UserAccount user = otpVerification.getUser();
        if (user == null) {
            user = userAccountDao.findByEmail(email.trim().toLowerCase());
        }
        if (user == null) {
            throw new IllegalArgumentException("Unable to find the account waiting for activation.");
        }
        user.setEnabled(true);
        userAccountDao.update(user);
        otpVerification.setUsed(true);
        otpVerificationDao.update(otpVerification);
    }

    @Override
    public String resendRegistrationOtp(String email) {
        UserAccount user = userAccountDao.findByEmail(normalizeEmail(email));
        if (user == null) {
            throw new IllegalArgumentException("This email has not been registered.");
        }
        if (user.isEnabled()) {
            throw new IllegalArgumentException("This account is already activated.");
        }
        return createAndSendOtp(user, OtpPurpose.REGISTER);
    }

    @Override
    public String sendResetPasswordOtp(String email) {
        UserAccount user = userAccountDao.findByEmail(normalizeEmail(email));
        if (user == null) {
            throw new IllegalArgumentException("No account was found with this email address.");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("This account has not been activated yet.");
        }
        return createAndSendOtp(user, OtpPurpose.RESET_PASSWORD);
    }

    @Override
    public void verifyResetPasswordOtp(String email, String otp) {
        OtpVerification otpVerification = validateOtp(email, otp, OtpPurpose.RESET_PASSWORD);
        otpVerification.setUsed(true);
        otpVerificationDao.update(otpVerification);
    }

    @Override
    public void resetPassword(String email, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("The new password must contain at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
        UserAccount user = userAccountDao.findByEmail(normalizeEmail(email));
        if (user == null) {
            throw new IllegalArgumentException("Unable to find the requested account.");
        }
        user.setPasswordHash(PasswordUtils.encode(newPassword));
        userAccountDao.update(user);
    }

    private void validateRegisterInput(String fullName, String username, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.isBlank() || username == null || username.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Please fill in full name, username, and email.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private void ensureUniqueUser(String username, String email) {
        if (userAccountDao.findByUsername(username.trim()) != null) {
            throw new IllegalArgumentException("This username already exists.");
        }
        if (userAccountDao.findByEmail(email.trim().toLowerCase()) != null) {
            throw new IllegalArgumentException("This email address is already in use.");
        }
    }

    private String createAndSendOtp(UserAccount user, String purpose) {
        otpVerificationDao.markAllUnusedAsUsed(user.getEmail(), purpose);

        String otp = OtpGenerator.generate6Digits();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setUser(user);
        otpVerification.setEmail(user.getEmail());
        otpVerification.setOtpCode(otp);
        otpVerification.setPurpose(purpose);
        otpVerification.setExpiryAt(LocalDateTime.now().plusMinutes(5));
        otpVerification.setUsed(false);
        otpVerificationDao.insert(otpVerification);

        return mailService.sendOtp(user.getEmail(), user.getFullName(), otp, purpose);
    }

    private OtpVerification validateOtp(String email, String otp, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Please enter the OTP code.");
        }
        OtpVerification otpVerification = otpVerificationDao.findLatest(normalizedEmail, purpose);
        if (otpVerification == null) {
            throw new IllegalArgumentException("No matching OTP could be found.");
        }
        if (otpVerification.isUsed()) {
            throw new IllegalArgumentException("This OTP has already been used.");
        }
        if (otpVerification.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This OTP has expired.");
        }
        if (!otpVerification.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("The OTP code is incorrect.");
        }
        return otpVerification;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty.");
        }
        return email.trim().toLowerCase();
    }
}
