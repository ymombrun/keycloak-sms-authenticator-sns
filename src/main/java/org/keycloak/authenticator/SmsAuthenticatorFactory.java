package org.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.gateway.Gateways;
import org.keycloak.util.Configuration;
//import six.six.gateway.Gateways;
//import six.six.keycloak.KeycloakSmsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SMS validation Input
 * Created by joris on 11/11/2016.
 */
public class SmsAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "sms-authentication";

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static Logger logger = Logger.getLogger(SmsAuthenticatorFactory.class);
    private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();
    private static final List<ProviderConfigProperty> configProperties = new ArrayList();

    static {
        ProviderConfigProperty property;

        // SMS Code
        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_CODE_TTL);
        property.setLabel("SMS code time to live");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The validity of the sent code in seconds.");
        property.setDefaultValue(60*5);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_CODE_LENGTH);
        property.setLabel("Length of the SMS code");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Length of the SMS code.");
        property.setDefaultValue(6);
        configProperties.add(property);

        // SMS gateway
        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_GATEWAY);
        property.setLabel("SMS gateway");
        property.setHelpText("Select SMS gateway");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setDefaultValue(Gateways.ISENDPRO_SMS);
        property.setOptions(Stream.of(Gateways.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        configProperties.add(property);

        // SMS Endpoint
        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_GATEWAY_ENDPOINT);
        property.setLabel("SMS endpoint");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Not useful for AWS SNS.");
        configProperties.add(property);

        // Credential
        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_CLIENT_TOKEN);
        property.setLabel("Client id");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("AWS Client Token or LyraSMS User");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_CLIENT_SECRET);
        property.setLabel("Client secret");
        property.setHelpText("ISendPro api key");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(Configuration.CONF_PRP_SMS_FROM);
        property.setLabel("From number");
        property.setHelpText("ISendPro emetteur");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);

        // Proxy
        property = new ProviderConfigProperty();
        property.setName(Configuration.PROXY_ENABLED);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setLabel("Use Proxy");
        property.setHelpText("Add Java Properties: http(s).proxyHost,http(s).proxyPort");
        configProperties.add(property);

        //First time verification
        property = new ProviderConfigProperty();
        property.setName(Configuration.MOBILE_VERIFICATION_ENABLED);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setLabel("Verify mobilephone\nnumber ONLY");
        property.setHelpText("Send SMS code ONLY to verify mobile number (add or update)");
        configProperties.add(property);

        //Ask for mobile if not defined
        property = new ProviderConfigProperty();
        property.setName(Configuration.MOBILE_ASK_FOR_ENABLED);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setLabel("Ask for mobile number");
        property.setHelpText("Enable access and ask for mobilenumber if it isn't defined");
        configProperties.add(property);
    }

    @Override
    public String getId() {
        logger.debug("getId called ... returning " + PROVIDER_ID);
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        logger.debug("create called ... returning " + SINGLETON);
        return SINGLETON;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        logger.debug("getRequirementChoices called ... returning " + REQUIREMENT_CHOICES);
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        logger.debug("isUserSetupAllowed called ... returning true");
        return true;
    }

    @Override
    public boolean isConfigurable() {
        logger.debug("isConfigurable called ... returning true");
        return true;
    }

    @Override
    public String getHelpText() {
        logger.debug("getHelpText called ...");
        return "Validates an OTP sent by SMS.";
    }

    @Override
    public String getDisplayType() {
        String result = "SMS Authentication";
        logger.debug("getDisplayType called ... returning " + result);
        return result;
    }

    @Override
    public String getReferenceCategory() {
        logger.debug("getReferenceCategory called ... returning sms-auth-code");
        return "sms-auth-code";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        logger.debug("getConfigProperties called ... returning " + configProperties);
        return configProperties;
    }

    @Override
    public void init(Config.Scope config) {
        logger.debug("init called ... config.scope = " + config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ... factory = " + factory);
    }

    @Override
    public void close() {
        logger.debug("close called ...");
    }
}
