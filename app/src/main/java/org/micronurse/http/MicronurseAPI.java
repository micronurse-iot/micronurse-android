package org.micronurse.http;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;

import org.micronurse.R;
import org.micronurse.http.model.result.Result;
import org.micronurse.ui.activity.LoginActivity;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class MicronurseAPI {
    private static final String BASE_URL = "http://101.200.144.204:13000/micronurse/v1/mobile";
    private static RequestQueue requestQueue = null;
    private Request<Result> request;

    public MicronurseAPI(final Context context, String apiURL, int method, Object requestData, String token, Response.Listener<Result> listener,
                         final APIErrorListener errorListener, Class<? extends Result> resultType){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        request = new JSONRequest(BASE_URL + apiURL, method, token, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if(error.getCause() instanceof JsonSyntaxException) {
                    Toast.makeText(context, R.string.response_data_corrupt, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(error.networkResponse == null) {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    errorListener.onErrorResponse(error, null);
                    return;
                }else{
                    Result result;
                    try {
                        result = GsonUtil.getGson().fromJson(new String(error.networkResponse.data), Result.class);
                    }catch (JsonSyntaxException jse){
                        jse.printStackTrace();
                        errorListener.onErrorResponse(error, new Result(-1, context.getString(R.string.unknown_error)));
                        return;
                    }
                    if(error.networkResponse.statusCode == 401 && result.getResultCode() == 401){
                        Toast.makeText(context, R.string.error_login_state_invalid, Toast.LENGTH_SHORT).show();
                        errorListener.onErrorResponse(error, result);
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        if(GlobalInfo.loginRecord != null) {
                            intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.loginRecord.getPhoneNumber());
                            GlobalInfo.loginRecord.setToken(null);
                            GlobalInfo.loginRecord.save();
                        }
                        GlobalInfo.clearLoginUserInfo();

                        context.startActivity(intent);
                        return;
                    }
                    else if(error.networkResponse.statusCode == 500) {
                        Toast.makeText(context, R.string.server_internal_error, Toast.LENGTH_SHORT).show();
                    }
                    errorListener.onErrorResponse(error, result);
                }
            }
        }, requestData, resultType);
    }

    public void startRequest(){
        requestQueue.add(request);
    }

    public void cancelRequest(){
        request.cancel();
    }
}
