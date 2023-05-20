package com.example.project_ee297.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 人脸比对 WebAPI 接口调用示例
 * 运行前：请先填写Appid、APIKey、APISecret以及图片路径
 * 运行方法：直接运行 main() 即可
 * 结果： 控制台输出结果信息
 * 接口文档（必看）：https://www.xfyun.cn/doc/face/xffaceComparisonRecg/API.html
 * @author iflytek
 */

public class WebFaceCompare {

    public static final Gson json = new Gson();
    public static String similarity(String imagePath1,String imagePath2){

        var face = new WebFaceCompare(
                "https://api.xf-yun.com/v1/private/s67c9c78c",
                "c42a8fc5",  //请填写控制台获取的APPID,
                "NDRkOGEwYWQwMzkwYTM2OTc2M2RmODYx",  //请填写控制台获取的APISecret
                "1389aa5d53e53ae7578609062dffe3b9",  //请填写控制台获取的APIKey
                imagePath1,//"C:\\Users\\Lenovo\\Pictures\\Screenshots\\kim.jpg",  //请填写要比对的第一张图片路径
                imagePath2,//"C:\\Users\\Lenovo\\Pictures\\Screenshots\\kim2.jpg",  //请填写要比对的第二张图片路径
                "s67c9c78c"
        );

    try {
            var resp =face.doRequest();
            System.out.println("接口返回结果："+resp);
            ResponseData respData = json.fromJson(resp, ResponseData.class);
            String textBase64 = "";
            if (respData.getPayLoad().getFaceCompareResult() != null) {
                textBase64 = respData.getPayLoad().getFaceCompareResult().getText();
                String text = new String(Base64.getDecoder().decode(textBase64));
                System.out.println("人脸比对结果(text)base64解码后：");
                System.out.println(text);
                return text;

            }
      } catch (Exception e) {
            e.printStackTrace();
        }
    return null;
    }


    private String requestUrl;
    private String apiSecret;
    private String apiKey;
    private String imagePath1;
    private String imagePath2;
    private String appid ;
    private String serviceId;

    public WebFaceCompare(String requestUrl, String appid, String apiSecret, String apiKey, String imagePath1, String imagePath2, String serviceId) {
        this.requestUrl = requestUrl;
        this.appid = appid;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.imagePath1 = imagePath1;
        this.imagePath2 = imagePath2;
        this.serviceId = serviceId;
    }

    //构建url
    public String buildRequetUrl(){
        return assembleRequestUrl(this.requestUrl,this.apiKey,this.apiSecret);
    }

    //读取image
    private byte[] readImage(String imagePath) throws IOException {
        InputStream is = new FileInputStream(imagePath);
        return is.readAllBytes();
    }

    //构建参数
    private String  buildParam() throws IOException {
        var req = new JsonObject();

        //平台参数
        var header = new JsonObject();
        header.addProperty("app_id",appid);
        header.addProperty("status",3);

        //功能参数
        var parameter = new JsonObject();
        var inputAcp = new JsonObject();
        var result = new JsonObject();
        inputAcp.addProperty("service_kind","face_compare");//face_compare:人脸1:1比对
        //构建face_detect_result段参数
        result.addProperty("encoding","utf8");
        result.addProperty("compress","raw");
        result.addProperty("format","json");
        inputAcp.add("face_compare_result",result);//face_detect_result
        parameter.add(this.serviceId,inputAcp);

        //请求数据
        var payload = new JsonObject();
        var payloadImage1 = new JsonObject();
        payloadImage1.addProperty("encoding","jpg"); //jpg:jpg格式,jpeg:jpeg格式,png:png格式,bmp:bmp格式
        payloadImage1.addProperty("status",3);   //3:一次性传完
        payloadImage1.addProperty("image", Base64.getEncoder().encodeToString(readImage(this.imagePath1))); //图像数据，base64
        payload.add("input1",payloadImage1);
        var payloadImage2 = new JsonObject();
        payloadImage2.addProperty("encoding","jpg"); //jpg:jpg格式,jpeg:jpeg格式,png:png格式,bmp:bmp格式
        payloadImage2.addProperty("status",3);   //3:一次性传完
        payloadImage2.addProperty("image", Base64.getEncoder().encodeToString(readImage(this.imagePath2))); //图像数据，base64
        payload.add("input2",payloadImage2);

        req.add("header",header);
        req.add("parameter",parameter);
        req.add("payload",payload);
        return req.toString();
    }

    private String makeRequest() throws Exception {
        String url = buildRequetUrl();
        System.out.println("url=>" + url);
        var realUrl = new URL(url);
        var connection = realUrl.openConnection();
        var httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-type","application/json");

        var out = httpURLConnection.getOutputStream();
        var params = buildParam();
        System.out.println("参数=>"+params);
        out.write(params.getBytes());
        out.flush();
        InputStream is = null;
        try{
            is = httpURLConnection.getInputStream();
            System.out.println("code is "+httpURLConnection.getResponseCode()+";"+"message is "+httpURLConnection.getResponseMessage());
        }catch (Exception e){
            is = httpURLConnection.getErrorStream();
            var resp = is.readAllBytes();
            throw new Exception("make request error:"+"code is "+httpURLConnection.getResponseCode()+";"+httpURLConnection.getResponseMessage()+new String(resp));
        }
        var resp = is.readAllBytes();
        return new String(resp);
    }

    public String doRequest() throws Exception {
        return this.makeRequest();
    }

    //构建url
    public static String assembleRequestUrl(String requestUrl, String apiKey, String apiSecret) {
        URL url = null;
        // 替换调schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String  httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://","https://" );
        try {
            url = new URL(httpRequestUrl);
            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            //System.out.println("date=>" + date);

            String host = url.getHost();
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append("POST ").append(url.getPath()).append(" HTTP/1.1");
            //System.out.println("builder=>" + builder);
            Charset charset = Charset.forName("UTF-8");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            //System.out.println("sha=>" + sha);

            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            //System.out.println("authorization=>" + authorization);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            //System.out.println("authBase=>" + authBase);
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:"+e.getMessage());
        }
    }

    public static class ResponseData {
        private Header header;
        private PayLoad payload;
        public Header getHeader() {
            return header;
        }
        public PayLoad getPayLoad() {
            return payload;
        }
    }
    public static class Header {
        private int code;
        private String message;
        private String sid;
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
        public String getSid() {
            return sid;
        }
    }
    public static class PayLoad {
        private FaceResult face_compare_result;
        public FaceResult getFaceCompareResult() {
            return face_compare_result;
        }
    }
    public static class FaceResult {
        private String compress;
        private String encoding;
        private String format;
        private String text;
        public String getCompress() {
            return compress;
        }
        public String getEncoding() {
            return encoding;
        }
        public String getFormat() {
            return format;
        }
        public String getText() {
            return text;
        }
    }
}
