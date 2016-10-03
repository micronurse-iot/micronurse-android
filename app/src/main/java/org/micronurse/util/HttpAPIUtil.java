package org.micronurse.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.PhoneCaptchaRequest;
import org.micronurse.http.model.result.Result;

/**
 * Created by zhou-shengyun on 8/3/16.
 */
public class HttpAPIUtil {
    public static void sendCaptcha(final Context context, PhoneCaptchaRequest phoneCaptchaRequest){
        new MicronurseAPI<Result>(context, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.SEND_CAPTCHA), Request.Method.PUT, phoneCaptchaRequest, null,
                new Response.Listener<Result>() {
                    @Override
                    public void onResponse(Result response) {
                        Toast.makeText(context, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                if(result != null)
                    Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, Result.class).startRequest();
    }
}
