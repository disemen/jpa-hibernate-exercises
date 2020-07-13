package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;

import javax.persistence.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction transaction = em.getTransaction();
//        transaction.begin();
//        try {
//            em.persist(account);
//            transaction.commit();
//        } catch (Exception e) {
//            transaction.rollback();
//            throw new AccountDaoException("Error", e);
//        } finally {
//            em.close();
//        }
//        performWithinPersistenceContext(entityManager -> entityManager.persist(account));
        performReturningWithinPersistenceContext(entityManager -> {
            entityManager.persist(account);
            return null;
        });
    }

    @Override
    public Account findById(Long id) {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction transaction = em.getTransaction();
//        transaction.begin();
//        try {
//            Account account = em.find(Account.class, id);
//            transaction.commit();
//            return account;
//        } catch (Exception e) {
//            transaction.rollback();
//            throw new AccountDaoException("Error", e);
//        } finally {
//            em.close();
//        }

        return performReturningWithinPersistenceContext(entityManager -> entityManager.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            Account account = em.createQuery("SELECT a FROM Account a WHERE a.email = :EMAIL", Account.class)
                    .setParameter("EMAIL", email)
                    .getSingleResult();
            transaction.commit();
            return account;
        } catch (Exception e) {
            transaction.rollback();
            throw new AccountDaoException("Error", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Account> findAll() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            List<Account> accounts = em.createQuery("SELECT a FROM Account a", Account.class)
                    .getResultList();
            transaction.commit();
            return accounts;
        } catch (Exception e) {
            transaction.rollback();
            throw new AccountDaoException("Error", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Account account) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            em.merge(account);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new AccountDaoException("Error", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void remove(Account account) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            em.remove(em.contains(account) ? account : em.merge(account));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new AccountDaoException("Error", e);
        } finally {
            em.close();
        }
    }

    private void performWithinPersistenceContext(Consumer<EntityManager> entityManagerConsumer) {
        performReturningWithinPersistenceContext(entityManager -> {
            entityManagerConsumer.accept(entityManager);
            return null;
        });
    }

    private <T> T performReturningWithinPersistenceContext(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        T result;
        try {
            result = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing dao operation. Transaction is rolled back!", e);
        } finally {
            entityManager.close();
        }
        return result;
    }
}