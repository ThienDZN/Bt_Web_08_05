package vn.iotstar.listener;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import vn.iotstar.config.AppProperties;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.config.PasswordUtils;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.UserAccount;

public class AppBootstrapListener implements ServletContextListener {
    private static final String PROFILE_DEMO_USERNAME = "thien";
    private static final String PROFILE_DEMO_EMAIL = "thien@example.com";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (AppProperties.getBoolean("app.demo.seed", false)) {
            seedAdminAndDemoData();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaConfig.close();
    }

    private void seedAdminAndDemoData() {
        String demoPassword = requiredDemoPassword();
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Long userCount = entityManager.createQuery("SELECT COUNT(u) FROM UserAccount u", Long.class).getSingleResult();
            if (userCount == 0) {
                UserAccount admin = new UserAccount();
                admin.setFullName("System Admin");
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(PasswordUtils.encode(demoPassword));
                admin.setRoleName("ADMIN");
                admin.setEnabled(true);
                admin.setStatus(1);
                entityManager.persist(admin);
            }

            ensureProfileDemoUser(entityManager, demoPassword);
            Map<String, Category> categories = ensureCategories(entityManager);
            syncDemoCatalog(entityManager, categories);

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    private String requiredDemoPassword() {
        String password = AppProperties.get("app.demo.password", "");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("APP_DEMO_PASSWORD must be set when APP_DEMO_SEED is enabled.");
        }
        return password;
    }

    private void ensureProfileDemoUser(EntityManager entityManager, String demoPassword) {
        List<UserAccount> matches = entityManager.createQuery(
                        "SELECT u FROM UserAccount u WHERE LOWER(u.username) = :username",
                        UserAccount.class)
                .setParameter("username", PROFILE_DEMO_USERNAME.toLowerCase())
                .setMaxResults(1)
                .getResultList();

        UserAccount profileUser = matches.isEmpty() ? null : matches.get(0);
        if (profileUser == null) {
            profileUser = new UserAccount();
            profileUser.setFullName("Thien");
            profileUser.setUsername(PROFILE_DEMO_USERNAME);
            profileUser.setEmail(PROFILE_DEMO_EMAIL);
            profileUser.setRoleName("USER");
            profileUser.setEnabled(true);
            profileUser.setStatus(1);
            profileUser.setPasswordHash(PasswordUtils.encode(demoPassword));
            entityManager.persist(profileUser);
            return;
        }

        if (profileUser.getFullName() == null || profileUser.getFullName().isBlank()) {
            profileUser.setFullName("Thien");
        }
        if (profileUser.getEmail() == null || profileUser.getEmail().isBlank()) {
            profileUser.setEmail(PROFILE_DEMO_EMAIL);
        }
        profileUser.setRoleName("USER");
        profileUser.setEnabled(true);
        profileUser.setStatus(1);
        profileUser.setPasswordHash(PasswordUtils.encode(demoPassword));
    }

    private Map<String, Category> ensureCategories(EntityManager entityManager) {
        List<Category> categories = entityManager.createQuery("SELECT c FROM Category c", Category.class).getResultList();
        Map<String, Category> byName = categories.stream()
                .collect(Collectors.toMap(category -> category.getCategoryname().toLowerCase(Locale.ROOT),
                        category -> category, (left, right) -> left, LinkedHashMap::new));

        createCategoryIfMissing(entityManager, byName, "Playlist Opener", "https://i.ytimg.com/vi/poGyHfrJ_uo/maxresdefault.jpg");
        createCategoryIfMissing(entityManager, byName, "Mid Playlist", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f");
        createCategoryIfMissing(entityManager, byName, "Closing Run", "https://images.unsplash.com/photo-1511379938547-c1f69419868d");

        entityManager.flush();
        return byName;
    }

    private void createCategoryIfMissing(EntityManager entityManager, Map<String, Category> byName, String name, String image) {
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (byName.containsKey(normalizedName)) {
            return;
        }
        Category category = new Category();
        category.setCategoryname(name);
        category.setImages(image);
        category.setStatus(1);
        entityManager.persist(category);
        byName.put(normalizedName, category);
    }

    private void syncDemoCatalog(EntityManager entityManager, Map<String, Category> categoryMap) {
        Category opener = categoryMap.get("playlist opener");
        Category mid = categoryMap.get("mid playlist");
        Category closing = categoryMap.get("closing run");

        persistProduct(entityManager, "Khóc Đấy (Album Version)", "Track 1 of 12 from the YouTube playlist. Album Version. Runtime: 3:52.", new BigDecimal("360897"), 1, "https://i.ytimg.com/vi/poGyHfrJ_uo/maxresdefault.jpg", opener);
        persistProduct(entityManager, "Bút Chì Bạc (Album Version)", "Track 2 of 12 from the YouTube playlist. Album Version. Runtime: 2:57.", new BigDecimal("121795"), 2, "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f", opener);
        persistProduct(entityManager, "Hoá Ra Là (feat. Wala)", "Track 3 of 12. Featuring Wala. Runtime: 3:22.", new BigDecimal("532449"), 3, "https://images.unsplash.com/photo-1485579149621-3123dd979885", opener);
        persistProduct(entityManager, "Gội Đầu (feat. Hà Lê)", "Track 4 of 12. Featuring Hà Lê. Runtime: 4:25.", new BigDecimal("873446"), 4, "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3", opener);
        persistProduct(entityManager, "100%", "Track 5 of 12 from the playlist. Runtime: 2:57.", new BigDecimal("419868"), 5, "https://images.unsplash.com/photo-1501612780327-45045538702b", mid);
        persistProduct(entityManager, "Căn Gác Lặng", "Track 6 of 12 from the playlist. Runtime: 2:32.", new BigDecimal("252183"), 6, "https://images.unsplash.com/photo-1507838153414-b4b713384a76", mid);
        persistProduct(entityManager, "Đồng Ý (kết hợp với Thơ Tơ Mơ)", "Track 7 of 12. Collaboration with Thơ Tơ Mơ. Runtime: 2:41.", new BigDecimal("840501"), 7, "https://images.unsplash.com/photo-1496293455970-f8581aae0e3b", mid);
        persistProduct(entityManager, "60m Vuông", "Track 8 of 12 from the playlist. Runtime: 2:28.", new BigDecimal("311093"), 8, "https://images.unsplash.com/photo-1459749411175-04bf5292ceea", mid);
        persistProduct(entityManager, "Nấu Con Beat (feat. Wala)", "Track 9 of 12. Featuring Wala. Runtime: 3:21.", new BigDecimal("150139"), 9, "https://images.unsplash.com/photo-1516280440614-37939bbacd81", closing);
        persistProduct(entityManager, "Rất (feat. SUNI, Pixel Neko)", "Track 10 of 12. Featuring SUNI and Pixel Neko. Runtime: 3:21.", new BigDecimal("120034"), 10, "https://images.unsplash.com/photo-1510915361894-db8b60106cb1", closing);
        persistProduct(entityManager, "Sáng Ra Chỉ Cần", "Track 11 of 12 from the playlist. Runtime: 3:30.", new BigDecimal("127532"), 11, "https://images.unsplash.com/photo-1506157786151-b8491531f063", closing);
        persistProduct(entityManager, "Tình Nhân Muôn Kiếp", "Track 12 of 12 from the playlist. Runtime: 3:59.", new BigDecimal("580643"), 12, "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee", closing);
    }

    private void persistProduct(EntityManager entityManager, String name, String description, BigDecimal price,
                                int quantity, String image, Category category) {
        Long existingCount = entityManager.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.productName = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        if (existingCount > 0) {
            return;
        }
        Product product = new Product();
        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImage(image);
        product.setStatus(1);
        product.setCategory(category);
        entityManager.persist(product);
    }
}
