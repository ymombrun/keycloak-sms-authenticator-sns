package org.keycloak.service;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class SmsSenderProviderFactoryImpl implements SmsSenderProviderFactory {

    private static Logger logger = Logger.getLogger(SmsSenderProviderFactoryImpl.class);

    static final SmsSenderService SINGLETON = new SmsSenderServiceImpl();

    public static final String PROVIDER_ID = "sms-sender-service";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public SmsSenderService create(KeycloakSession session) {
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
