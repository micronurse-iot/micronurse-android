package org.micronurse.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.User;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.HttpAPIUtil;
import java.util.List;

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
                    new MicronurseAPI<Result>(WelcomeActivity.this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.CHECK_LOGIN, loginUserRecord.getPhoneNumber()), Request.Method.GET,
                            null, loginUserRecord.getToken(), new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            GlobalInfo.token = loginUserRecord.getToken();

                            HttpAPIUtil.finishLogin(WelcomeActivity.this, loginUserRecord.getPhoneNumber(), new Response.Listener<Result>() {
                                @Override
                                public void onResponse(Result response) {
                                    loginIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(loginIntent);
                                }
                            }, new APIErrorListener() {
                                @Override
                                public boolean onErrorResponse(VolleyError err, Result result) {
                                    if(result != null) {
                                        if(GlobalInfo.user != null){
                                            if((GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER && result.getResultCode() == PublicResultCode.MOBILE_FRIEND_JUAN_NO_FRIENDSHIP) ||
                                                    (GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN && result.getResultCode() == PublicResultCode.GUARDIANSHIP_NOT_EXIST)) {
                                                finish();
                                                startActivity(loginIntent);
                                                return true;
                                            }
                                        }
                                        startLoginActivity();
                                    }
                                    return false;
                                }
                            }, false);
                        }
                    }, new APIErrorListener() {
                        @Override
                        public boolean onErrorResponse(VolleyError err, Result result) {
                            if (result == null || result.getResultCode() != 401)
                                startLoginActivity();
                            return false;
                        }
                    }, Result.class, false, null).startRequest();
                }else{
                    startLoginActivity();
                }
            }
        }, 1000);
    }
}