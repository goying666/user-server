package com.renchaigao.zujuba.userserver.uti;


import com.alibaba.fastjson.JSONObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationCodeMethod {

    public static Boolean GetAuthenticationCode(String telephoneNumber, String code) {
        String param = "code:" + code;
        String tplId = "TP19011827";
        String appcode = "d39d906897464015bf6c1388aa2d40c7";
        String url = "http://dingxin.market.alicloudapi.com/dx/sendSms?mobile=" + telephoneNumber
                + "&param=" + param + "&tpl_id=" + tplId;
        FormBody.Builder builder = new FormBody.Builder();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "APPCODE " + appcode)
                .post(builder.build())
                .build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            response = okHttpClient.newCall(request).execute();
            int status = response.code();
            if (response.isSuccessful()) {
                JSONObject responseJson = JSONObject.parseObject(response.body().string());
                String return_code = responseJson.get("return_code").toString();
                String order_id = responseJson.get("order_id").toString();
                if (return_code.equals("00000")) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    public static String CreateFourCode() {
        Integer a = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;//产生1000-9999的随机数
        return a.toString();
    }
}
