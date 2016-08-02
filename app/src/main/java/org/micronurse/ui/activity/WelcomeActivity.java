package org.micronurse.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

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

import java.util.List;

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
                    new MicronurseAPI(WelcomeActivity.this, "/account/check_login", Request.Method.GET,
                            null, loginUserRecord.getToken(), new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            GlobalInfo.token = loginUserRecord.getToken();
                            new MicronurseAPI(WelcomeActivity.this, "/account/user_basic_info/by_phone/" + loginUserRecord.getPhoneNumber(), Request.Method.GET, null, null,
                                    new Response.Listener<Result>() {
                                        @Override
                                        public void onResponse(Result response) {
                                            UserResult userResult = (UserResult) response;
                                            GlobalInfo.user = userResult.getUser();
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
                            }, UserResult.class).startRequest();
                        }
                    }, new APIErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError err, Result result) {
                            if (result == null || result.getResultCode() != 401)
                                startLoginActivity();
                        }
                    }, Result.class).startRequest();
                }else{
                    startLoginActivity();
                }
            }
        }, 1000);
    }
}