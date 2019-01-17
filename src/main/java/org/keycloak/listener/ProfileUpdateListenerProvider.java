package org.keycloak.listener;

import org.jboss.logging.Logger;
import org.keycloak.action.required.KeycloakSmsMobilenumberRequiredAction;
import org.keycloak.action.required.KeycloakSmsMobilenumberValidationRequiredAction;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.forms.account.AccountProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.sms.KeycloakSmsConstants;
import org.keycloak.sms.KeycloakSmsSenderService;

import javax.ws.rs.core.Response;

public class ProfileUpdateListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private static Logger logger = Logger.getLogger(ProfileUpdateListenerProvider.class);

    public ProfileUpdateListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void close() {

    }

    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(EventType.UPDATE_PROFILE)) {
            logger.info("Checking mobile number after profile update");
            UserModel user = session.users().getUserById(event.getUserId(), session.getContext().getRealm());
            String mobileNumber = !user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE).isEmpty()
                    ? user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE).get(0)
                    : null;

            String mobileNumberVerified = !user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE_VERIFIED).isEmpty()
                    ? user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE_VERIFIED).get(0)
                    : null;

            if (mobileNumber != null && (mobileNumberVerified == null || !mobileNumber.equalsIgnoreCase(mobileNumberVerified))) {
                logger.info("Checking mobile number required");
                // validation required
                user.addRequiredAction(KeycloakSmsMobilenumberValidationRequiredAction.PROVIDER_ID);
                if (mobileNumberVerified != null) {
                    user.removeAttribute(KeycloakSmsConstants.ATTR_MOBILE_VERIFIED);
                }
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

    }
}
