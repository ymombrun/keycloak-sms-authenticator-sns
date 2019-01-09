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
public class KeycloakSmsMobilenumberValidationRequiredActionFactory implements RequiredActionFactory {
    private static Logger logger = Logger.getLogger(KeycloakSmsMobilenumberValidationRequiredActionFactory.class);
    private static final KeycloakSmsMobilenumberValidationRequiredAction SINGLETON = new KeycloakSmsMobilenumberValidationRequiredAction();

    public RequiredActionProvider create(KeycloakSession session) {
        logger.debug("create called ...");
        return SINGLETON;
    }

    public String getId() {
        logger.debug("getId called ... returning " + KeycloakSmsMobilenumberValidationRequiredAction.PROVIDER_ID);
        return KeycloakSmsMobilenumberValidationRequiredAction.PROVIDER_ID;
    }

    public String getDisplayText() {
        logger.debug("getDisplayText called ...");
        return "Validate Mobile Number";
    }

    public void init(Config.Scope config) {
        logger.debug("init called ...");
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ...");
    }

    public void close() {
        logger.debug("getId close ...");
    }
}
