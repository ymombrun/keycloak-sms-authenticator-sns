package org.keycloak.sms.impl.gateway.isendpro;

import org.keycloak.sms.KeycloakSmsConstants;
import org.keycloak.sms.impl.gateway.SMSService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.util.Optional;
import java.util.UUID;

/**
 * ISendProdSMS Service implementation
 */
public class ISendProdSMSService implements SMSService {

    private static Logger logger = Logger.getLogger(ISendProdSMSService.class);


    private String emetteur;

    private String url;
    private ISendProSMSRestService remoteService;

    public ISendProdSMSService(String url, Boolean proxyOn, String emetteur) {
        this.url = url;
        this.remoteService = buildClient(url, proxyOn);
        this.emetteur = emetteur;
    }

    private static ISendProSMSRestService buildClient(String uri, Boolean proxyOn) {
        String portTemp = Optional.ofNullable(System.getProperty("http." + KeycloakSmsConstants.PROXY_PORT))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + KeycloakSmsConstants.PROXY_PORT));

        final String host = Optional.ofNullable(System.getProperty("http." + KeycloakSmsConstants.PROXY_HOST))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + KeycloakSmsConstants.PROXY_HOST));
        final int port = portTemp != null ? Integer.valueOf(portTemp) : 8080;
        final String scheme = System.getProperty("http." + KeycloakSmsConstants.PROXY_HOST) != null ? "http" : "https";

        ResteasyClientBuilder builder = new ResteasyClientBuilder();

        if (proxyOn) {
            builder.defaultProxy(host, port, scheme);
        }

        ResteasyClient client = builder.disableTrustManager().build();
        ResteasyWebTarget target = client.target(uri);

        return target
                .proxyBuilder(ISendProSMSRestService.class)
                .classloader(ISendProSMSRestService.class.getClassLoader())
                .build();

    }

    public boolean send(String phoneNumber, String message, String login, String pw) {
        boolean result;
//        if (phoneNumber != null) {
//            //Support only this format 3367...
//            phoneNumber = phoneNumber.replace("+", "");
//        }

        String resultM = this.remoteService.send(pw, phoneNumber, message, UUID.randomUUID().toString(), emetteur, "1");
        result = resultM.indexOf("\"code\": \"0\"") > -1;

        if (!result) {
            logger.error("Fail to send SMS by ISendPro: " + resultM );
        }
        return result;
    }
}
