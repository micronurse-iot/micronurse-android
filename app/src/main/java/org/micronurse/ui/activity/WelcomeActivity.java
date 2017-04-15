package org.micronurse.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.util.DatabaseUtil;
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
                final LoginUserRecord loginUserRecord;
                List<LoginUserRecord> records = DatabaseUtil.findAllLoginUserRecords(1);
                if(records == null || records.isEmpty())
                    loginUserRecord = null;
                else
                    loginUserRecord = records.get(0);

                if(loginUserRecord != null && loginUserRecord.getToken() != null && !loginUserRecord.getToken().isEmpty()) {
                    HttpApi.startRequest(new HttpApiJsonRequest(getApplicationContext(), HttpApi.getApiUrl(HttpApi.AccountAPI.CHECK_LOGIN, String.valueOf(loginUserRecord.getUserId())), Request.Method.GET,
                            loginUserRecord.getToken(), null, new HttpApiJsonListener<Result>(Result.class) {
                        @Override
                        public void onDataResponse(Result data) {
                            GlobalInfo.token = loginUserRecord.getToken();
                            LoginActivity.afterLogin(WelcomeActivity.this, loginUserRecord.getPhoneNumber(), new HttpApiJsonListener<Result>(Result.class) {
                                @Override
                                public void onDataResponse(Result data) {
                                    finish();
                                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                                }

                                @Override
                                public void onErrorResponse() {
                                    startLoginActivity();
                                }

                                @Override
                                public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                                    if(statusCode == 401 && errorInfo.getResultCode() == 401){
                                        Toast.makeText(WelcomeActivity.this, R.string.alert_session_expired, Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }

                        @Override
                        public void onErrorResponse() {
                            startLoginActivity();
                        }

                        @Override
                        public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                            if(statusCode == 401 && errorInfo.getResultCode() == 401){
                                Toast.makeText(WelcomeActivity.this, R.string.alert_session_expired, Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            return false;
                        }
                    }));

                }else{
                    startLoginActivity();
                }
            }
        }, 1000);
    }
}