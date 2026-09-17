package vn.iotstar.dao.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.IUserAccountDao;
import vn.iotstar.entity.UserAccount;

public class UserAccountDao implements IUserAccountDao {
    @Override
    public void insert(UserAccount user) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(user);
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

    @Override
    public void update(UserAccount user) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(user);
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

    @Override
    public void delete(Long id) throws Exception {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            UserAccount user = entityManager.find(UserAccount.class, id);
            if (user == null) {
                throw new Exception("No user was found for id = " + id);
            }
            entityManager.createQuery("DELETE FROM OtpVerification o WHERE o.user.userId = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            entityManager.remove(user);
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

    @Override
    public UserAccount findById(Long id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            return entityManager.find(UserAccount.class, id);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public UserAccount findByEmail(String email) {
        return findSingle("SELECT u FROM UserAccount u WHERE LOWER(u.email) = LOWER(:value)", email);
    }

    @Override
    public UserAccount findByUsername(String username) {
        return findSingle("SELECT u FROM UserAccount u WHERE LOWER(u.username) = LOWER(:value)", username);
    }

    @Override
    public UserAccount findByUsernameOrEmail(String value) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<UserAccount> query = entityManager.createQuery(
                    "SELECT u FROM UserAccount u WHERE LOWER(u.username) = LOWER(:value) OR LOWER(u.email) = LOWER(:value)",
                    UserAccount.class);
            query.setParameter("value", value);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<UserAccount> findAll() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            return entityManager.createNamedQuery("UserAccount.findAll", UserAccount.class).getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<UserAccount> search(String keyword) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<UserAccount> query = entityManager.createQuery(
                    "SELECT u FROM UserAccount u " +
                            "WHERE LOWER(u.fullName) LIKE LOWER(:keyword) ESCAPE '!' " +
                            "OR LOWER(u.username) LIKE LOWER(:keyword) ESCAPE '!' " +
                            "OR LOWER(u.email) LIKE LOWER(:keyword) ESCAPE '!' " +
                            "OR LOWER(u.roleName) LIKE LOWER(:keyword) ESCAPE '!' " +
                            "ORDER BY u.userId DESC",
                    UserAccount.class);
            query.setParameter("keyword", "%" + escapeLikePattern(keyword) + "%");
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    private UserAccount findSingle(String jpql, String value) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<UserAccount> query = entityManager.createQuery(jpql, UserAccount.class);
            query.setParameter("value", value);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    private String escapeLikePattern(String value) {
        return value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
