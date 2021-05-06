package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;

/**
 * SMS code Internal management
 * Created by nickpack on 09/08/2017.
 */
public class SmsCodeCredentialProviderFactory implements CredentialProviderFactory<SmsCodeCredentialProvider> {

    public static final String PROVIDER_ID =  "sms-auth-code";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new SmsCodeCredentialProvider(session);
    }
}
