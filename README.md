# sms-authenticator-validator

A keycloak authenticator and mobile phone number validator inspired from [keycloak-sms-authenticator-sns](https://github.com/makeandship/keycloak-sms-authenticator-sns).

Differences from the original project :

* The phone number required action allow check presence of the mobile_number user profile attribute
* The phone number validation required action allow mobile_number to be validated with sms code
* New Keycloak sms-sender SPI used in both authenticator and required actions + refactoring
* Simplify phone number form
* Add ISENDPRO gateway

To install the SMS Authenticator one has to:

* Add the jar to the Keycloak server:
  * `$ cp target/sms-authenticator-validator-1.0-*.jar _KEYCLOAK_HOME_/providers/`

You can also use jboss-cli commande :

    `/opt/jboss/keycloak/bin/jboss-cli.sh --command=" \
               module add \
                       --name=com.google.libphonenumber \
                       --resources=libphonenumber-8.9.0.jar
       "
       
       /opt/jboss/keycloak/bin/jboss-cli.sh --command=" \
               module add \
                       --name=commons-lang.commons-lang \
                       --resources=commons-lang-2.6.jar
       "
       
       /opt/jboss/keycloak/bin/jboss-cli.sh --command=" \
               module add \
                       --name=org.keycloak.sms \
                       --resources=sms-authenticator-validator-1.0.jar \
                       --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-common,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.jboss.logging,com.google.libphonenumber,javax.ws.rs.api,org.jboss.resteasy.resteasy-jaxrs,commons-lang.commons-lang
       "`

* Update sms-sender provider config in standalone.xml

   ```
   <subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
        ...
        <spi name="sms-sender">
            <provider name="sms-sender-service" enabled="true">
                <properties>
                    <property name="sms-auth.sms.gateway" value="ISENDPRO_SMS"/>
                    <property name="sms-auth.sms.gateway.endpoint" value="https://apirest.isendpro.com/cgi-bin"/>
                    <property name="sms-auth.sms.gateway.from" value="MyCompany"/>
                    <property name="sms-auth.sms.clientsecret" value="..."/>
                    <property name="sms-auth.code.ttl" value="600"/>
                    <property name="sms-auth.code.length" value="4"/>
                    <property name="sms-auth.code.alphanumeric" value="true"/>
                    <property name="mobile_verification_enabled" value="true"/>
                </properties>
            </provider>
        </spi>
    </subsystem> 
    ```

* Add three templates to the Keycloak server:
  * `$ cp templates/sms-validation.ftl _KEYCLOAK_HOME_/themes/base/login/`
  * `$ cp templates/sms-validation-error.ftl _KEYCLOAK_HOME_/themes/base/login/`
  * `$ cp templates/sms-validation-mobile-number.ftl _KEYCLOAK_HOME_/themes/base/login/`


Configure your REALM to use the SMS Authentication.
First create a new REALM (or select a previously created REALM).

Under Authentication > Flows:
* Copy 'Browse' flow to 'Browser with SMS' flow
* Click on 'Actions > Add execution on the 'Browser with SMS Forms' line and add the 'SMS Authentication'
* Set 'SMS Authentication' to 'REQUIRED' or 'ALTERNATIVE'
* To configure the SMS Authenticator, click on Actions  Config and fill in the attributes.

Under Authentication > Bindings:
* Select 'Browser with SMS' as the 'Browser Flow' for the REALM.

Under Authentication > Required Actions:
* Click on Register and select 'SMS Authentication' to add the Required Action to the REALM.
* Make sure that for the 'SMS Authentication' both the 'Enabled' and 'Default Action' check boxes are checked.
* Click on Register and select 'Mobile Number' to add the Required Action to the REALM.
* Make sure that for the 'Update Mobile Number' both the 'Enabled' and 'Default Action' check boxes are checked.
* To validate mobile_number attribute make sure that for the 'Validate Mobile Number' both the 'Enabled' and 'Default Action' check boxes are checked.

Malys contributions (for [Lyra Network](https://www.lyra-network.com/))
* Internationalization support
* Vault, Java properties, environment variables parameters support
* Lyrasms gateway support
* Add mobilephone number verification
* Add input mobile phone number on authenticator
* Refactoring
* Template cleaning
* Documentation
