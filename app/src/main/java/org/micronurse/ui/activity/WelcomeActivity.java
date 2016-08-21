package org.micronurse.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.activeandroid.query.Select;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.UserResult;
import org.micronurse.model.User;
import org.micronurse.ui.activity.older.OlderMainActivity;
import org.micronurse.util.GlobalInfo;

public class WelcomeActivity extends AppCompatActivity {
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
                final LoginUserRecord loginUserRecord = new Select().from(LoginUserRecord.class)
                        .orderBy("LastLoginTime DESC").limit(1).executeSingle();
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
                                                    Intent intent = new Intent(WelcomeActivity.this, OlderMainActivity.class);
                                                    finish();
                                                    startActivity(intent);
                                                    break;
                                            }
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
    }
}