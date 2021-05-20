package org.keycloak.listener;

import org.jboss.logging.Logger;
import org.keycloak.action.required.MobileNumberSmsValidationRequiredAction;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.util.UserProfile;

public class ProfileUpdateMobileNumberActions implements EventListenerProvider {

    private KeycloakSession session;
    private static Logger logger = Logger.getLogger(ProfileUpdateMobileNumberActions.class);

    public ProfileUpdateMobileNumberActions(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(EventType.UPDATE_PROFILE)) {
            logger.debug("Checking mobile number after profile update");
            UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());

            var mobileNumber = UserProfile.getMobileNumber(user, false);
            var verifiedMobileNumber = UserProfile.getMobileNumber(user, true);
            if (mobileNumber.isPresent() && (!verifiedMobileNumber.isPresent() || !verifiedMobileNumber.get().equals(mobileNumber.get()))) {
                logger.debug("Checking mobile number required");
                // validation required
                user.addRequiredAction(MobileNumberSmsValidationRequiredAction.PROVIDER_ID);
                user.addRequiredAction(MobileNumberSmsValidationRequiredAction.FORCE_REFRESH_SMS_CODE);
                if (verifiedMobileNumber.isPresent()) {
                    // removing verified as doesn't match
                    user.removeAttribute(UserProfile.ATTR_MOBILE_VERIFIED);
                }
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

    }

    @Override
    public void close() {

    }
}
