package vn.iotstar.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;

class CategoryDaoIntegrationTest {
    private final CategoryDao categoryDao = new CategoryDao();

    @BeforeEach
    void clearDatabase() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.createQuery("DELETE FROM Product").executeUpdate();
            entityManager.createQuery("DELETE FROM Category").executeUpdate();
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        JpaConfig.close();
    }

    @Test
    void crudUsesTheJpaMappingAndSearchTreatsLikeCharactersLiterally() throws Exception {
        Category literalCategory = category("Jazz 100%_ Live", 1);
        Category wildcardCandidate = category("Jazz 100AB Live", 1);
        categoryDao.insert(literalCategory);
        categoryDao.insert(wildcardCandidate);

        assertNotNull(literalCategory.getCategoryid());
        List<Category> matches = categoryDao.searchByName("100%_");
        assertEquals(List.of("Jazz 100%_ Live"), matches.stream().map(Category::getCategoryname).toList());

        literalCategory.setCategoryname("Jazz Updated");
        literalCategory.setStatus(0);
        categoryDao.update(literalCategory);

        Category reloaded = categoryDao.findById(literalCategory.getCategoryid());
        assertEquals("Jazz Updated", reloaded.getCategoryname());
        assertEquals(0, reloaded.getStatus());

        categoryDao.delete(literalCategory.getCategoryid());
        assertEquals(null, categoryDao.findById(literalCategory.getCategoryid()));
    }

    @Test
    void databaseRejectsDuplicateCategoryNames() {
        categoryDao.insert(category("Unique category", 1));

        assertThrows(RuntimeException.class, () -> categoryDao.insert(category("Unique category", 1)));
    }

    @Test
    void databasePreventsDeletingACategoryUsedByAProduct() {
        Category category = category("Referenced category", 1);
        categoryDao.insert(category);
        persistProductReferencing(category);

        assertThrows(Exception.class, () -> categoryDao.delete(category.getCategoryid()));
    }

    private Category category(String name, int status) {
        Category category = new Category();
        category.setCategoryname(name);
        category.setStatus(status);
        return category;
    }

    private void persistProductReferencing(Category category) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Product product = new Product();
            product.setProductName("Referenced product");
            product.setDescription("Integration-test product");
            product.setPrice(BigDecimal.ONE);
            product.setQuantity(1);
            product.setStatus(1);
            product.setCategory(entityManager.getReference(Category.class, category.getCategoryid()));
            entityManager.persist(product);
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }
}
