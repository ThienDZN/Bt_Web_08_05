<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Management</title>
    <link rel="stylesheet" href="<c:url value='/assets/app-theme.css'/>">
</head>
<body class="theme-music">
<div class="theme-shell">
    <div class="theme-nav">
        <div class="theme-brand">
            User Management
            <small>Admin CRUD for the users table with search by name, username, email, or role</small>
        </div>
        <div class="theme-nav-links">
            <a class="btn btn-secondary" href="<c:url value='/home'/>">Home</a>
            <a class="btn btn-secondary" href="<c:url value='/admin/products'/>">Catalog</a>
            <a class="btn btn-secondary" href="<c:url value='/admin/categories'/>">Categories</a>
            <a class="btn btn-secondary" href="<c:url value='/profile'/>">Profile</a>
            <a class="btn btn-primary" href="<c:url value='/admin/user/add'/>">Add User</a>
            <a class="btn btn-secondary" href="<c:url value='/logout'/>">Logout</a>
        </div>
    </div>

    <c:if test="${not empty param.message}">
        <div class="message-box"><c:out value="${param.message}"/></div>
    </c:if>

    <section class="panel section-panel">
        <div class="section-head">
            <div>
                <h1>User table</h1>
                <p>Total users: ${fn:length(users)}</p>
            </div>
        </div>

        <form action="<c:url value='/admin/users'/>" method="get" class="search-row">
            <input class="form-input${not empty errors.keyword ? ' is-invalid' : ''}" type="text" name="keyword"
                   value="<c:out value='${keyword}'/>" placeholder="Search by full name, username, email, or role">
            <button class="btn btn-primary" type="submit">Search</button>
            <a class="btn btn-secondary" href="<c:url value='/admin/users'/>">Reset</a>
            <c:if test="${not empty errors.keyword}">
                <div class="invalid-feedback d-block">${errors.keyword}</div>
            </c:if>
        </form>

        <c:choose>
            <c:when test="${empty users}">
                <div class="empty-box">There are no user accounts matching the current filter.</div>
            </c:when>
            <c:otherwise>
                <table class="data-table">
                    <tr>
                        <th>ID</th>
                        <th>Avatar</th>
                        <th>Full Name</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Enabled</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                    <c:forEach items="${users}" var="user">
                        <tr>
                            <td>${user.userId}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty user.images}">
                                        <img class="table-thumb table-thumb-square" src="<c:url value='/assets/no-image.svg'/>" alt="No image">
                                    </c:when>
                                    <c:when test="${fn:startsWith(user.images, 'http://') or fn:startsWith(user.images, 'https://')}">
                                        <img class="table-thumb table-thumb-square" src="${fn:escapeXml(user.images)}" alt="${fn:escapeXml(user.fullName)}">
                                    </c:when>
                                    <c:otherwise>
                                        <img class="table-thumb table-thumb-square" src="<c:url value='/image?fname=${user.images}'/>" alt="${fn:escapeXml(user.fullName)}">
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${user.fullName}"/></td>
                            <td><c:out value="${user.username}"/></td>
                            <td><c:out value="${user.email}"/></td>
                            <td><c:out value="${user.roleName}"/></td>
                            <td><c:choose><c:when test="${user.enabled}">Enabled</c:when><c:otherwise>Disabled</c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${user.status == 1}">Active</c:when><c:otherwise>Locked</c:otherwise></c:choose></td>
                            <td><c:out value="${fn:replace(user.createdAt, 'T', ' ')}"/></td>
                            <td>
                                <div class="action-row">
                                    <a class="btn btn-secondary" href="<c:url value='/admin/user/edit?id=${user.userId}'/>">Edit</a>
                                    <a class="btn btn-danger" href="<c:url value='/admin/user/delete?id=${user.userId}'/>"
                                       onclick="return confirm('Delete this user account?');">Delete</a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </section>
</div>
</body>
</html>
