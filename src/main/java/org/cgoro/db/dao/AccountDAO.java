package org.cgoro.db.dao;

import org.cgoro.db.entity.Account;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class AccountDAO extends DAOImpl
{
    @Inject
    AccountDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public Account find(String accountId){
        return em.createNamedQuery("findByAccountId", Account.class).setParameter("accountId",accountId).getSingleResult();
    }

    @Override
    public Account find(Long id) {
        return em.find(Account.class, id);
    }

    @Override
    public List getAll() {
       return em.createNamedQuery("allAccounts", Account.class).getResultList();
    }

}
