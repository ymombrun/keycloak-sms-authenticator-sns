package org.keycloak.action.required;

import org.keycloak.sms.KeycloakSmsConstants;
import org.keycloak.sms.impl.KeycloakSmsUtil;
import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class KeycloakSmsMobilenumberRequiredAction implements RequiredActionProvider {
    private static Logger logger = Logger.getLogger(KeycloakSmsMobilenumberRequiredAction.class);
    public static final String PROVIDER_ID = "sms_auth_check_mobile";

    public void evaluateTriggers(RequiredActionContext context) {
        logger.debug("evaluateTriggers called ...");
    }

    public void requiredActionChallenge(RequiredActionContext context) {
        logger.debug("requiredActionChallenge called ...");

        UserModel user = context.getUser();

        List<String> mobileNumberCreds = user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE);

        String mobileNumber = null;

        if (mobileNumberCreds != null && !mobileNumberCreds.isEmpty()) {
            mobileNumber = mobileNumberCreds.get(0);
        }

        Theme theme = null;
        Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
        try {
            theme = context.getSession().theme().getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
        } catch (Exception e) {
            logger.error("Unable to get theme required to send SMS", e);
        }

        if (mobileNumber != null && KeycloakSmsUtil.validateTelephoneNumber(mobileNumber, KeycloakSmsUtil.getMessage(theme, locale, KeycloakSmsConstants.MSG_MOBILE_REGEXP))) {
            // Mobile number is configured
            context.ignore();
        } else {
            // Mobile number is not configured or is invalid
            Response challenge = context.form().createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        }
    }

    public void processAction(RequiredActionContext context) {
        logger.debug("processAction called ...");

        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"));
        Theme theme = null;
        Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
        try {
            theme = context.getSession().theme().getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
        } catch (Exception e) {
            logger.error("Unable to get theme required to send SMS", e);
        }

        if (answer != null && answer.length() > 0 && KeycloakSmsUtil.validateTelephoneNumber(answer, KeycloakSmsUtil.getMessage(theme, locale, KeycloakSmsConstants.MSG_MOBILE_REGEXP))) {
            logger.debug("Valid matching mobile numbers supplied, save credential ...");
            List<String> mobileNumber = new ArrayList<String>();
            mobileNumber.add(answer);

            UserModel user = context.getUser();
            user.setAttribute(KeycloakSmsConstants.ATTR_MOBILE, mobileNumber);
            user.addRequiredAction(KeycloakSmsMobilenumberValidationRequiredAction.PROVIDER_ID);
            context.success();
        } else {
            logger.debug("Either one of two fields wasn\'t complete, or the first contains an invalid number...");
            Response challenge = context.form()
                    .setError("mobile_number.no.valid")
                    .createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
