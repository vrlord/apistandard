package com.syngenta.apistandard.helper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import java.time.Year;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;


/**
 * Util Class created by Darwo
 */
public class Util {
    private final static Logger log = Logger.getLogger("cl.darwo.rest.helper.Util");

    /**
     * Creates an integer from a String, returning an Integer (wrapper class)
     * Also it replace all characters leaving only number and if are not numbers
     * it return a null
     * @param string
     * @return Integer | null
     */
    public static Integer createIntegerFromString(String string){
        if(string == null || Objects.equals(string.replaceAll("[^0-9]", ""), ""))
            return null;

        return Integer.valueOf(string.replaceAll("[^0-9]", ""));
    }

    /**
     * @param newValue Receives a string with numeric type value separated by a .
     * @param oldValue Receives a BigDecimal value that can be null to use in case a conversion failure.
     * @return return a new BigDecimal or an Old Value if value can not be converted, it can return a null if oldValue is set to null
     */
    public static BigDecimal createBigDecimalFromString(String newValue, BigDecimal oldValue){

        if(newValue.equals(""))
            return null;

        try{
            return new BigDecimal(newValue.replaceAll("[^0-9.,]", ""));
        }catch(Exception e){
            log.warning("Error converting new BigDecimal: " + e);
        }

        return oldValue;
    }


    public static Date formatDate(String newValue, Date oldValue){
        if(Objects.equals(newValue, ""))
            return null;
        Date returnDate;
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        try {
            returnDate = df.parse(newValue);
        } catch (ParseException e) {
            //throw new RuntimeException(e);
            returnDate = oldValue;
        }
        return returnDate;
    }

    public static Year createYearFromString(String newValue, Year oldValue){
        if(Objects.equals(newValue, ""))
            return oldValue;

        try{
            return Year.parse(newValue);
        }catch(Exception e){
            log.warning("Error parsing Year: ");
        }

        return oldValue;
    }

    public static String getFormDataAsString(Map<String, String> formData) { //FORM DATA (POST)
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    public static String convertMaptoQueryString(Map<String,String> formData){ //QUERYSTRING (GET)
        StringBuilder sb = new StringBuilder("?");
        for(HashMap.Entry<String, String> e : formData.entrySet()){
            if(sb.length() > 1){
                sb.append('&');
            }
            try{
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            }catch(UnsupportedEncodingException encException){
                log.warning("Error encoding: " + encException);
            }
        }

        return sb.toString().replace("+", "%20");
    }



    public static String encode(String s){
        try{
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch(UnsupportedEncodingException e){
            throw new IllegalStateException(e);
        }
    }


    public static String convertMaptoQueryString2(Map<String,String> formData){
        String result = formData.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        return ("?" + result).replace("+", "%20");
    }

}
