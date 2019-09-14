package org.cgoro.db.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public abstract class DAOImpl<T> implements DAO<T> {

    EntityManager em;

    @Override
    public void save(Object entity) {
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
    }

    @Override
    public boolean exists(String id) {
        try {
            find(id);
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(Long id) {
        try {
            find(id);
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public void update(Object entity) {
        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
    }

    public void updateAll(List<Object> entityList) {
        em.getTransaction().begin();
        entityList.forEach(object -> em.merge(object));
        em.getTransaction().commit();
    }
}
