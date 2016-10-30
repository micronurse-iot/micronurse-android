package org.micronurse.http;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.model.result.Result;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.LoginActivity;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class MicronurseAPI<T extends Result> {
    private static final String BASE_URL = "http://101.200.144.204:13000/micronurse/v1/mobile";
    private static RequestQueue requestQueue = null;
    private JSONRequest<T> request;
    private ProgressDialog mStatusDialog;
    private Context mContext;

    public MicronurseAPI(final Context context, String apiURL, int method, Object requestData, String token, final Response.Listener<T> listener,
                         final APIErrorListener errorListener, Class<T> resultClass){
        this(context, apiURL, method, requestData, token, listener, errorListener, resultClass, true,
                context.getResources().getString(R.string.action_waiting));
    }

    public MicronurseAPI(Context context, String apiURL, int method, Object requestData, String token, final Response.Listener<T> listener,
                         final APIErrorListener errorListener, Class<T> resultClass, boolean showStatus, String statusText){
        this.mContext = context;
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(mContext);
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
                    showError(MicronurseAPI.this.mContext.getString(R.string.response_data_corrupt));
                } else if (error.networkResponse == null) {
                    if(!errorListener.onErrorResponse(error, null))
                        showError(MicronurseAPI.this.mContext.getString(R.string.network_error));
                } else {
                    Result result;
                    try {
                        result = GsonUtil.getGson().fromJson(new String(error.networkResponse.data), Result.class);
                    } catch (JsonSyntaxException jse) {
                        jse.printStackTrace();
                        result = new Result(-1, MicronurseAPI.this.mContext.getString(R.string.unknown_error));
                    }
                    Log.e("Micro nurser API", "onErrorResponse: result code:" + result.getResultCode() + " message:" + result.getMessage());
                    if(!errorListener.onErrorResponse(error, result)){
                        if (error.networkResponse.statusCode == 401 && result.getResultCode() == 401) {
                            Toast.makeText(mContext, R.string.error_login_state_invalid, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            if (GlobalInfo.user != null) {
                                intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.user.getPhoneNumber());
                                LoginUserRecord lur = DatabaseUtil.findLoginUserRecord(GlobalInfo.user.getPhoneNumber());
                                if(lur != null) {
                                    lur.setToken(null);
                                    lur.save();
                                }
                                Intent mqttServiceIntent = new Intent(mContext, MQTTService.class);
                                mContext.stopService(mqttServiceIntent);
                            }
                            GlobalInfo.clearLoginUserInfo();
                            mContext.startActivity(intent);
                        } else if (error.networkResponse.statusCode == 500) {
                            showError(mContext.getString(R.string.server_internal_error));
                        } else {
                            showError(result.getMessage());
                        }
                    }
                }
            }
        }, requestData, resultClass);

        if(showStatus) {
            mStatusDialog = new ProgressDialog(mContext);
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

    public void setJsonParser(JSONParser<T> jsonParser){
        request.setJsonParser(jsonParser);
    }

    public static String getApiUrl(String... urlParam){
        String url = BASE_URL;
        for(String s : urlParam){
            if(s != null && !s.isEmpty())
                url += '/' + s;
        }
        return url;
    }

    private void showError(String error){
        if(mContext instanceof Activity)
            Snackbar.make(((Activity) mContext).findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
    }

    public static class AccountAPI{
        public static String CHECK_LOGIN = "account/check_login";
        public static String LOGIN = "account/login";
        public static String USER_BASIC_INFO_BY_PHONE = "account/user_basic_info/by_phone";
        public static String REGISTER = "account/register";
        public static String SEND_CAPTCHA = "account/send_captcha";
        public static String LOGOUT = "account/logout";
        public static String RESET_PASSWORD = "account/reset_password";
        public static String GUARDIANSHIP = "account/guardianship";
    }

    public static class OlderAccountAPI{
        public static String SET_HOME_LOCATION ="account/set_home_address";
        public static String HOME_ADDRESS = "account/home_address/older";
    }

    public static class GuardianAccountAPI{
        public static String HOME_ADDRESS = "account/home_address/guardian";
    }

    public static class OlderSensorAPI{
        public static String LATEST_SENSOR_DATA = "sensor/sensor_data/older/latest";
        public static String SENSOR_WARNING = "sensor/warning/older";
    }

    public static class GuardianSensorAPI{
        public static String LATEST_SENSOR_DATA = "sensor/sensor_data/guardian/latest";
        public static String SENSOR_WARNING = "sensor/warning/guardian";
    }
}
