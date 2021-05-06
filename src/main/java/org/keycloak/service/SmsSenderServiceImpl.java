package org.keycloak.service;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.SmsCredentialProvider;
import org.keycloak.gateway.Gateways;
import org.keycloak.gateway.SMSGateway;
import org.keycloak.gateway.isendpro.ISendProdSMSService;
import org.keycloak.gateway.lyrasms.LyraSMSService;
import org.keycloak.gateway.snsclient.SnsNotificationService;
import org.keycloak.models.*;
import org.keycloak.theme.Theme;
import org.keycloak.util.Configuration;
import org.keycloak.util.Sms;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class SmsSenderServiceImpl implements SmsSenderService {

    public static final String SMS_CODE_FORM_FIELD = "smsCode";

    private static Logger logger = Logger.getLogger(SmsSenderServiceImpl.class);

    private Config.Scope config;

    @Override
    public boolean sendSmsCode(String mobileNumber, AuthenticationFlowContext context) {

        int nrOfDigits = Configuration.getConfigInt(context.getAuthenticatorConfig(), Configuration.CONF_PRP_SMS_CODE_LENGTH, 4);
        logger.debug("Using nrOfDigits " + nrOfDigits);

        long ttl = Configuration.getConfigLong(context.getAuthenticatorConfig(), Configuration.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s
        logger.info("Using ttl " + ttl + " (s)");

        boolean isAlphaNumeric = Configuration.getConfigBoolean(context.getAuthenticatorConfig(), Configuration.CONF_PRP_SMS_CODE_ALPHANUMERIC, true); // 10 minutes in s
        logger.debug("Using alphanumeric " + isAlphaNumeric);

        String code = generateSmsCode(nrOfDigits, isAlphaNumeric);
        storeSMSCode(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), code, new Date().getTime() + (ttl * 1000));

        Theme theme = null;
        Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
        try {
            theme = context.getSession().theme().getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
        } catch (Exception e) {
            logger.error("Unable to get theme required to send SMS", e);
        }

        return sendSmsCodeToGateway(mobileNumber, code, theme, locale);
    }

    @Override
    public boolean sendSmsCode(String mobileNumber, RequiredActionContext context) {

        // sharing conf with authenticator
        // used when the authenticator alias is the same as to realm name
        logger.debugf("Searching for authenticator config with alias %s", context.getRealm().getName());
        var authenticatorConfig = context.getRealm().getAuthenticatorConfigByAlias(context.getRealm().getDisplayName());
        logger.debugf("Using authenticator config %s", authenticatorConfig);

        int nrOfDigits = Configuration.getConfigInt(authenticatorConfig, Configuration.CONF_PRP_SMS_CODE_LENGTH, 4);
        logger.debug("Using nrOfDigits " + nrOfDigits);

        long ttl = Configuration.getConfigLong(authenticatorConfig, Configuration.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s
        logger.debugf("Using ttl " + ttl + " (s)");

        boolean isAlphaNumeric = Configuration.getConfigBoolean(authenticatorConfig, Configuration.CONF_PRP_SMS_CODE_ALPHANUMERIC);
        logger.debug("Using alphanumeric " + isAlphaNumeric);

        String code = generateSmsCode(nrOfDigits, isAlphaNumeric);
        storeSMSCode(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), code, new Date().getTime() + (ttl * 1000));

        Theme theme = null;
        Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
        try {
            theme = context.getSession().theme().getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
        } catch (Exception e) {
            logger.error("Unable to get theme required to send SMS", e);
        }

        return sendSmsCodeToGateway(mobileNumber, code, theme, locale);
    }

    public List getCodeDigits(KeycloakSession session, UserModel user) {
        return session.userCredentialManager().getStoredCredentialsByTypeStream(session.getContext().getRealm(), user, SmsCredentialProvider.USR_CRED_MDL_SMS_CODE)
                .map(CredentialModel::getSecretData)
                .findFirst()
                .map(code -> IntStream.rangeClosed(1, code.length()).boxed().collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public CODE_STATUS validateCode(RequiredActionContext context) {
        logger.debug("validateCode called ... ");
        return validateCode(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), context.getHttpRequest());
    }

    @Override
    public CODE_STATUS validateCode(AuthenticationFlowContext context) {
        logger.debug("validateCode called ... ");
        return validateCode(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), context.getHttpRequest());
    }

    @Override
    public void setConfig(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void close() {

    }

    private String generateSmsCode(int length, boolean isAlphaNumeric) {
        if (length < 1) {
            throw new RuntimeException("Number of digits must be bigger than 0");
        }
        return isAlphaNumeric ? RandomStringUtils.randomAlphanumeric(length) : RandomStringUtils.randomNumeric(length);
    }

    private boolean sendSmsCodeToGateway(String mobileNumber, String code, Theme theme, Locale locale) {
        // Send an SMS
        String smsUsr = config.get(Configuration.CONF_PRP_SMS_CLIENT_TOKEN);
        String smsPwd = config.get(Configuration.CONF_PRP_SMS_CLIENT_SECRET);
        String smsFrom = config.get(Configuration.CONF_PRP_SMS_FROM);
        String gateway = config.get(Configuration.CONF_PRP_SMS_GATEWAY);
        String endpoint = config.get(Configuration.CONF_PRP_SMS_GATEWAY_ENDPOINT);
        boolean isProxy = Boolean.getBoolean(config.get(Configuration.PROXY_ENABLED));

        String template = Sms.getMessage(theme, locale, Configuration.CONF_PRP_SMS_TEXT);
        String smsText = Sms.createMessage(template, code, mobileNumber);

        logger.debug("Sending " + code + "  to mobileNumber " + mobileNumber);
        logger.debug("Using config : "+gateway+" - "+smsPwd+" - " +endpoint);

        boolean result;
        SMSGateway smsService;
        try {
            Gateways g = Gateways.valueOf(gateway);
            switch(g) {
                case LYRA_SMS:
                    smsService = new LyraSMSService(endpoint,isProxy);
                    break;
                case ISENDPRO_SMS:
                    smsService = new ISendProdSMSService(endpoint, isProxy, smsFrom);
                    break;
                case AMAZON_SNS:
                    smsService = new SnsNotificationService();
                    break;
                default:
                    smsService = new SnsNotificationService();
            }

            result = smsService.send(
                    Sms.checkAndFormatMobileNumber(mobileNumber),
                    smsText,
                    smsUsr,
                    smsPwd
            );
            return result;
        } catch(Exception e) {
            logger.warn("Fail to send SMS : " + e.getLocalizedMessage());
            return false;
        }
    }

    private void storeSMSCode(UserCredentialManager credentialManager, RealmModel realm, UserModel user, String code, Long expiringAt) {
        UserCredentialModel credentials = new UserCredentialModel();
        credentials.setType(SmsCredentialProvider.USR_CRED_MDL_SMS_CODE);
        credentials.setValue(code);
        credentialManager.updateCredential(realm, user, credentials);
        credentials.setType(SmsCredentialProvider.USR_CRED_MDL_SMS_EXP_TIME);
        credentials.setValue((expiringAt).toString());
        credentialManager.updateCredential(realm, user, credentials);
    }

    private CODE_STATUS validateCode(UserCredentialManager credentialManager, RealmModel realm, UserModel user, HttpRequest request) {
        MultivaluedMap<String, String> formData = request.getDecodedFormParameters();
        String enteredCode = formData.getFirst(SMS_CODE_FORM_FIELD);
        CODE_STATUS result = CODE_STATUS.INVALID;

        var count = credentialManager.getStoredCredentialsByTypeStream(realm, user, SmsCredentialProvider.USR_CRED_MDL_SMS_CODE).count();

        if (count != 1) {
            logger.warnf("Invalid code count in the credential store : %d", count);
        }
        var expectedCode = credentialManager.getStoredCredentialsByTypeStream(realm, user, SmsCredentialProvider.USR_CRED_MDL_SMS_CODE)
                .map(CredentialModel::getSecretData)
                .findFirst();
        var expTimeString = credentialManager.getStoredCredentialsByTypeStream(realm, user, SmsCredentialProvider.USR_CRED_MDL_SMS_EXP_TIME)
                .map(CredentialModel::getSecretData)
                .findFirst();

        if (expectedCode.isPresent()) {
            result = enteredCode.equals(expectedCode.get()) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            long now = new Date().getTime();

            logger.debug("Valid code expires in " + (Long.parseLong(expTimeString.get()) - now) + " ms");
            if (result == CODE_STATUS.VALID) {
                if (expTimeString.isPresent() && Long.parseLong(expTimeString.get()) < now) {
                    logger.warn("Code is expired !!");
                    result = CODE_STATUS.EXPIRED;
                }
            }
            if (result.equals(CODE_STATUS.INVALID)) {
                logger.debugf("Expected code %s but was %s", expectedCode.get(), enteredCode);
            }
        }
        logger.debug("result : " + result);
        return result;
    }
}
