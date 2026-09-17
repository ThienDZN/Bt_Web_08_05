<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${formTitle}</title>
    <link rel="stylesheet" href="<c:url value='/assets/app-theme.css'/>">
</head>
<body class="theme-music">
<div class="theme-shell">
    <div class="panel form-shell">
        <div class="eyebrow">Admin User Account</div>
        <h1>${formTitle}</h1>
        <p class="inline-note">Create or update user accounts for the admin role management requirement.</p>
        <c:if test="${not empty error}">
            <div class="error-box"><c:out value="${error}"/></div>
        </c:if>

        <form action="${formAction}" method="post">
            <c:if test="${userAccount.userId != null}">
                <input type="hidden" name="userId" value="<c:out value='${userAccount.userId}'/>">
            </c:if>

            <div class="form-group">
                <label>Full Name</label>
                <input class="form-input${not empty errors.fullName ? ' is-invalid' : ''}" type="text" name="fullName"
                       value="<c:out value='${not empty formData.fullName ? formData.fullName : userAccount.fullName}'/>" placeholder="Enter full name">
                <c:if test="${not empty errors.fullName}">
                    <div class="invalid-feedback d-block">${errors.fullName}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Username</label>
                <input class="form-input${not empty errors.username ? ' is-invalid' : ''}" type="text" name="username"
                       value="<c:out value='${not empty formData.username ? formData.username : userAccount.username}'/>" placeholder="Enter username">
                <c:if test="${not empty errors.username}">
                    <div class="invalid-feedback d-block">${errors.username}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Email</label>
                <input class="form-input${not empty errors.email ? ' is-invalid' : ''}" type="email" name="email"
                       value="<c:out value='${not empty formData.email ? formData.email : userAccount.email}'/>" placeholder="Enter email address">
                <c:if test="${not empty errors.email}">
                    <div class="invalid-feedback d-block">${errors.email}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Phone</label>
                <input class="form-input${not empty errors.phone ? ' is-invalid' : ''}" type="text" name="phone"
                       value="<c:out value='${not empty formData.phone ? formData.phone : userAccount.phone}'/>" placeholder="Enter phone number">
                <c:if test="${not empty errors.phone}">
                    <div class="invalid-feedback d-block">${errors.phone}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Avatar URL</label>
                <input class="form-input${not empty errors.images ? ' is-invalid' : ''}" type="text" name="images"
                       value="<c:out value='${not empty formData.images ? formData.images : userAccount.images}'/>" placeholder="https://...">
                <div class="field-hint">Existing local file names are also preserved when you keep the current value.</div>
                <c:if test="${not empty errors.images}">
                    <div class="invalid-feedback d-block">${errors.images}</div>
                </c:if>
            </div>

            <c:if test="${not empty userAccount.images}">
                <c:choose>
                    <c:when test="${fn:startsWith(userAccount.images, 'http://') or fn:startsWith(userAccount.images, 'https://')}">
                        <img class="preview-image preview-image-square" src="${fn:escapeXml(userAccount.images)}" alt="${fn:escapeXml(userAccount.fullName)}">
                    </c:when>
                    <c:otherwise>
                        <img class="preview-image preview-image-square" src="<c:url value='/image?fname=${userAccount.images}'/>" alt="${fn:escapeXml(userAccount.fullName)}">
                    </c:otherwise>
                </c:choose>
            </c:if>

            <div class="form-group">
                <label>Role</label>
                <select class="form-select${not empty errors.roleName ? ' is-invalid' : ''}" name="roleName">
                    <option value="USER" ${not empty formData.roleName ? (formData.roleName == 'USER' ? 'selected' : '') : (userAccount.roleName == 'USER' ? 'selected' : '')}>USER</option>
                    <option value="ADMIN" ${not empty formData.roleName ? (formData.roleName == 'ADMIN' ? 'selected' : '') : (userAccount.roleName == 'ADMIN' ? 'selected' : '')}>ADMIN</option>
                </select>
                <c:if test="${not empty errors.roleName}">
                    <div class="invalid-feedback d-block">${errors.roleName}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Enabled</label>
                <div class="radio-row">
                    <label><input type="radio" name="enabled" value="1" ${not empty formData.enabled ? (formData.enabled == '1' ? 'checked' : '') : (userAccount.enabled or userAccount.userId == null ? 'checked' : '')}> Yes</label>
                    <label><input type="radio" name="enabled" value="0" ${not empty formData.enabled ? (formData.enabled == '0' ? 'checked' : '') : (userAccount.userId != null and !userAccount.enabled ? 'checked' : '')}> No</label>
                </div>
                <c:if test="${not empty errors.enabled}">
                    <div class="invalid-feedback d-block">${errors.enabled}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Status</label>
                <div class="radio-row">
                    <label><input type="radio" name="status" value="1" ${not empty formData.status ? (formData.status == '1' ? 'checked' : '') : (userAccount.status != 0 ? 'checked' : '')}> Active</label>
                    <label><input type="radio" name="status" value="0" ${not empty formData.status ? (formData.status == '0' ? 'checked' : '') : (userAccount.status == 0 ? 'checked' : '')}> Locked</label>
                </div>
                <c:if test="${not empty errors.status}">
                    <div class="invalid-feedback d-block">${errors.status}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Password</label>
                <input class="form-input${not empty errors.password ? ' is-invalid' : ''}" type="password" name="password" placeholder="Enter password">
                <div class="field-hint">
                    <c:choose>
                        <c:when test="${userAccount.userId != null}">Leave blank to keep the current password.</c:when>
                        <c:otherwise>Password must contain at least 6 characters.</c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${not empty errors.password}">
                    <div class="invalid-feedback d-block">${errors.password}</div>
                </c:if>
            </div>

            <div class="form-group">
                <label>Confirm Password</label>
                <input class="form-input${not empty errors.confirmPassword ? ' is-invalid' : ''}" type="password" name="confirmPassword" placeholder="Confirm password">
                <c:if test="${not empty errors.confirmPassword}">
                    <div class="invalid-feedback d-block">${errors.confirmPassword}</div>
                </c:if>
            </div>

            <c:if test="${userAccount.createdAt != null}">
                <div class="form-group">
                    <label>Created At</label>
                    <input class="form-input" type="text" value="${fn:replace(userAccount.createdAt, 'T', ' ')}" readonly>
                </div>
            </c:if>

            <div class="form-actions">
                <button class="btn btn-primary" type="submit">Save User</button>
                <a class="btn btn-secondary" href="<c:url value='/admin/users'/>">Back to Users</a>
                <a class="btn btn-secondary" href="<c:url value='/admin/products'/>">Catalog</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>
