package org.micronurse.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.PhoneCaptchaRequest;
import org.micronurse.http.model.result.Result;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

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

    private static List<Context> jPushAliasContextQueue = new LinkedList<>();
    private static List<String> jPushAliasQueue = new LinkedList<>();

    public static void setJPushAlias(Context context, String alias){
        jPushAliasContextQueue.add(context);
        jPushAliasQueue.add(alias);
        if(jPushAliasContextQueue.size() == 1)
            setJPushAlias();
    }

    private static void setJPushAlias(){
        if(jPushAliasContextQueue.isEmpty() || jPushAliasQueue.isEmpty())
            return;
        JPushInterface.setAlias(jPushAliasContextQueue.get(0), jPushAliasQueue.get(0), new TagAliasCallback() {
            @Override
            public void gotResult(int responseCode, String alias, Set<String> tags) {
                Log.i(GlobalInfo.LOG_TAG, "JPush set alias result code:" + responseCode);
                switch (responseCode){
                    case 0:
                        jPushAliasContextQueue.remove(0);
                        jPushAliasQueue.remove(0);
                        break;
                    case 6002:
                        break;
                }
                setJPushAlias();
            }
        });
    }
}
