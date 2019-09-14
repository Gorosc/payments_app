package org.cgoro.db.dao;

import java.util.List;

/**
 * @param <T> DAO Interface
 */
public interface DAO<T> {
    T find(String id);
    T find(Long id);
    List<T> getAll();
    void save(T entity);
    void update(T entity);
    boolean exists(String id);
    boolean exists(Long id);

}
