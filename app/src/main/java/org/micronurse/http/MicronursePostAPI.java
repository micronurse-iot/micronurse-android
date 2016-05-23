package org.micronurse.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.micronurse.http.model.Result;
import org.micronurse.util.GsonUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class MicronursePostAPI {
    private static final String baseURL = "http://101.200.144.204:13000/micronurse/mobile/";
    private static RequestQueue requestQueue = null;
    private Map<String, String> requestMap = new HashMap<>();
    private Request<Result> request;

    public MicronursePostAPI(Context context, String apiName, Object requestData, String token, Response.Listener<Result> listener,
                             Response.ErrorListener errorListener, Class<? extends Result> resultType){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        requestMap.put("data", GsonUtil.getGson().toJson(requestData));
        requestMap.put("timestamp", Long.toString(System.currentTimeMillis()));
        requestMap.put("sign", getSign(requestMap.get("data"),
                                       requestMap.get("timestamp"),
                                       token));
        request = new PostRequest(baseURL + apiName, listener, errorListener, requestMap, resultType);
    }

    private String getSign(String data, String timestamp, String token){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
        md5.update(data.getBytes());
        md5.update(timestamp.getBytes());
        if(token != null && !token.isEmpty())
            md5.update(token.getBytes());
        byte[] tmp = md5.digest();
        StringBuilder sb = new StringBuilder();
        for(byte b:tmp) {
            String hexNum = Integer.toHexString(b & 0xff);
            if(hexNum.length() < 2)
                sb.append('0');
            sb.append(hexNum);
        }
        return sb.toString();
    }

    public void startRequest(){
        requestQueue.add(request);
    }

    public void cancelRequest(){
        request.cancel();
    }
}
