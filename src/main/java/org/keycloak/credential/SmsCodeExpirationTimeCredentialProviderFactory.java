package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class SmsCodeExpirationTimeCredentialProviderFactory implements CredentialProviderFactory<SmsCodeExpirationTimeCredentialProvider> {

    public static final String PROVIDER_ID =  "sms-code-expiration-time";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new SmsCodeExpirationTimeCredentialProvider(session);
    }
}
