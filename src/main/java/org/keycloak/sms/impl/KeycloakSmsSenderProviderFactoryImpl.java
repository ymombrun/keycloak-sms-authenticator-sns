package org.keycloak.sms.impl;

import org.keycloak.sms.KeycloakSmsSenderProviderFactory;
import org.keycloak.sms.KeycloakSmsSenderService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsSenderProviderFactoryImpl implements KeycloakSmsSenderProviderFactory {

    private static Logger logger = Logger.getLogger(KeycloakSmsSenderProviderFactoryImpl.class);

    static final KeycloakSmsSenderService SINGLETON = new KeycloakSmsSenderServiceImpl();

    public static final String PROVIDER_ID = "sms-sender-service";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public KeycloakSmsSenderService create(KeycloakSession session) {
        logger.debug("Create PROVIDER called ...");
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope scope) {
        logger.debug("init PROVIDER called ...");
        SINGLETON.setConfig(scope);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

}
