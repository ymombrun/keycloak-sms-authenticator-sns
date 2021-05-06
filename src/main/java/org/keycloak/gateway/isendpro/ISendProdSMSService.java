package org.keycloak.gateway.isendpro;

import org.keycloak.gateway.SMSGateway;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.util.Configuration;

import java.util.Optional;
import java.util.UUID;

/**
 * ISendProdSMS Service implementation
 */
public class ISendProdSMSService implements SMSGateway {

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
        String portTemp = Optional.ofNullable(System.getProperty("http." + Configuration.PROXY_PORT))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + Configuration.PROXY_PORT));

        final String host = Optional.ofNullable(System.getProperty("http." + Configuration.PROXY_HOST))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + Configuration.PROXY_HOST));
        final int port = portTemp != null ? Integer.valueOf(portTemp) : 8080;
        final String scheme = System.getProperty("http." + Configuration.PROXY_HOST) != null ? "http" : "https";

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
        boolean result = false;
//        if (phoneNumber != null) {
//            //Support only this format 3367...
//            phoneNumber = phoneNumber.replace("+", "");
//        }
        SmsRequest sms = new SmsRequest()
                .setKey(pw)
                .setPhone(phoneNumber)
                .setMessage(message)
                .setTracker(UUID.randomUUID())
                .setFrom(emetteur)
                .setNoStop(1);
        try {
            String resultM = this.remoteService.send(sms);
            result = resultM.indexOf("\"code\": \"0\"") > -1;

            if (!result) {
                logger.warn("Fail to send SMS by ISendPro: " + resultM);
            }
        } catch (Exception e) {
            logger.error("Fail to send SMS to ISendPro " + sms.toString() + " :" +e.getMessage());
        }
        return result;
    }
}
