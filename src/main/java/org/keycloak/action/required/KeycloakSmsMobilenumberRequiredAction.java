package org.keycloak.action.required;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.sms.KeycloakSmsConstants;
import org.keycloak.sms.KeycloakSmsSenderService;
import org.keycloak.sms.impl.KeycloakSmsUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class KeycloakSmsMobilenumberRequiredAction implements RequiredActionProvider {
    private static Logger logger = Logger.getLogger(KeycloakSmsMobilenumberRequiredAction.class);
    public static final String PROVIDER_ID = "sms_auth_check_mobile";

    public void evaluateTriggers(RequiredActionContext context) {
        logger.debug("evaluateTriggers called ...");
    }

    public void requiredActionChallenge(RequiredActionContext context) {
        logger.debug("requiredActionChallenge for mobileNumber required action called ...");

        UserModel user = context.getUser();
        List<String> mobileNumberCreds = user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE);
        String mobileNumber = null;

        try {
            if (mobileNumberCreds != null && !mobileNumberCreds.isEmpty()) {
                mobileNumber = KeycloakSmsUtil.checkAndFormatMobileNumber(mobileNumberCreds.get(0));
            }
        } catch (Exception e) {
            logger.warn("Unable to format number "+e.getLocalizedMessage());
        }

        boolean numberAlreadyTaken = context
                .getSession()
                .getProvider(KeycloakSmsSenderService.class)
                .checkNumberAlreadyTaken(
                        mobileNumber,
                        context.getSession(),
                        user,
                        context.getRealm()
                );

        if (mobileNumber != null && !numberAlreadyTaken) {
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

    public void processAction(RequiredActionContext context) {
        logger.debug("processAction for mobileNumber required action called ...");

        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"));
        UserModel user = context.getUser();
        String mobileNumber = null;
        try {
            mobileNumber = KeycloakSmsUtil.checkAndFormatMobileNumber(answer);
        } catch (Exception e) {
            logger.warn("Unable to format number "+e.getLocalizedMessage());
        }

        boolean numberAlreadyTaken = context
                .getSession()
                .getProvider(KeycloakSmsSenderService.class)
                .checkNumberAlreadyTaken(
                        mobileNumber,
                        context.getSession(),
                        user,
                        context.getRealm()
                );

        if (mobileNumber != null && !numberAlreadyTaken) {
            logger.debug("Valid mobile numbers supplied, save credential ...");
            List<String> mobileNumberList = new ArrayList<String>();
            mobileNumberList.add(mobileNumber);

            user.setAttribute(KeycloakSmsConstants.ATTR_MOBILE, mobileNumberList);
            user.addRequiredAction(KeycloakSmsMobilenumberValidationRequiredAction.PROVIDER_ID);
            context.success();
        } else {
            logger.debug("The field contains an invalid number...");
            if (numberAlreadyTaken) {
                Response challenge = context.form()
                        .setError("mobile_number.already.taken")
                        .createForm("sms-validation-mobile-number.ftl");
                context.challenge(challenge);
            } else {
                Response challenge = context.form()
                        .setError("mobile_number.no.valid")
                        .createForm("sms-validation-mobile-number.ftl");
                context.challenge(challenge);
            }
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
