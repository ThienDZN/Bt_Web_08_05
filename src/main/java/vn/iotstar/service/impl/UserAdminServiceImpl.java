package vn.iotstar.service.impl;

import java.util.List;

import vn.iotstar.config.PasswordUtils;
import vn.iotstar.dao.IUserAccountDao;
import vn.iotstar.dao.impl.UserAccountDao;
import vn.iotstar.entity.UserAccount;
import vn.iotstar.service.IUserAdminService;
import vn.iotstar.validation.ValidationUtils;

public class UserAdminServiceImpl implements IUserAdminService {
    private final IUserAccountDao userAccountDao = new UserAccountDao();

    @Override
    public List<UserAccount> findAll() {
        return userAccountDao.findAll();
    }

    @Override
    public List<UserAccount> search(String keyword) {
        String normalizedKeyword = ValidationUtils.trimToNull(keyword);
        return normalizedKeyword == null ? findAll() : userAccountDao.search(normalizedKeyword);
    }

    @Override
    public UserAccount findById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userAccountDao.findById(userId);
    }

    @Override
    public void insert(UserAccount user, String rawPassword) {
        UserAccount normalized = normalize(user);
        validatePassword(rawPassword, true);
        ensureUnique(normalized, null);
        normalized.setPasswordHash(PasswordUtils.encode(rawPassword.trim()));
        userAccountDao.insert(normalized);
    }

    @Override
    public UserAccount update(UserAccount user, String rawPassword) {
        if (user == null || user.getUserId() == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("The user data is not valid.");
        }

        UserAccount existing = userAccountDao.findById(user.getUserId());
        if (existing == null) {
            throw new IllegalArgumentException("The user does not exist.");
        }

        UserAccount normalized = normalize(user);
        normalized.setUserId(existing.getUserId());
        normalized.setCreatedAt(existing.getCreatedAt());
        ensureUnique(normalized, existing.getUserId());

        if (ValidationUtils.trimToNull(rawPassword) == null) {
            normalized.setPasswordHash(existing.getPasswordHash());
        } else {
            validatePassword(rawPassword, false);
            normalized.setPasswordHash(PasswordUtils.encode(rawPassword.trim()));
        }

        userAccountDao.update(normalized);
        return userAccountDao.findById(existing.getUserId());
    }

    @Override
    public void delete(Long userId) throws Exception {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("The user id is not valid.");
        }
        userAccountDao.delete(userId);
    }

    private void ensureUnique(UserAccount user, Long currentUserId) {
        UserAccount usernameMatch = userAccountDao.findByUsername(user.getUsername());
        if (usernameMatch != null && !usernameMatch.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("This username already exists.");
        }

        UserAccount emailMatch = userAccountDao.findByEmail(user.getEmail());
        if (emailMatch != null && !emailMatch.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("This email address is already in use.");
        }
    }

    private void validatePassword(String rawPassword, boolean required) {
        String normalized = ValidationUtils.trimToNull(rawPassword);
        if (normalized == null) {
            if (required) {
                throw new IllegalArgumentException("Password must contain at least 6 characters.");
            }
            return;
        }
        if (normalized.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
    }

    private UserAccount normalize(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("The user data is not valid.");
        }

        String fullName = ValidationUtils.trimToNull(user.getFullName());
        if (fullName == null || ValidationUtils.exceedsLength(fullName, 120)) {
            throw new IllegalArgumentException("Please provide a valid full name.");
        }

        String username = ValidationUtils.trimToNull(user.getUsername());
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be 3-50 characters and use letters, digits, dot, underscore, or dash.");
        }

        String email = ValidationUtils.normalizeEmail(user.getEmail());
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        String phone = ValidationUtils.trimToNull(user.getPhone());
        if (!ValidationUtils.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone number may only contain digits, spaces, plus, dash, or parentheses.");
        }
        if (ValidationUtils.exceedsLength(phone, 20)) {
            throw new IllegalArgumentException("Phone number must not exceed 20 characters.");
        }

        String images = ValidationUtils.trimToNull(user.getImages());
        if (ValidationUtils.exceedsLength(images, 500)) {
            throw new IllegalArgumentException("Image URL must not exceed 500 characters.");
        }

        String roleName = ValidationUtils.normalizeRoleName(user.getRoleName());
        if (!ValidationUtils.isValidRoleName(roleName)) {
            throw new IllegalArgumentException("Please choose a valid role.");
        }

        UserAccount normalized = new UserAccount();
        normalized.setUserId(user.getUserId());
        normalized.setFullName(fullName);
        normalized.setUsername(username);
        normalized.setEmail(email);
        normalized.setPhone(phone);
        normalized.setImages(images);
        normalized.setRoleName(roleName);
        normalized.setEnabled(user.isEnabled());
        normalized.setStatus(user.getStatus() == 1 ? 1 : 0);
        normalized.setCreatedAt(user.getCreatedAt());
        return normalized;
    }
}
