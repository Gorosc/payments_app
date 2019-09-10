package org.cgoro.config;

import dagger.Component;
import org.cgoro.db.DBManager;

import javax.inject.Singleton;

@Singleton
@Component(modules = ConfigModule.class)
public interface DIEngine {
    DBManager dbManager();
}

