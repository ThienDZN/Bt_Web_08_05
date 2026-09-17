(() => {
    const endpoint = document.body.dataset.graphqlEndpoint;
    const imageEndpoint = document.body.dataset.imageEndpoint;
    const noImage = document.body.dataset.noImage;
    const detailEndpoint = document.body.dataset.detailEndpoint;
    const message = document.getElementById("graphql-message");
    const priceProducts = document.getElementById("price-products");
    const categoryFilter = document.getElementById("category-filter");
    const categoryProducts = document.getElementById("category-products");

    async function queryCatalog(query, variables = {}) {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify({ query, variables })
        });
        const payload = await response.json();
        if (!response.ok || payload.errors) {
            const error = payload.errors && payload.errors[0];
            throw new Error(error ? error.message : "The catalog could not be loaded.");
        }
        return payload.data;
    }

    function clear(element) {
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    function showEmpty(element, text) {
        clear(element);
        const empty = document.createElement("div");
        empty.className = "empty-box";
        empty.textContent = text;
        element.appendChild(empty);
    }

    function resolveImage(image) {
        if (!image) {
            return noImage;
        }
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        return imageEndpoint + encodeURIComponent(image);
    }

    function createProductCard(product) {
        const card = document.createElement("article");
        card.className = "item-card";

        const image = document.createElement("img");
        image.src = resolveImage(product.image);
        image.alt = product.productName || "Catalog image";
        image.addEventListener("error", () => {
            image.src = noImage;
        }, { once: true });
        card.appendChild(image);

        const category = document.createElement("div");
        category.className = "meta-line";
        category.textContent = `Category: ${product.category.categoryName} | Track ${product.quantity} of 12`;
        card.appendChild(category);

        const title = document.createElement("h3");
        title.textContent = product.productName;
        card.appendChild(title);

        const description = document.createElement("p");
        description.textContent = product.description || "No description is available.";
        card.appendChild(description);

        const price = document.createElement("div");
        price.className = "price-line";
        price.textContent = `Views: ${Number(product.price).toLocaleString()} on YouTube`;
        card.appendChild(price);

        const actions = document.createElement("div");
        actions.className = "action-row";
        const detail = document.createElement("a");
        detail.className = "btn btn-primary";
        detail.href = detailEndpoint + encodeURIComponent(product.productId);
        detail.textContent = "View Detail";
        actions.appendChild(detail);
        card.appendChild(actions);
        return card;
    }

    function renderProducts(element, products, emptyMessage) {
        if (!products || products.length === 0) {
            showEmpty(element, emptyMessage);
            return;
        }
        clear(element);
        const fragment = document.createDocumentFragment();
        products.forEach((product) => fragment.appendChild(createProductCard(product)));
        element.appendChild(fragment);
    }

    function renderCategories(categories) {
        categories.forEach((category) => {
            const option = document.createElement("option");
            option.value = category.categoryId;
            option.textContent = category.categoryName;
            categoryFilter.appendChild(option);
        });
    }

    async function loadCategoryProducts() {
        const categoryId = categoryFilter.value;
        if (!categoryId) {
            showEmpty(categoryProducts, "Choose a category to view its track entries.");
            return;
        }
        showEmpty(categoryProducts, "Loading category entries…");
        try {
            const data = await queryCatalog(`
                query ProductsByCategory($categoryId: ID!) {
                    productsByCategory(categoryId: $categoryId) {
                        productId productName description price quantity image
                        category { categoryName }
                    }
                }`, { categoryId });
            renderProducts(categoryProducts, data.productsByCategory,
                    "This category has no track entries yet.");
        } catch (error) {
            showEmpty(categoryProducts, error.message);
        }
    }

    async function loadHomeCatalog() {
        try {
            const data = await queryCatalog(`
                query HomeCatalog {
                    productsByPrice {
                        productId productName description price quantity image
                        category { categoryName }
                    }
                    categories(page: 1, size: 50) {
                        items { categoryId categoryName }
                    }
                }`);
            renderProducts(priceProducts, data.productsByPrice,
                    "There are no track entries in the system yet.");
            renderCategories(data.categories.items);
            message.textContent = "Products are ordered by price (low to high).";
        } catch (error) {
            showEmpty(priceProducts, error.message);
            message.textContent = "GraphQL catalog loading failed.";
        }
    }

    categoryFilter.addEventListener("change", loadCategoryProducts);
    loadHomeCatalog();
})();
