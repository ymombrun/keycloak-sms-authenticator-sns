package org.keycloak.sms.impl.gateway.isendpro;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


/**
 * ISENDPRO service description
 */

@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.TEXT_HTML})
public interface ISendProSMSRestService {
    @GET
    @Path("/sms")
    String send(
            @QueryParam("keyid") String password,
            @QueryParam("num") String phonenumber,
            @QueryParam("sms") String message,
            @QueryParam("tracker") String smsHttpId,
            @QueryParam("emetteur") String from,
            @QueryParam("nostop") String noStop
    );
}

