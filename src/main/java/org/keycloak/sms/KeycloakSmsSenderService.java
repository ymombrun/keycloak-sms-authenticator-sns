package org.keycloak.sms;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface KeycloakSmsSenderService extends Provider {

    enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }

    boolean sendSmsCode(String mobileNumber, AuthenticationFlowContext context);

    boolean sendSmsCode(String mobileNumber, RequiredActionContext context);

    CODE_STATUS validateCode(RequiredActionContext context);

    CODE_STATUS validateCode(AuthenticationFlowContext context);

    void updateVerifiedMobilenumber(UserModel user);

    void setConfig(Config.Scope config);
}
