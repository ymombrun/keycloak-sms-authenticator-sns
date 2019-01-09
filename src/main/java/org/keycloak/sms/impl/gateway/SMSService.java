package org.keycloak.sms.impl.gateway;

/**
 * SMS provider interface
 */
public interface SMSService {
    boolean send(String phoneNumber, String message, String login, String pw);
}
