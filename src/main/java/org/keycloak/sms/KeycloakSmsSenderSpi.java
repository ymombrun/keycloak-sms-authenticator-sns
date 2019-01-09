package org.keycloak.sms;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsSenderSpi implements Spi {


    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "sms-sender";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return KeycloakSmsSenderService.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return KeycloakSmsSenderProviderFactory.class;
    }
}
