package vn.iotstar.controller.admin;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.config.SessionConstants;
import vn.iotstar.entity.UserAccount;
import vn.iotstar.service.IUserAdminService;
import vn.iotstar.service.impl.UserAdminServiceImpl;
import vn.iotstar.validation.ValidationErrors;
import vn.iotstar.validation.ValidationUtils;

public class UserController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IUserAdminService userAdminService = new UserAdminServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/admin/users".equals(servletPath)) {
            showList(req, resp);
            return;
        }
        if ("/admin/user/add".equals(servletPath)) {
            UserAccount user = new UserAccount();
            user.setRoleName("USER");
            user.setEnabled(true);
            user.setStatus(1);
            showForm(req, resp, user, "Create User", req.getContextPath() + "/admin/user/insert");
            return;
        }
        if ("/admin/user/edit".equals(servletPath)) {
            showEdit(req, resp);
            return;
        }
        if ("/admin/user/delete".equals(servletPath)) {
            deleteUser(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/admin/user/insert".equals(servletPath)) {
            insertUser(req, resp);
            return;
        }
        if ("/admin/user/update".equals(servletPath)) {
            updateUser(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ValidationErrors errors = new ValidationErrors();
        String keyword = ValidationUtils.trimToNull(req.getParameter("keyword"));
        if (ValidationUtils.exceedsLength(keyword, 100)) {
            errors.add("keyword", "Search keyword must not exceed 100 characters.");
        }

        List<UserAccount> users = errors.hasErrors()
                ? userAdminService.findAll()
                : userAdminService.search(keyword);
        req.setAttribute("errors", errors.asMap());
        req.setAttribute("users", users);
        req.setAttribute("keyword", keyword == null ? "" : keyword);
        req.getRequestDispatcher("/views/admin/user-list.jsp").include(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = parseId(req.getParameter("id"));
        UserAccount user = userAdminService.findById(userId);
        if (user == null) {
            redirect(resp, req.getContextPath() + "/admin/users", "User not found.");
            return;
        }
        showForm(req, resp, user, "Update User", req.getContextPath() + "/admin/user/update");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, UserAccount user,
                          String formTitle, String formAction) throws ServletException, IOException {
        req.setAttribute("userAccount", user);
        req.setAttribute("formTitle", formTitle);
        req.setAttribute("formAction", formAction);
        req.getRequestDispatcher("/views/admin/user-form.jsp").include(req, resp);
    }

    private void insertUser(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserAccount user = new UserAccount();
        Map<String, String> formData = new LinkedHashMap<>();
        ValidationErrors errors = validateUserRequest(req, user, formData, true, null);
        bindFormState(req, user, formData, errors);
        if (errors.hasErrors()) {
            showForm(req, resp, user, "Create User", req.getContextPath() + "/admin/user/insert");
            return;
        }

        try {
            userAdminService.insert(user, req.getParameter("password"));
            redirect(resp, req.getContextPath() + "/admin/users", "User created successfully.");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, user, "Create User", req.getContextPath() + "/admin/user/insert");
        }
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = parseId(req.getParameter("userId"));
        UserAccount existing = userAdminService.findById(userId);
        if (existing == null) {
            redirect(resp, req.getContextPath() + "/admin/users", "User not found.");
            return;
        }

        Map<String, String> formData = new LinkedHashMap<>();
        ValidationErrors errors = validateUserRequest(req, existing, formData, false, existing.getImages());
        bindFormState(req, existing, formData, errors);
        if (errors.hasErrors()) {
            showForm(req, resp, existing, "Update User", req.getContextPath() + "/admin/user/update");
            return;
        }

        try {
            UserAccount updatedUser = userAdminService.update(existing, req.getParameter("password"));
            refreshCurrentUserSession(req.getSession(false), updatedUser);
            redirect(resp, req.getContextPath() + "/admin/users", "User updated successfully.");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, existing, "Update User", req.getContextPath() + "/admin/user/update");
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = parseId(req.getParameter("id"));
        HttpSession session = req.getSession(false);
        UserAccount currentUser = session == null ? null : (UserAccount) session.getAttribute(SessionConstants.CURRENT_USER);
        try {
            userAdminService.delete(userId);
            if (currentUser != null && currentUser.getUserId().equals(userId)) {
                session.invalidate();
                redirect(resp, req.getContextPath() + "/login", "Your account was deleted.");
                return;
            }
            redirect(resp, req.getContextPath() + "/admin/users", "User deleted successfully.");
        } catch (Exception e) {
            redirect(resp, req.getContextPath() + "/admin/users", e.getMessage());
        }
    }

    private ValidationErrors validateUserRequest(HttpServletRequest req, UserAccount user,
                                                 Map<String, String> formData,
                                                 boolean passwordRequired, String previousImage) {
        ValidationErrors errors = new ValidationErrors();

        String fullName = ValidationUtils.trimToNull(req.getParameter("fullName"));
        formData.put("fullName", ValidationUtils.emptyIfNull(fullName));
        if (fullName == null) {
            errors.add("fullName", "Please enter the full name.");
        } else if (ValidationUtils.exceedsLength(fullName, 120)) {
            errors.add("fullName", "Full name must not exceed 120 characters.");
        } else {
            user.setFullName(fullName);
        }

        String username = ValidationUtils.trimToNull(req.getParameter("username"));
        formData.put("username", ValidationUtils.emptyIfNull(username));
        if (!ValidationUtils.isValidUsername(username)) {
            errors.add("username", "Username must be 3-50 characters and use letters, digits, dot, underscore, or dash.");
        } else {
            user.setUsername(username);
        }

        String email = ValidationUtils.normalizeEmail(req.getParameter("email"));
        formData.put("email", ValidationUtils.emptyIfNull(email));
        if (!ValidationUtils.isValidEmail(email) || ValidationUtils.exceedsLength(email, 120)) {
            errors.add("email", "Please enter a valid email address.");
        } else {
            user.setEmail(email);
        }

        String phone = ValidationUtils.trimToNull(req.getParameter("phone"));
        formData.put("phone", ValidationUtils.emptyIfNull(phone));
        if (ValidationUtils.exceedsLength(phone, 20)) {
            errors.add("phone", "Phone number must not exceed 20 characters.");
        } else if (!ValidationUtils.isValidPhone(phone)) {
            errors.add("phone", "Phone number may only contain digits, spaces, plus, dash, or parentheses.");
        } else {
            user.setPhone(phone);
        }

        String imageValue = ValidationUtils.trimToNull(req.getParameter("images"));
        formData.put("images", ValidationUtils.emptyIfNull(imageValue));
        if (ValidationUtils.exceedsLength(imageValue, 500)) {
            errors.add("images", "Image URL must not exceed 500 characters.");
        } else if (imageValue != null && !isAcceptedImageReference(imageValue, previousImage)) {
            errors.add("images", "Image URL must start with http:// or https://.");
        } else {
            user.setImages(imageValue == null ? ValidationUtils.trimToNull(previousImage) : imageValue);
        }

        String roleName = ValidationUtils.normalizeRoleName(req.getParameter("roleName"));
        formData.put("roleName", ValidationUtils.emptyIfNull(roleName));
        if (!ValidationUtils.isValidRoleName(roleName)) {
            errors.add("roleName", "Please choose a valid role.");
        } else {
            user.setRoleName(roleName);
        }

        String enabledValue = ValidationUtils.trimToNull(req.getParameter("enabled"));
        formData.put("enabled", ValidationUtils.emptyIfNull(enabledValue));
        if (!ValidationUtils.isStatusValue(enabledValue)) {
            errors.add("enabled", "Please choose a valid enabled state.");
        } else {
            user.setEnabled("1".equals(enabledValue));
        }

        String statusValue = ValidationUtils.trimToNull(req.getParameter("status"));
        formData.put("status", ValidationUtils.emptyIfNull(statusValue));
        if (!ValidationUtils.isStatusValue(statusValue)) {
            errors.add("status", "Please choose a valid status.");
        } else {
            user.setStatus("1".equals(statusValue) ? 1 : 0);
        }

        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        if (passwordRequired && ValidationUtils.trimToNull(password) == null) {
            errors.add("password", "Please enter a password with at least 6 characters.");
        } else if (ValidationUtils.trimToNull(password) != null && password.trim().length() < 6) {
            errors.add("password", "Password must contain at least 6 characters.");
        }
        if (ValidationUtils.trimToNull(password) != null
                && !password.equals(confirmPassword == null ? "" : confirmPassword)) {
            errors.add("confirmPassword", "Password confirmation does not match.");
        }

        return errors;
    }

    private void bindFormState(HttpServletRequest req, UserAccount user,
                               Map<String, String> formData, ValidationErrors errors) {
        req.setAttribute("userAccount", user);
        req.setAttribute("formData", formData);
        req.setAttribute("errors", errors.asMap());
    }

    private void refreshCurrentUserSession(HttpSession session, UserAccount updatedUser) {
        if (session == null || updatedUser == null) {
            return;
        }
        UserAccount currentUser = (UserAccount) session.getAttribute(SessionConstants.CURRENT_USER);
        if (currentUser != null && currentUser.getUserId().equals(updatedUser.getUserId())) {
            session.setAttribute(SessionConstants.CURRENT_USER, updatedUser);
        }
    }

    private Long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return -1L;
        }
    }

    private boolean isAcceptedImageReference(String value, String previousImage) {
        return ValidationUtils.isValidHttpUrl(value)
                || (isLocalImage(previousImage) && value.equals(ValidationUtils.trimToNull(previousImage)));
    }

    private boolean isLocalImage(String value) {
        return value != null
                && !value.isBlank()
                && !value.startsWith("http://")
                && !value.startsWith("https://");
    }

    private void redirect(HttpServletResponse resp, String baseUrl, String message) throws IOException {
        resp.sendRedirect(baseUrl + "?message=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
