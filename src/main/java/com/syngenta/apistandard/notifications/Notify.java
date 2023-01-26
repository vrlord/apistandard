package com.syngenta.apistandard.notifications;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Notify {
    private final static Logger log = Logger.getLogger("com.syngenta.apistandar.notifications.Notify");
    public static void sendInsecuredTelegramNotif(String message) throws NoSuchAlgorithmException, KeyManagementException { //%20 to spaces on message
        log.info("OKHTTP3 POST sendTelegramNotif function called with message: " + message);
        //trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
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
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
        newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        newBuilder.hostnameVerifier((hostname, session) -> true);

        String url = "https://noti.darwo.cl/syngentanotifica/msg/" + message;
        OkHttpClient client = newBuilder.build(); //new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualData = new JSONObject();
        try{
            actualData.put("name", "darwin");
            actualData.put("age", 33);
        } catch (JSONException e){
            log.warning("JSON Exception");
            e.printStackTrace();
        }
        okhttp3.RequestBody body = okhttp3.RequestBody.create(actualData.toString(), JSON);
        log.info("OKHTTP3 Request Body created.");
        Request newReq = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(newReq).execute();
            log.info("OKHTTP3 Request Done, got the response");
            log.info("OKHTTP3 Response: " + response.body().string());
        } catch(IOException e){
            log.info("OKHTTP3 Exception while doing request");
            e.printStackTrace();
        }

    }

    public void sendTelegramNotif(String message){ //%20 to spaces on message
        log.info("OKHTTP3 POST sendTelegramNotif function called with message: " + message);
        String url = "https://noti.darwo.cl/syngentanotifica/msg/" + message;
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualData = new JSONObject();
        try{
            actualData.put("name", "darwin");
            actualData.put("age", 33);
        } catch (JSONException e){
            log.warning("JSON Exception");
            e.printStackTrace();
        }
        okhttp3.RequestBody body = okhttp3.RequestBody.create(actualData.toString(), JSON);
        log.info("OKHTTP3 Request Body created.");
        Request newReq = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(newReq).execute();
            log.info("OKHTTP3 Request Done, got the response");
            log.info("OKHTTP3 Response: " + response.body().string());
        } catch(IOException e){
            log.info("OKHTTP3 Exception while doing request");
            e.printStackTrace();
        }
    }

}
