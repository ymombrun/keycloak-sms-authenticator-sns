package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;

/**
 * Created by nickpack on 15/08/2017.
 */
public class SmsCodeExpirationTimeCredentialProvider extends SmsCredentialProvider {
    public static final String SMS_AUTH_EXP_TIME = "sms-auth.exp-time";
    public static final String CACHE_KEY = SmsCodeExpirationTimeCredentialProvider.class.getName() + "." + SMS_AUTH_EXP_TIME;

    public SmsCodeExpirationTimeCredentialProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    public String getType() {
        return SMS_AUTH_EXP_TIME;
    }
}
