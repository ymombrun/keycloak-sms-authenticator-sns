package org.keycloak.action.required;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.util.Sms;
import org.keycloak.util.UserProfile;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Action to add mobile number to user
 */
public class MobileNumberRequiredAction implements RequiredActionProvider {
    private static Logger logger = Logger.getLogger(MobileNumberRequiredAction.class);
    public static final String PROVIDER_ID = "sms_auth_check_mobile";

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        logger.debug("requiredActionChallenge for Mobile Number required action called ...");

        UserModel user = context.getUser();
        var mobileNumber = UserProfile.getMobileNumber(user, false);
        boolean numberAlreadyTaken = false;

        if (mobileNumber.isPresent()) {
            numberAlreadyTaken = UserProfile.checkNumberAlreadyTaken(
                            mobileNumber.get(),
                            context.getSession(),
                            user,
                            context.getRealm()
                    );
        }

        if (mobileNumber.isPresent() && !numberAlreadyTaken) {
            logger.debug("Phone number already set and valid, ignoring");
            // Mobile number is configured
            context.ignore();
        } else {
            // Mobile number is not configured or is invalid
            Response challenge;
            if (numberAlreadyTaken) {
                logger.debug("Phone number already taken, asking new one");
                challenge = context
                        .form()
                        .setError("mobile_number.already.taken")
                        .createForm("sms-validation-mobile-number.ftl");
            } else {
                logger.debug("No phone number set, asking one");
                challenge = context
                        .form()
                        .createForm("sms-validation-mobile-number.ftl");
            }

            context.challenge(challenge);
        }
    }

    @Override
    public void processAction(RequiredActionContext context) {
        logger.debug("processAction for mobileNumber required action called ...");

        Optional<String> mobileNumber = Optional.ofNullable(context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"))
                .map(Sms::checkAndFormatMobileNumber)
                .filter(StringUtils::isNotEmpty);

        UserModel user = context.getUser();

        boolean numberAlreadyTaken = mobileNumber
                .map(mobile -> UserProfile.checkNumberAlreadyTaken(
                        mobile,
                        context.getSession(),
                        user,
                        context.getRealm())
                )
                .orElse(false);

        if (mobileNumber.isPresent() && !numberAlreadyTaken) {
            logger.debug("Valid mobile numbers supplied, save credential and remove required action ...");
            UserProfile.addMobileNumberAndUpdateActions(user, mobileNumber.get());
            context.success();
        } else {
            logger.debug("The field contains an invalid or already taken number...");
            Response challenge;
            if (mobileNumber.isPresent()) {
                challenge = context.form()
                        .setError("mobile_number.already.taken")
                        .createForm("sms-validation-mobile-number.ftl");
            } else {
                challenge = context.form()
                        .setError("mobile_number.no.valid")
                        .createForm("sms-validation-mobile-number.ftl");
            }
            context.challenge(challenge);
        }
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        logger.debug("evaluateTriggers called ...");
    }

    @Override
    public void close() {
        logger.debug("close called ...");
    }
}
