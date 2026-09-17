package vn.iotstar.controller.graphql;

import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.iotstar.config.SessionConstants;
import vn.iotstar.dto.graphql.CategoryInput;
import vn.iotstar.dto.graphql.CategoryPageView;
import vn.iotstar.dto.graphql.CategoryView;
import vn.iotstar.dto.graphql.ProductInput;
import vn.iotstar.dto.graphql.ProductPageView;
import vn.iotstar.dto.graphql.ProductView;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.UserAccount;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.service.impl.ProductServiceImpl;
import vn.iotstar.validation.ValidationUtils;

@Controller
public class CatalogGraphqlController {
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_PAGE = 10_000;

    private final IProductService productService;
    private final ICategoryService categoryService;

    public CatalogGraphqlController() {
        this(new ProductServiceImpl(), new CategoryServiceImpl());
    }

    CatalogGraphqlController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @QueryMapping
    public List<ProductView> productsByPrice() {
        return productService.findByPriceAscending().stream().map(this::toProductView).toList();
    }

    @QueryMapping
    public List<ProductView> productsByCategory(@Argument("categoryId") Long categoryId) {
        return productService.findByCategoryId(requireCategoryId(categoryId)).stream().map(this::toProductView).toList();
    }

    @QueryMapping
    public ProductPageView products(@Argument String keyword, @Argument Integer page, @Argument Integer size) {
        String normalizedKeyword = normalizeKeyword(keyword, 150);
        PageRequest pageRequest = pageRequest(page, size);
        int totalItems = productService.count(normalizedKeyword);
        return new ProductPageView(
                productService.findPage(normalizedKeyword, pageRequest.page(), pageRequest.size()).stream()
                        .map(this::toProductView).toList(),
                pageRequest.page(), pageRequest.size(), totalItems, totalPages(totalItems, pageRequest.size()));
    }

    @QueryMapping
    public CategoryPageView categories(@Argument String keyword, @Argument Integer page, @Argument Integer size) {
        String normalizedKeyword = normalizeKeyword(keyword, 50);
        PageRequest pageRequest = pageRequest(page, size);
        int totalItems = categoryService.count(normalizedKeyword);
        return new CategoryPageView(
                categoryService.findPage(normalizedKeyword, pageRequest.page(), pageRequest.size()).stream()
                        .map(this::toCategoryView).toList(),
                pageRequest.page(), pageRequest.size(), totalItems, totalPages(totalItems, pageRequest.size()));
    }

    @QueryMapping
    public ProductView product(@Argument Long id) {
        Product product = productService.findById(requireProductId(id));
        return product == null ? null : toProductView(product);
    }

    @QueryMapping
    public CategoryView category(@Argument Long id) {
        Category category = categoryService.findById(requireCategoryId(id));
        return category == null ? null : toCategoryView(category);
    }

    @MutationMapping
    public ProductView createProduct(@Argument ProductInput input) {
        requireAdministrator();
        Product product = new Product();
        applyProductInput(product, input, false);
        try {
            productService.insert(product);
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
        return toProductView(product);
    }

    @MutationMapping
    public ProductView updateProduct(@Argument Long id, @Argument ProductInput input) {
        requireAdministrator();
        Product product = productService.findById(requireProductId(id));
        if (product == null) {
            throw notFound("Product was not found.");
        }
        applyProductInput(product, input, true);
        try {
            productService.update(product);
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
        return toProductView(product);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument Long id) {
        requireAdministrator();
        Long productId = requireProductId(id);
        if (productService.findById(productId) == null) {
            throw notFound("Product was not found.");
        }
        try {
            productService.delete(productId);
            return true;
        } catch (Exception exception) {
            throw badRequest("Product could not be deleted.");
        }
    }

    @MutationMapping
    public CategoryView createCategory(@Argument CategoryInput input) {
        requireAdministrator();
        Category category = new Category();
        applyCategoryInput(category, input, false);
        try {
            categoryService.insert(category);
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
        return toCategoryView(category);
    }

    @MutationMapping
    public CategoryView updateCategory(@Argument Long id, @Argument CategoryInput input) {
        requireAdministrator();
        Category category = categoryService.findById(requireCategoryId(id));
        if (category == null) {
            throw notFound("Category was not found.");
        }
        applyCategoryInput(category, input, true);
        try {
            categoryService.update(category);
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
        return toCategoryView(category);
    }

    @MutationMapping
    public boolean deleteCategory(@Argument Long id) {
        requireAdministrator();
        int categoryId = requireCategoryId(id);
        if (categoryService.findById(categoryId) == null) {
            throw notFound("Category was not found.");
        }
        try {
            categoryService.delete(categoryId);
            return true;
        } catch (Exception exception) {
            throw badRequest("Category cannot be deleted while it is used by a product.");
        }
    }

    private void applyProductInput(Product product, ProductInput input, boolean isUpdate) {
        if (input == null) {
            throw badRequest("Product input is required.");
        }
        product.setProductName(requireText(input.getProductName(), "Product name", 150));
        product.setDescription(optionalText(input.getDescription(), "Description", 2_000));
        product.setPrice(requirePrice(input.getPrice()));
        product.setQuantity(requireNonNegative(input.getQuantity(), "Quantity"));
        product.setStatus(requireStatus(input.getStatus()));
        product.setImage(normalizeImage(input.getImage(), isUpdate ? product.getImage() : null));
        product.setCategory(findCategory(input.getCategoryId()));
    }

    private void applyCategoryInput(Category category, CategoryInput input, boolean isUpdate) {
        if (input == null) {
            throw badRequest("Category input is required.");
        }
        category.setCategoryname(requireText(input.getCategoryName(), "Category name", 50));
        category.setStatus(requireStatus(input.getStatus()));
        category.setImages(normalizeImage(input.getImage(), isUpdate ? category.getImages() : null));
    }

    private Category findCategory(Long categoryId) {
        Category category = categoryService.findById(requireCategoryId(categoryId));
        if (category == null) {
            throw notFound("Category was not found.");
        }
        return category;
    }

    private ProductView toProductView(Product product) {
        return new ProductView(product.getProductId(), product.getProductName(), product.getDescription(),
                product.getPrice(), product.getQuantity(), product.getImage(), product.getStatus(),
                toCategoryView(product.getCategory()));
    }

    private CategoryView toCategoryView(Category category) {
        return new CategoryView(category.getCategoryid(), category.getCategoryname(), category.getImages(),
                category.getStatus());
    }

    private void requireAdministrator() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw forbidden();
        }
        HttpSession session = servletAttributes.getRequest().getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionConstants.CURRENT_USER);
        if (!(user instanceof UserAccount account)
                || !"ADMIN".equalsIgnoreCase(account.getRoleName())
                || !account.isEnabled()
                || account.getStatus() != 1) {
            throw forbidden();
        }
    }

    private PageRequest pageRequest(Integer page, Integer size) {
        int requestedPage = page == null ? 1 : page;
        int requestedSize = size == null ? DEFAULT_PAGE_SIZE : size;
        return new PageRequest(Math.min(MAX_PAGE, Math.max(1, requestedPage)),
                Math.min(MAX_PAGE_SIZE, Math.max(1, requestedSize)));
    }

    private int totalPages(int totalItems, int pageSize) {
        return totalItems == 0 ? 0 : (totalItems + pageSize - 1) / pageSize;
    }

    private String normalizeKeyword(String value, int maximumLength) {
        String normalized = ValidationUtils.trimToNull(value);
        if (normalized != null && normalized.length() > maximumLength) {
            throw badRequest("Search keyword must not exceed " + maximumLength + " characters.");
        }
        return normalized;
    }

    private String requireText(String value, String label, int maximumLength) {
        String normalized = ValidationUtils.trimToNull(value);
        if (normalized == null) {
            throw badRequest(label + " is required.");
        }
        if (normalized.length() > maximumLength) {
            throw badRequest(label + " must not exceed " + maximumLength + " characters.");
        }
        return normalized;
    }

    private String optionalText(String value, String label, int maximumLength) {
        String normalized = ValidationUtils.trimToNull(value);
        if (normalized != null && normalized.length() > maximumLength) {
            throw badRequest(label + " must not exceed " + maximumLength + " characters.");
        }
        return normalized;
    }

    private BigDecimal requirePrice(Double price) {
        if (price == null || !Double.isFinite(price) || price < 0) {
            throw badRequest("Price must be a non-negative number.");
        }
        return BigDecimal.valueOf(price);
    }

    private int requireNonNegative(Integer value, String label) {
        if (value == null || value < 0) {
            throw badRequest(label + " must be a non-negative integer.");
        }
        return value;
    }

    private int requireStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw badRequest("Status must be 0 or 1.");
        }
        return status;
    }

    private String normalizeImage(String value, String currentValue) {
        if (value == null) {
            return currentValue;
        }
        String normalized = ValidationUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > 500 || !ValidationUtils.isValidHttpUrl(normalized)) {
            throw badRequest("Image must be a HTTP or HTTPS URL no longer than 500 characters.");
        }
        return normalized;
    }

    private Long requireProductId(Long id) {
        if (id == null || id <= 0) {
            throw badRequest("Product id is not valid.");
        }
        return id;
    }

    private int requireCategoryId(Long id) {
        if (id == null || id <= 0 || id > Integer.MAX_VALUE) {
            throw badRequest("Category id is not valid.");
        }
        return id.intValue();
    }

    private CatalogGraphqlException badRequest(String message) {
        return new CatalogGraphqlException(ErrorType.BAD_REQUEST, message);
    }

    private CatalogGraphqlException notFound(String message) {
        return new CatalogGraphqlException(ErrorType.NOT_FOUND, message);
    }

    private CatalogGraphqlException forbidden() {
        return new CatalogGraphqlException(ErrorType.FORBIDDEN, "Administrator access is required.");
    }

    private record PageRequest(int page, int size) {
    }
}
