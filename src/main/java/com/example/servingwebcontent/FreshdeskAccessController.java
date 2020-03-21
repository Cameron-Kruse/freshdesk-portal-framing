package com.example.servingwebcontent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
* Sample SSO configuration for use with in-site portal iframing.
* */

@Controller
public class FreshdeskAccessController{

    private static Log log = LogFactory.getLog(FreshdeskAccessController.class);
    private static String FRESHDESK_LOGIN_URL;
    private static String BASE_FRESHDESK_URL = "https://myhelpdesk.freshdesk.com"; // N.B: Replace with own base url
    private static String SHARED_KEY = "THIS_IS_YOUR_SIMPLE_SSO_SHARED_KEY";  // N.B: Replace with own shared key
    private final static String TEST_CONTACT_NAME = "John1234";
    private final static String TEST_CONTACT_EMAIL = "john1234@mail.com";


    public FreshdeskAccessController(){
        FRESHDESK_LOGIN_URL = (BASE_FRESHDESK_URL == null ? "" : BASE_FRESHDESK_URL) + "/login/sso";
    }

    /*
     *  Configured on Freshdesk as Simple SSO with login url as:
     *   {{baseurl}}/ssofreshdesk.ihtml e.g. http://localhost:8080/ssofreshdesk.ihtml
     */
    @RequestMapping("/**/ssofreshdesk.ihtml")
    public RedirectView ssofreshdesk(){
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(generateSSOLoginUrl(TEST_CONTACT_NAME, TEST_CONTACT_EMAIL));
        return redirectView;
    }

    private String generateSSOLoginUrl(String name, String email) {
        String hash;
        String redirectUrl = null;
        long timeInSeconds = System.currentTimeMillis()/1000;
        
        try {
            hash = getHMACHash(name,email,timeInSeconds);
            redirectUrl = FRESHDESK_LOGIN_URL + "?name="+name+"&email="+email+"&timestamp="+timeInSeconds+"&hash=" + hash;
        } catch (Exception e) {
            log.error(e);
        }
        return redirectUrl;
    }

    private static String hashToHexString(byte[] byteData)
    {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private static String getHMACHash(String name,String email,long timeInMillis) throws Exception {
        byte[] keyBytes = SHARED_KEY.getBytes();
        String movingFact =name+SHARED_KEY+email+timeInMillis;
        byte[] text = movingFact.getBytes();
        
        String hexString = "";
        Mac hmacMD5;
        try {
            hmacMD5 = Mac.getInstance("HmacMD5");
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmacMD5.init(macKey);
            byte[] hash =  hmacMD5.doFinal(text);
            hexString = hashToHexString(hash);
            
        } catch (Exception nsae) {
            System.out.println("Caught the exception");
        }
        return hexString;
    }
}
