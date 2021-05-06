package org.keycloak.util;

import org.jboss.logging.Logger;
import org.keycloak.models.AuthenticatorConfigModel;

/**
 * Created by joris on 18/11/2016.
 */
public class Configuration {

    public static final String CONF_PRP_SMS_CLIENT_SECRET = "sms-auth.sms.clientsecret";

    // User/Credential
    public static final String CONF_PRP_SMS_CLIENT_TOKEN = "sms-auth.sms.clienttoken";

    public static final String CONF_PRP_SMS_CODE_ALPHANUMERIC = "sms-auth.code.alphanumeric";

    public static final String CONF_PRP_SMS_CODE_LENGTH = "sms-auth.code.length";

    public static final String CONF_PRP_SMS_CODE_TTL = "sms-auth.code.ttl";

    public static final String CONF_PRP_SMS_FROM = "sms-auth.sms.gateway.from";

    // Gateway
    public static final String CONF_PRP_SMS_GATEWAY = "sms-auth.sms.gateway";

    public static final String CONF_PRP_SMS_GATEWAY_ENDPOINT = "sms-auth.sms.gateway.endpoint";

    public static final String CONF_PRP_SMS_TEXT = "sms-auth.msg.text";

    // Verification
    public static final String MOBILE_VERIFICATION_ENABLED = "mobile_verification_enabled";

    public static final String MOBILE_ASK_FOR_ENABLED = "mobile_ask_for_enabled";

    public static final String CHECK_NUMBER_ALREADY_EXISTS = "check_number_already_exists";

    // Proxy
    public static final String PROXY_ENABLED = "proxy_enabled";

    public static final String PROXY_HOST= "proxy_host";

    public static final String PROXY_PORT= "proxy_port";

    private static Logger logger = Logger.getLogger(Configuration.class);

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName, Long defaultValue) {

        Long value = defaultValue;

        if (config != null && config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Long.valueOf((String) obj);
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a number.");
            }
        }
        return value;
    }

    public static int getConfigInt(AuthenticatorConfigModel config, String configName, int defaultValue) {

        int value = defaultValue;

        if (config != null && config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Integer.valueOf((String) obj);
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a number.");
            }
        }
        return value;
    }

    public static Boolean getConfigBoolean(AuthenticatorConfigModel config, String configName) {
        return getConfigBoolean(config, configName, true);
    }

    public static Boolean getConfigBoolean(AuthenticatorConfigModel config, String configName, Boolean defaultValue) {

        Boolean value = defaultValue;

        if (config != null && config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Boolean.valueOf((String) obj);
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a boolean.");
            }
        }
        return value;
    }
}
