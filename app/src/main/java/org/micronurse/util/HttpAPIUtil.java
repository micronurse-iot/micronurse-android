package org.micronurse.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.volley.Request;

import org.micronurse.R;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.LoginIoTRequest;
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

    public static void loginIoT(final Context context, final String deviceToken){
        if(deviceToken == null || deviceToken.isEmpty())
            return;
        AlertDialog ad = new AlertDialog.Builder(context)
                .setTitle(R.string.action_confirm)
                .setMessage(R.string.alert_login_iot)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HttpApi.startRequest(new HttpApiJsonRequest(context, HttpApi.getApiUrl(HttpApi.AccountAPI.LOGIN_IOT), Request.Method.PUT, GlobalInfo.token,
                                new LoginIoTRequest(deviceToken), new HttpApiJsonListener<Result>(Result.class) {
                            @Override
                            public void onDataResponse(Result data) {
                                Toast.makeText(context, data.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                                if(errorInfo.getResultCode() != 401) {
                                    AlertDialog errDialog = new AlertDialog.Builder(context)
                                            .setTitle(R.string.error)
                                            .setMessage(errorInfo.getMessage())
                                            .setPositiveButton(R.string.action_ok, null)
                                            .create();
                                    if (errorInfo.getResultCode() == PublicResultCode.IOT_LOGIN_INVALID_DEVICE_TOKEN) {
                                        errDialog.setMessage(context.getString(R.string.alert_invalid_login_iot_qrcode));
                                    }
                                    errDialog.show();
                                    return true;
                                }
                                return false;
                            }
                        }));
                    }
                }).create();
        ad.show();
    }
}
