package org.micronurse.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.UserListResult;
import org.micronurse.http.model.result.UserResult;
import org.micronurse.model.User;
import org.micronurse.ui.activity.guardian.GuardianMainActivity;
import org.micronurse.ui.activity.older.OlderMainActivity;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.HttpAPIUtil;

import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class WelcomeActivity extends AppCompatActivity {
    private Intent loginIntent;

    private void startLoginActivity() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final LoginUserRecord loginUserRecord;
                List<LoginUserRecord> records = DatabaseUtil.findAllLoginUserRecords(1);
                if(records == null || records.isEmpty())
                    loginUserRecord = null;
                else
                    loginUserRecord = records.get(0);

                if(loginUserRecord != null && loginUserRecord.getToken() != null && !loginUserRecord.getToken().isEmpty()) {
                    new MicronurseAPI<Result>(WelcomeActivity.this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.CHECK_LOGIN), Request.Method.GET,
                            null, loginUserRecord.getToken(), new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            GlobalInfo.token = loginUserRecord.getToken();
                            new MicronurseAPI<UserResult>(WelcomeActivity.this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.USER_BASIC_INFO_BY_PHONE, loginUserRecord.getPhoneNumber()), Request.Method.GET, null, null,
                                    new Response.Listener<UserResult>() {
                                        @Override
                                        public void onResponse(UserResult response) {
                                            GlobalInfo.user = response.getUser();
                                            GlobalInfo.user.setPhoneNumber(loginUserRecord.getPhoneNumber());
                                            switch (GlobalInfo.user.getAccountType()) {
                                                case User.ACCOUNT_TPYE_OLDER:
                                                    loginIntent = new Intent(WelcomeActivity.this, OlderMainActivity.class);
                                                    break;
                                                case User.ACCOUNT_TYPE_GUARDIAN:
                                                    loginIntent = new Intent(WelcomeActivity.this, GuardianMainActivity.class);
                                                    break;
                                            }
                                            new MicronurseAPI<UserListResult>(WelcomeActivity.this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.GUARDIANSHIP), Request.Method.GET,
                                                    null, GlobalInfo.token, new Response.Listener<UserListResult>() {
                                                @Override
                                                public void onResponse(UserListResult response) {
                                                    GlobalInfo.guardianshipList = response.getUserList();
                                                    HttpAPIUtil.setJPushAlias(WelcomeActivity.this, GlobalInfo.user.getPhoneNumber());
                                                    finish();
                                                    startActivity(loginIntent);
                                                }
                                            }, new APIErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError err, Result result) {
                                                    startLoginActivity();
                                                }
                                            }, UserListResult.class, false, null).startRequest();
                                        }
                                    }, new APIErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError err, Result result) {
                                            startLoginActivity();
                                        }
                                    }, UserResult.class, false, null).startRequest();
                        }
                    }, new APIErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError err, Result result) {
                            if (result == null || result.getResultCode() != 401)
                                startLoginActivity();
                        }
                    }, Result.class, false, null).startRequest();
                }else{
                    startLoginActivity();
                }
            }
        }, 1000);

        if(JPushInterface.isPushStopped(getApplicationContext())){
            JPushInterface.resumePush(getApplicationContext());
        }
    }
}