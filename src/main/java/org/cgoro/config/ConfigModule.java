package org.cgoro.config;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Module
public class ConfigModule {

    private EntityManagerFactory emf;
    private EntityManager em;


    public ConfigModule() {
        this.emf = Persistence.createEntityManagerFactory("paymentsAppPU");
        this.em =  emf.createEntityManager();
    }

    @Provides @Singleton EntityManager provideEntityManager() {
        return this.em;
    }
    @Provides @Singleton EntityManagerFactory provideEntityManagerFactory() {return this.emf;}
}
