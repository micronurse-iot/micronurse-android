package org.micronurse.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.PhoneCaptchaRequest;
import org.micronurse.net.model.result.Result;

/**
 * Created by zhou-shengyun on 8/3/16.
 */
public class HttpAPIUtil {
    public static void sendCaptcha(final Context context, PhoneCaptchaRequest phoneCaptchaRequest){
        HttpApi.startRequest(new HttpApiJsonRequest(context, HttpApi.getApiUrl(HttpApi.AccountAPI.SEND_CAPTCHA), Request.Method.PUT, null, phoneCaptchaRequest, new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onDataResponse(Result data) {
                Toast.makeText(context, data.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
