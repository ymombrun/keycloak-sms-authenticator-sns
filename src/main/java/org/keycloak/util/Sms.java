package org.keycloak.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.logging.Logger;
import org.keycloak.theme.Theme;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by joris on 18/11/2016.
 */
public class Sms {

    private static Logger logger = Logger.getLogger(Sms.class);

    /**
     * Check mobile number normative structure
     * @param mobileNumber
     * @return formatted mobile number
     */
    public static String checkAndFormatMobileNumber(String mobileNumber) {

        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phone = phoneUtil.parse(mobileNumber, "FR");

            if (!phoneUtil.getNumberType(phone).equals(PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                logger.error("Invalid mobile phone number (not a mobile)");
            }

            String formatted = phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

            logger.debug("formatted number " + formatted + " from " + mobileNumber);
            return formatted;
        } catch (Exception e) {
            logger.warnf("Invalid phone number %s", e.getLocalizedMessage());
            return null;
        }
    }

    public static String getMessage(Theme theme, Locale locale, String key){
        String result=null;
        try {
            result = theme.getMessages(locale).getProperty(key);
        }catch (IOException e){
            logger.warn(key + "not found in messages");
        }
        return StringEscapeUtils.unescapeJava(result);
    }

    public static String createMessage(String text,String code, String mobileNumber) {
        if(text !=null){
            text = text.replaceAll("%sms-code%", code);
            text = text.replaceAll("%phonenumber%", mobileNumber);
        }
        return StringEscapeUtils.unescapeJava(text);
    }
}
