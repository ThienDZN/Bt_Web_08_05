package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.IOtpVerificationDao;
import vn.iotstar.entity.OtpVerification;

public class OtpVerificationDao implements IOtpVerificationDao {
    @Override
    public void insert(OtpVerification otpVerification) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(otpVerification);
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
    public void markAllUnusedAsUsed(String email, String purpose) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.createQuery(
                            "UPDATE OtpVerification o SET o.used = true WHERE LOWER(o.email) = LOWER(:email) AND o.purpose = :purpose AND o.used = false")
                    .setParameter("email", email)
                    .setParameter("purpose", purpose)
                    .executeUpdate();
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
    public OtpVerification findLatest(String email, String purpose) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<OtpVerification> query = entityManager.createQuery(
                    "SELECT o FROM OtpVerification o LEFT JOIN FETCH o.user WHERE LOWER(o.email) = LOWER(:email) AND o.purpose = :purpose ORDER BY o.createdAt DESC",
                    OtpVerification.class);
            query.setParameter("email", email);
            query.setParameter("purpose", purpose);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(OtpVerification otpVerification) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(otpVerification);
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
}
