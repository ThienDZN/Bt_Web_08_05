<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thang - Cai Thu Hai Catalog</title>
    <link rel="stylesheet" href="<c:url value='/assets/app-theme.css'/>">
</head>
<body class="theme-music" data-graphql-endpoint="<c:url value='/graphql'/>"
      data-image-endpoint="<c:url value='/image?fname='/>"
      data-no-image="<c:url value='/assets/no-image.svg'/>"
      data-detail-endpoint="<c:url value='/product/detail?id='/>">
<div class="theme-shell">
    <div class="theme-nav">
        <div class="theme-brand">
            Thang - Cai Thu Hai Catalog
            <small>OTP authentication, multipart upload, category-product relation, and public catalog pages</small>
        </div>
        <div class="theme-nav-links">
            <a class="btn btn-secondary" href="<c:url value='/home'/>">Home</a>
            <a class="btn btn-secondary" href="<c:url value='/product'/>">Catalog</a>
            <c:if test="${sessionScope.currentUser != null and sessionScope.currentUser.roleName == 'ADMIN'}">
                <a class="btn btn-secondary" href="<c:url value='/admin/products'/>">Admin</a>
            </c:if>
            <c:choose>
                <c:when test="${sessionScope.currentUser != null}">
                    <a class="btn btn-secondary" href="<c:url value='/profile'/>">Profile</a>
                    <span class="inline-note">Hello, ${sessionScope.currentUser.fullName}</span>
                    <a class="btn btn-primary" href="<c:url value='/logout'/>">Logout</a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-secondary" href="<c:url value='/login'/>">Login</a>
                    <a class="btn btn-primary" href="<c:url value='/register'/>">Register</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <c:if test="${not empty param.message}">
        <div class="message-box">${param.message}</div>
    </c:if>

    <section class="hero-grid">
        <div class="panel hero-copy">
            <div class="eyebrow">Featured Playlist</div>
            <h1 class="hero-title">Thang's Cai Thu Hai, reimagined as the demo catalog</h1>
            <p class="hero-lead">The assignment logic now keeps category-product CRUD, OTP account activation, upload and catalog pages, while this home page loads its catalog through a GraphQL API and AJAX. The visible sample data follows the requested track list from <strong>Cai Thu Hai</strong>.</p>
            <div class="cta-row">
                <a class="btn btn-primary" href="<c:url value='/product'/>">Open the Catalog</a>
                <c:if test="${sessionScope.currentUser != null and sessionScope.currentUser.roleName == 'ADMIN'}">
                    <a class="btn btn-secondary" href="<c:url value='/admin/product/add'/>">Create Entry</a>
                </c:if>
            </div>
        </div>
        <div class="panel stats-card">
            <div class="badge">GraphQL + AJAX</div>
            <div>
                <div class="big-number">API</div>
                <div class="inline-note">Products below are queried in price order without reloading the page.</div>
            </div>
            <div class="soft-panel" style="padding:22px;">
                <div>Authentication</div>
                <div class="big-number">OTP</div>
                <div class="inline-note">Registration activation and password reset are both wired through OTP verification pages.</div>
            </div>
        </div>
    </section>

    <section class="section-panel panel">
        <div class="section-head">
            <div>
                <h2>All track entries by price</h2>
                <p>This grid is loaded from <code>/graphql</code> and sorted from the lowest price to the highest.</p>
            </div>
            <a class="btn btn-primary" href="<c:url value='/product'/>">View the full catalog</a>
        </div>
        <div id="graphql-message" class="inline-note" role="status">Loading products…</div>
        <div id="price-products" class="card-grid" aria-live="polite"></div>
    </section>

    <section class="section-panel panel">
        <div class="section-head">
            <div>
                <h2>Browse one category</h2>
                <p>Choose a category to load just its products through GraphQL.</p>
            </div>
        </div>
        <div class="search-row">
            <label for="category-filter" class="inline-note">Category</label>
            <select id="category-filter" class="form-select" aria-label="Choose a category">
                <option value="">Choose a category</option>
            </select>
        </div>
        <div id="category-products" class="card-grid" aria-live="polite">
            <div class="empty-box">Choose a category to view its track entries.</div>
        </div>
    </section>
</div>
<script src="<c:url value='/assets/graphql-catalog.js'/>" defer></script>
</body>
</html>
