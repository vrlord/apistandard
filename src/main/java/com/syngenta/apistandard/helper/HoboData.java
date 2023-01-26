package com.syngenta.apistandard.helper;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syngenta.apistandard.notifications.Notify;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.URI;
import java.io.IOException;

public class HoboData {
    //trust all certificates
    static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }
                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };
    private final static Logger log = Logger.getLogger("com.syngenta.apistandard.helper.HoboData");

    private static String getToken() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        log.info("Authenticating, please wait");

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", "Henry_WS");
        formData.put("client_secret", "de235eae211c94c7d3a09aa96688c2b03f7af8bf");

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://webservice.hobolink.com/ws/auth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method("POST", HttpRequest.BodyPublishers.ofString(Util.getFormDataAsString(formData)))
                .build();
        //HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());
        if(response.statusCode() == 200){
            final JSONObject obj = new JSONObject(response.body());
            // log.info("Access Token: " + obj.getString("access_token"));
            // log.info("Token Type: " + obj.getString("token_type"));
            // log.info("Expires In: " + obj.getInt("expires_in"));
            log.info("Access Token: " + obj.getString("access_token"));


            //Response res = new Response();

            return obj.getString("access_token");
        }

        return null;
    }

    private static String getInfo(String bearer, String logger, String date) throws IOException, InterruptedException, KeyManagementException, NoSuchAlgorithmException {
        Map<String, String> formData = new HashMap<>();
        formData.put("loggers", logger); //comma separated
        formData.put("start_date_time", date + " 00:00:00");
        formData.put("end_date_time", date + " 23:59:59");

        //System.out.println("URI: " + URI.create("https://webservice.hobolink.com/ws/data/file/json/user/8933" + Util.convertMaptoQueryString(formData)));

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create("https://webservice.hobolink.com/ws/data/file/json/user/8933?loggers=21317982&start_date_time=2022-12-19%2000%3A00%3A00&end_date_time=2022-12-19%2023%3A59%3A59"))
                .uri(URI.create("https://webservice.hobolink.com/ws/data/file/json/user/8933" + Util.convertMaptoQueryString(formData)))
                .header("Authorization", "Bearer " + bearer)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        System.out.println("Getting data from server");
        //HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());
        System.out.println(response.statusCode());
        if(response.statusCode() == 200){
            final JSONObject obj = new JSONObject(response.body());
            final JSONArray jsonArray = obj.getJSONArray("observation_list");

            AtomicInteger totalSavedRecords = new AtomicInteger();
            AtomicInteger totalErrorRecords = new AtomicInteger();


            String url = "jdbc:postgresql://localhost/syngenta?user=darwo&password=darwosyngenta&ssl=false";
            try{
                Connection conn = DriverManager.getConnection(url);
                log.info("Connected to DB, Saving " + jsonArray.length() + " records.");

                jsonArray.forEach(e ->{
                    JSONObject objLogger = (JSONObject) e;
                    String logger_sn = objLogger.getString("logger_sn");
                    String sensor_sn = objLogger.getString("sensor_sn");
                    String timestamp = objLogger.getString("timestamp");
                    String data_type_id = objLogger.getString("data_type_id");
                    Double si_value = objLogger.getDouble("si_value");
                    String si_unit = objLogger.getString("si_unit");
                    Double us_value = objLogger.getDouble("us_value");
                    String us_unit = objLogger.getString("us_unit");
                    Double scaled_value = objLogger.getDouble("scaled_value");
                    String scaled_unit = objLogger.optString("scaled_unit", null);
                    Integer sensor_key = objLogger.getInt("sensor_key");
                    String sensor_measurement_type = objLogger.getString("sensor_measurement_type");


                    // System.out.println("logger_sn: " + logger_sn + ", sensor_sn: " + sensor_sn + ", timestamp: " + timestamp
                    // + ", data_type_id: " + data_type_id + ", si_value: " + si_value + ", si_unit: " + si_unit + "us_value: " + us_value
                    // + ", us_unit: " + us_unit + ", scaled_value: " + scaled_value + ", scaled_unit: " + scaled_unit
                    // + ", sensor_key: " + sensor_key + ", sensor_measurement_type: " + sensor_measurement_type);
                    // System.out.println("");
                    // System.out.println("");

                    //System.out.print("logger_sn: " + logger_sn + ", sensor_sn: " + sensor_sn + "; => ");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'Z'");
                    LocalDateTime newTimeStamp = LocalDateTime.parse(timestamp, formatter);


                    try{
                        LocalDate localDate = LocalDate.now();
                        LocalDateTime localDateTime = LocalDateTime.now();
                        PreparedStatement st = conn.prepareStatement("INSERT INTO sensors.sensors (logger_sn, sensor_sn, timestamp, data_type_id, si_value, si_unit, us_value, us_unit, scaled_value, scaled_unit, sensor_key, sensor_measurement_type, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

                        st.setObject(1, logger_sn);
                        st.setObject(2, sensor_sn);
                        st.setObject(3, newTimeStamp);
                        st.setObject(4, data_type_id);
                        st.setObject(5, si_value);
                        st.setObject(6, si_unit);
                        st.setObject(7, us_value);
                        st.setObject(8, us_unit);
                        st.setObject(9, scaled_value);
                        st.setObject(10, scaled_unit);
                        st.setObject(11, sensor_key);
                        st.setObject(12, sensor_measurement_type);
                        st.setObject(13, localDateTime);
                        st.executeUpdate();
                        st.close();
                        totalSavedRecords.getAndIncrement();
                    }catch(SQLException sqle){
                        log.warning("Error saving record: " + sqle);
                        //notification err saving record (maybe send file?)
                        totalErrorRecords.getAndIncrement();
                    }

                });

                //System.out.println(" ");


            }catch(SQLException e){
                log.warning("Error connecting to DB: " + e);
                //notification error DB

                return "db_error";
            }
            String message = "Logger: " + logger + "%0ATotal Records: " + jsonArray.length() + "%0ASaved: " + totalSavedRecords + "%0AErrors: " + totalErrorRecords;
            log.info(message);
            Notify.sendInsecuredTelegramNotif(message);
            //send notification


            return "ok";


        }else if(response.statusCode() == 401){ //unauthorized (no token xd)
            return "notoken";
        }else if(response.statusCode() == 429){
            System.out.println("Too many requests, pausing for 10 seconds: " + response.statusCode());
            try{
                Thread.sleep(10000);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }

            return "too many requests";

        }else{
            System.out.println("Status: " + response.statusCode());
        }

        return "end";
    }

    private static String retrieveToken() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        String token = null;
        while(token == null){
            token = getToken();
        }
        return token;
    }


    public static void GetNewData(LocalDate fromdate, LocalDate todate, String loggersn) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {


        // loop of dates
        var startDate = fromdate; //LocalDate.parse("2022-12-26");
        var endDate  = todate; //LocalDate.parse("2022-12-27");
        String logger = loggersn; //"20758903";

        var today = LocalDate.now();
        String token = null;
        /*
            21317982 => Arriendo
            20993658 => Syngenta Arica, Sta Gema
            20758903 => Syngenta Arica´s Site
        */

        while(startDate.isBefore(endDate) || startDate.isEqual(endDate)){

            String message = "Logger: " + loggersn + "%0ADate Start: " + startDate + "%0ADate End: " + endDate;
            log.info(message);

            new Thread(() -> {
                try {
                    Notify.sendInsecuredTelegramNotif(message);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            String status = getInfo(token, logger, startDate.toString());
            while(status != "ok"){
                token = retrieveToken();
                status = getInfo(token, logger, startDate.toString());
            }




            startDate = startDate.plusDays(1);
        }

        //status 429 too many requests

    }


}
