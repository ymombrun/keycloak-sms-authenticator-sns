package org.keycloak.service;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import java.util.List;

public interface SmsSenderService extends Provider {

    enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }

    boolean sendSmsCode(String mobileNumber, AuthenticationFlowContext context);

    boolean sendSmsCode(String mobileNumber, RequiredActionContext context);

    List getCodeDigits(KeycloakSession session, UserModel user);

    CODE_STATUS validateCode(RequiredActionContext context);

    CODE_STATUS validateCode(AuthenticationFlowContext context);

//    void updateVerifiedMobileNumber(UserModel user);

//    boolean checkNumberAlreadyTaken(String mobileNumber, KeycloakSession session, UserModel user, RealmModel realm);

    void setConfig(Config.Scope config);
}
