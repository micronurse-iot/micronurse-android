package org.micronurse.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
public class MicronurseAPI<T extends Result> {
    private static final String BASE_URL = "http://101.200.144.204:13000/micronurse/v1/mobile";
    private static RequestQueue requestQueue = null;
    private Request<T> request;
    private ProgressDialog mStatusDialog;

    public MicronurseAPI(final Context context, String apiURL, int method, Object requestData, String token, final Response.Listener<T> listener,
                         final APIErrorListener errorListener, Class<T> resultClass){
        this(context, apiURL, method, requestData, token, listener, errorListener, resultClass, true,
                context.getResources().getString(R.string.action_waiting));
    }

    public MicronurseAPI(final Context context, String apiURL, int method, Object requestData, String token, final Response.Listener<T> listener,
                         final APIErrorListener errorListener, Class<T> resultClass, boolean showStatus, String statusText){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        request = new JSONRequest<>(apiURL, method, token, new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (mStatusDialog != null)
                    mStatusDialog.dismiss();
                listener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (mStatusDialog != null)
                    mStatusDialog.dismiss();
                if (error.getCause() instanceof JsonSyntaxException) {
                    Toast.makeText(context, R.string.response_data_corrupt, Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse == null) {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    errorListener.onErrorResponse(error, null);
                } else {
                    Result result;
                    try {
                        result = GsonUtil.getGson().fromJson(new String(error.networkResponse.data), Result.class);
                    } catch (JsonSyntaxException jse) {
                        jse.printStackTrace();
                        errorListener.onErrorResponse(error, new Result(-1, context.getString(R.string.unknown_error)));
                        return;
                    }
                    Log.e("Micro nurser API", "onErrorResponse: result code:" + result.getResultCode() + " message:" + result.getMessage());
                    if (error.networkResponse.statusCode == 401 && result.getResultCode() == 401) {
                        Toast.makeText(context, R.string.error_login_state_invalid, Toast.LENGTH_SHORT).show();
                        errorListener.onErrorResponse(error, result);
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        if (GlobalInfo.loginRecord != null) {
                            intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.loginRecord.getPhoneNumber());
                            GlobalInfo.loginRecord.setToken(null);
                            GlobalInfo.loginRecord.save();
                        }
                        GlobalInfo.clearLoginUserInfo();

                        context.startActivity(intent);
                        return;
                    } else if (error.networkResponse.statusCode == 500) {
                        Toast.makeText(context, R.string.server_internal_error, Toast.LENGTH_SHORT).show();
                    }
                    errorListener.onErrorResponse(error, result);
                }
            }
        }, requestData, resultClass);

        if(showStatus) {
            mStatusDialog = new ProgressDialog(context);
            mStatusDialog.setMessage(statusText);
            mStatusDialog.setCancelable(false);
        }
    }

    public void startRequest(){
        requestQueue.add(request);
        if(mStatusDialog != null)
            mStatusDialog.show();
    }

    public void cancelRequest(){
        request.cancel();
    }

    public static String getApiUrl(String... urlParam){
        String url = BASE_URL;
        for(String s : urlParam){
            url += '/' + s;
        }
        return url;
    }

    public static class AccountAPI{
        public static String CHECK_LOGIN = "account/check_login";
        public static String LOGIN = "account/login";
        public static String USER_BASIC_INFO_BY_PHONE = "account/user_basic_info/by_phone";
        public static String REGISTER = "account/register";
        public static String SEND_CAPTCHA = "account/send_captcha";
        public static String LOGOUT = "account/logout";
        public static String RESET_PASSWORD = "account/reset_password";
    }

    public static class OlderSensorAPI{
        public static String LATEST_SENSOR_DATA = "sensor/sensor_data/older/latest";
    }
}
