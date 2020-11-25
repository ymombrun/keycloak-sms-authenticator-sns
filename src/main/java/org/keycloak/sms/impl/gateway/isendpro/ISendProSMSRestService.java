package org.keycloak.sms.impl.gateway.isendpro;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


/**
 * ISENDPRO service description
 */

@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON})
public interface ISendProSMSRestService {
    @POST
    @Path("/sms")
    String send(SmsRequest smsRequest);
}

