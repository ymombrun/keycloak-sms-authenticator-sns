package org.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.action.required.MobileNumberRequiredAction;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import org.keycloak.service.SmsSenderService;
import org.keycloak.util.Configuration;
import org.keycloak.util.UserProfile;

import javax.ws.rs.core.Response;

/**
 * Created by joris on 11/11/2016.
 */
public class SmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(SmsAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        boolean onlyForVerification= Configuration.getConfigBoolean(config, Configuration.MOBILE_VERIFICATION_ENABLED);

        var mobileNumber = UserProfile.getMobileNumber(user, false);
        var mobileNumberVerified = UserProfile.getMobileNumber(user, true);

        if (!onlyForVerification // send sms code on every auth
                || !mobileNumber.isPresent() // no mobile
                || !mobileNumberVerified.isPresent() // not verified
                || !mobileNumber.get().equals(mobileNumberVerified.get())
        ) {
            if (mobileNumber.isPresent()) {
                // The mobile number is configured --> send an SMS
                SmsSenderService provider = context.getSession().getProvider(SmsSenderService.class);
                logger.debug("Sending sms code from authenticator");

                if ( provider.sendSmsCode(mobileNumber.get(), context)) {
                    Response challenge = context.form()
                            .setAttribute("mobile_number", mobileNumber)
                            .setAttribute("code_digits", provider.getCodeDigits(context.getSession(), context.getUser()))
                            .createForm("sms-validation.ftl");
                    context.challenge(challenge);
                } else {
                    Response challenge = context.form()
                            .setError("sms-auth.not.send")
                            .createForm("sms-validation-error.ftl");
                    // TODO remove mobile ?
                    context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
                }
            } else {
                boolean askForMobileNumber = Configuration.getConfigBoolean(config, Configuration.MOBILE_ASK_FOR_ENABLED);
                if (askForMobileNumber) {
                    // Enable access and ask for mobile number
                    logger.debug("Adding required action to get mobile number");
                    user.addRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
                    context.success();
                } else {
                    // The mobile number is NOT configured --> complain
                    Response challenge = context.form()
                            .setError("sms-auth.not.mobile")
                            .createForm("sms-validation-error.ftl");
                    context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED, challenge);
                }
            }
        } else {
            logger.debug("Skip SMS code because onlyForVerification or verified mobileNumber");
            context.success();
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        boolean changeNumber = Boolean.valueOf(context.getHttpRequest().getFormParameters().getFirst("changeNumber"));
        var user = context.getUser();
        logger.debug("Change Number from validation action ? " + changeNumber);
        if (changeNumber) {
            UserProfile.removeMobileNumberAndUpdateActions(context.getUser());
            context.success();
        } else {
            SmsSenderService provider = context.getSession().getProvider(SmsSenderService.class);
            SmsSenderService.CODE_STATUS status = provider.validateCode(context);
            Response challenge;

            var mobileNumber = UserProfile.getMobileNumber(user, false);
            switch (status) {
                case EXPIRED:
                    provider.sendSmsCode(mobileNumber.get(), context);
                    challenge = context.form()
                            .setError("sms-auth.code.expired")
                            .setAttribute("mobile_number", mobileNumber.orElse(null))
                            .setAttribute("code_digits", provider.getCodeDigits(context.getSession(), user))
                            .createForm("sms-validation.ftl");
                    context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challenge);
                    break;

                case INVALID:
                    if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                        logger.debug("Calling context.attempted()");
                        context.attempted();
                    } else if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                        challenge = context.form()
                                .setError("sms-auth.code.invalid")
                                .setAttribute("mobile_number", mobileNumber.orElse(null))
                                .setAttribute("code_digits", provider.getCodeDigits(context.getSession(), user))
                                .createForm("sms-validation.ftl");
                        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                    } else {
                        // Something strange happened
                        logger.warn("Undefined execution ...");
                    }
                    break;

                case VALID:
                    context.success();
                    UserProfile.addVerifiedNumberAndUpdateActions(
                            context.getUser(),
                            UserProfile.getMobileNumber(context.getUser(), false).get()
                    );
                    break;

            }
        }
    }

    @Override
    public boolean requiresUser() {
        logger.debug("requiresUser called ... returning true");
        return true;
    }
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }
    @Override
    public void close() {
        logger.debug("close called ...");
    }
}
