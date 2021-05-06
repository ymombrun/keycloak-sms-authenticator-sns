package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;

/**
 * Created by nickpack on 09/08/2017.
 */
public class SmsCodeCredentialProvider extends SmsCredentialProvider {
    private static final String SMS_AUTH_CODE = "sms-auth.code";
    private static final String CACHE_KEY = SmsCodeCredentialProvider.class.getName() + "." + SMS_AUTH_CODE;

    public SmsCodeCredentialProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    public String getType() {
        return SMS_AUTH_CODE; // was password
    }
}
