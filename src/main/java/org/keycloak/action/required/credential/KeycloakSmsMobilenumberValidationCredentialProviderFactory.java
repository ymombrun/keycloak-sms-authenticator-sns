package org.keycloak.action.required.credential;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsMobilenumberValidationCredentialProviderFactory implements CredentialProviderFactory<KeycloakSmsMobilenumberCredentialProvider> {
    @Override
    public String getId() {
        return "mobile_number_validation";
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new KeycloakSmsMobilenumberValidationCredentialProvider(session);
    }

}
