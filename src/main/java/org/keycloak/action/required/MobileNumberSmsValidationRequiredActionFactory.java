package org.keycloak.action.required;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Mobile Number Input (RequireAction)
 * Created by nickpack on 15/08/2017.
 */
public class MobileNumberSmsValidationRequiredActionFactory implements RequiredActionFactory {
    private static Logger logger = Logger.getLogger(MobileNumberSmsValidationRequiredActionFactory.class);
    private static final MobileNumberSmsValidationRequiredAction SINGLETON = new MobileNumberSmsValidationRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        logger.debug("create called ...");
        return SINGLETON;
    }

    @Override
    public String getId() {
        logger.debug("getId called ... returning " + MobileNumberSmsValidationRequiredAction.PROVIDER_ID);
        return MobileNumberSmsValidationRequiredAction.PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        logger.debug("getDisplayText called ...");
        return "Validate Mobile Number";
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
