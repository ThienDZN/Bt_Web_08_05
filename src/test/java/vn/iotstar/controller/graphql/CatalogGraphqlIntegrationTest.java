package vn.iotstar.controller.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.config.SessionConstants;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.UserAccount;

@SpringBootTest
class CatalogGraphqlIntegrationTest {
    @Autowired
    private ExecutionGraphQlService graphQlService;

    private GraphQlTester graphQlTester;
    private int rockCategoryId;

    @BeforeEach
    void setUp() {
        graphQlTester = ExecutionGraphQlServiceTester.create(graphQlService);
        clearAndSeedCatalog();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        JpaConfig.close();
    }

    @Test
    void publicQueriesSortFilterAndPageCatalogData() {
        graphQlTester.document("""
                query {
                    productsByPrice { productName price category { categoryName } }
                    productsByCategory(categoryId: %d) { productName }
                    products(keyword: "Alpha", page: 1, size: 1) { totalItems totalPages items { productName } }
                    categories(keyword: "Rock", page: 1, size: 5) { totalItems items { categoryName } }
                }
                """.formatted(rockCategoryId))
                .execute()
                .path("productsByPrice[*].productName").entityList(String.class)
                .satisfies(names -> assertEquals(List.of("Beta Song", "Alpha Song"), names))
                .path("productsByCategory[*].productName").entityList(String.class)
                .satisfies(names -> assertEquals(List.of("Alpha Song"), names))
                .path("products.totalItems").entity(Integer.class).isEqualTo(1)
                .path("products.totalPages").entity(Integer.class).isEqualTo(1)
                .path("products.items[0].productName").entity(String.class).isEqualTo("Alpha Song")
                .path("categories.totalItems").entity(Integer.class).isEqualTo(1)
                .path("categories.items[0].categoryName").entity(String.class).isEqualTo("Rock");
    }

    @Test
    void mutationRequiresAdminAndWorksForAnActiveAdminSession() {
        String mutation = """
                mutation {
                    createCategory(input: {categoryName: "GraphQL Category", status: 1}) { categoryName status }
                }
                """;

        graphQlTester.document(mutation).execute().errors().satisfy(errors -> {
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).getMessage().contains("Administrator access is required"));
        });

        MockHttpServletRequest request = new MockHttpServletRequest();
        UserAccount administrator = new UserAccount();
        administrator.setRoleName("ADMIN");
        administrator.setEnabled(true);
        administrator.setStatus(1);
        request.getSession().setAttribute(SessionConstants.CURRENT_USER, administrator);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            graphQlTester.document(mutation).execute()
                    .path("createCategory.categoryName").entity(String.class).isEqualTo("GraphQL Category")
                    .path("createCategory.status").entity(Integer.class).isEqualTo(1);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    private void clearAndSeedCatalog() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.createQuery("DELETE FROM Product").executeUpdate();
            entityManager.createQuery("DELETE FROM Category").executeUpdate();

            Category rock = category("Rock");
            Category jazz = category("Jazz");
            entityManager.persist(rock);
            entityManager.persist(jazz);
            entityManager.flush();
            rockCategoryId = rock.getCategoryid();

            entityManager.persist(product("Alpha Song", "A search result", "20.00", rock));
            entityManager.persist(product("Beta Song", "A second entry", "5.00", jazz));
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    private Category category(String name) {
        Category category = new Category();
        category.setCategoryname(name);
        category.setStatus(1);
        return category;
    }

    private Product product(String name, String description, String price, Category category) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(1);
        product.setStatus(1);
        product.setCategory(category);
        return product;
    }
}
