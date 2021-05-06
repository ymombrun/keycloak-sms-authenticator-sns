package org.keycloak.util;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.action.required.MobileNumberRequiredAction;
import org.keycloak.action.required.MobileNumberSmsValidationRequiredAction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Optional;

public class UserProfile {
    public static final String ATTR_MOBILE = "mobile_number";

    public static final String ATTR_MOBILE_VERIFIED = "mobile_number_verified";

    private static Logger logger = Logger.getLogger(UserProfile.class);

    public static Optional<String> getMobileNumber(UserModel user, boolean verified) {
        return user.getAttributeStream(verified ? ATTR_MOBILE_VERIFIED: ATTR_MOBILE)
                .map(Sms::checkAndFormatMobileNumber)
                .filter(StringUtils::isNotEmpty)
                .findFirst();

    }

    public static void addMobileNumberAndUpdateActions(UserModel user, String mobileNumber) {
        user.setAttribute(UserProfile.ATTR_MOBILE, List.of(mobileNumber));
        user.removeRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
        user.addRequiredAction(MobileNumberSmsValidationRequiredAction.PROVIDER_ID);
    }

    public static void removeMobileNumberAndUpdateActions(UserModel user) {
        user.removeAttribute("mobile_number");
        user.removeRequiredAction(MobileNumberSmsValidationRequiredAction.PROVIDER_ID);
        user.addRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
    }

    public static void addVerifiedNumberAndUpdateActions(UserModel user, String verifiedMobileNumber) {
        user.setAttribute(UserProfile.ATTR_MOBILE_VERIFIED, List.of(verifiedMobileNumber));
        user.removeRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
    }

    public static boolean checkNumberAlreadyTaken(String mobileNumber, KeycloakSession session, UserModel user, RealmModel realm) {
        boolean numberAlreadyTaken = false;
        if (mobileNumber != null) {
            logger.debug("search for user with phone " + mobileNumber);
            numberAlreadyTaken = session
                    .users()
                    .searchForUserByUserAttributeStream("mobile_number", mobileNumber, realm)
                    .filter(existingUser -> !existingUser.equals(user))
                    .count() > 0;
        }
        logger.debug("Number already taken ? " + numberAlreadyTaken);
        return numberAlreadyTaken;
    }
}
