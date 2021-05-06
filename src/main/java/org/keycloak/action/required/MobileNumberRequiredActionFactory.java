package org.keycloak.action.required;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MobileNumberRequiredActionFactory implements RequiredActionFactory {
    private static Logger logger = Logger.getLogger(MobileNumberRequiredActionFactory.class);
    private static final MobileNumberRequiredAction SINGLETON = new MobileNumberRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        logger.debug("create called ...");
        return SINGLETON;
    }

    @Override
    public String getId() {
        logger.debug("getId called ... returning " + MobileNumberRequiredAction.PROVIDER_ID);
        return MobileNumberRequiredAction.PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        logger.debug("getDisplayText called ...");
        return "Update Mobile Number";
    }

    @Override
    public void init(Config.Scope config) {
        logger.debug("init called ...");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ...");
    }

    @Override
    public void close() {
        logger.debug("getId close ...");
    }
}
