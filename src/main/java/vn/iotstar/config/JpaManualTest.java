package vn.iotstar.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Video;

public class JpaManualTest {
    public static void main(String[] args) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        Category category = new Category();
        category.setCategoryname("Iphone");
        category.setImages("iphone.jpg");
        category.setStatus(1);

        Video video = new Video();
        video.setVideoId("v01");
        video.setTitle("JPA Test");
        video.setActive(1);
        video.setViews(100);
        video.setCategory(category);

        try {
            transaction.begin();
            entityManager.persist(category);
            entityManager.persist(video);
            transaction.commit();
            System.out.println("Insert test data success.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
