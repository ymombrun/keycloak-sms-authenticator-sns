import org.junit.Test;
import org.keycloak.sms.impl.gateway.isendpro.ISendProdSMSService;

public class TestSms {
    @Test
    public void sendSms() {
        ISendProdSMSService service= new ISendProdSMSService("https://apirest.isendpro.com/cgi-bin", false, "Me");
        var res = service.send("a number", "Hello", "", "a key");
        System.out.println(res);
    }
}
