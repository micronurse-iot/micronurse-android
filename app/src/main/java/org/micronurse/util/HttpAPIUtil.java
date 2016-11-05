package org.micronurse.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.request.PhoneCaptchaRequest;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.UserListResult;
import org.micronurse.http.model.result.UserResult;
import org.micronurse.model.User;

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
            public boolean onErrorResponse(VolleyError err, Result result) {
                return false;
            }
        }, Result.class).startRequest();
    }

    public static void getFriendList(final Context context, final Response.Listener<Result> listener, final APIErrorListener errorListener,
                                     final boolean showStatus){
        new MicronurseAPI<UserListResult>(context, MicronurseAPI.getApiUrl(MicronurseAPI.OlderFriendJuanAPI.FRIENDSHIP), Request.Method.GET,
                null, GlobalInfo.token, new Response.Listener<UserListResult>() {
            @Override
            public void onResponse(UserListResult response) {
                GlobalInfo.Older.friendList = response.getUserList();
                listener.onResponse(response);
            }
        }, new APIErrorListener() {
            @Override
            public boolean onErrorResponse(VolleyError err, Result result) {
                return errorListener.onErrorResponse(err, result);
            }
        }, UserListResult.class, showStatus, null).startRequest();
    }

    public static void finishLogin(final Context context, final String phoneNumber, final Response.Listener<Result> listener, final APIErrorListener errorListener,
                                   final boolean showStatus){
        new MicronurseAPI<UserResult>(context, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.USER_BASIC_INFO_BY_PHONE, phoneNumber), Request.Method.GET, null, null,
            new Response.Listener<UserResult>() {
                @Override
                public void onResponse(UserResult response) {
                    GlobalInfo.user = response.getUser();
                    GlobalInfo.user.setPhoneNumber(phoneNumber);
                    new MicronurseAPI<UserListResult>(context, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.GUARDIANSHIP), Request.Method.GET,
                            null, GlobalInfo.token, new Response.Listener<UserListResult>() {
                        @Override
                        public void onResponse(UserListResult response) {
                            GlobalInfo.guardianshipList = response.getUserList();
                            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN)
                                listener.onResponse(response);
                            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
                                getFriendList(context, listener, errorListener, showStatus);
                            }
                        }
                    }, new APIErrorListener() {
                        @Override
                        public boolean onErrorResponse(VolleyError err, Result result) {
                            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER && result.getResultCode() == PublicResultCode.GUARDIANSHIP_NOT_EXIST){
                                getFriendList(context, listener, errorListener, showStatus);
                                return true;
                            }
                            return errorListener.onErrorResponse(err, result);
                        }
                    }, UserListResult.class, showStatus, null).startRequest();
                }
            }, new APIErrorListener() {
                @Override
                public boolean onErrorResponse(VolleyError err, Result result) {
                    return errorListener.onErrorResponse(err, result);
                }
            }, UserResult.class, showStatus, null).startRequest();
    }
}
