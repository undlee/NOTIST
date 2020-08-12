package org.techtown.direcord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class Http extends Thread {

    private String url;
    private String method = "GET";

    private int responseCode;
    private StringBuffer response = new StringBuffer();

    public static final String USER_AGENT = "Mozilla/5.0";

    public Http(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // 전송방식
            con.setRequestMethod(method);
            // Request Header 정의
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setConnectTimeout(10000); // 컨텍션타임아웃 10초
            con.setReadTimeout(5000); // 컨텐츠조회 타임아웃 5총

            responseCode = con.getResponseCode();

            Charset charset = Charset.forName("UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeGet() throws InterruptedException {
        method = "GET";
        start();
        join();
    }

    public void executePost() throws InterruptedException {
        method = "POST";
        start();
        join();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response.toString();
    }

}
