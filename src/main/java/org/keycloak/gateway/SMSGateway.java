package org.keycloak.gateway;

/**
 * SMS provider interface
 */
public interface SMSGateway {
    boolean send(String phoneNumber, String message, String login, String pw);
}
