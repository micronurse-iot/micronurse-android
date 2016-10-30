package org.micronurse.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.model.request.PhoneCaptchaRequest;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.ResetPasswordRequest;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.HttpAPIUtil;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText mPhoneNumberView;
    private EditText mPasswordView;
    private EditText mRPasswordView;
    private EditText mCaptchaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPhoneNumberView = (EditText) findViewById(R.id.resetpassword_phone_number);
        mPasswordView = (EditText)findViewById(R.id.resetpassword_password);
        mRPasswordView = (EditText)findViewById(R.id.resetpassword_reconfirm_password);
        mCaptchaView = (EditText)findViewById(R.id.resetpassword_identifying_code);

        mPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(CheckUtil.checkPhoneNumber(mPhoneNumberView))
                        mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(CheckUtil.checkPassword(mPasswordView))
                        mRPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mRPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_NEXT) {
                    if(CheckUtil.recheckPassword(mPasswordView, mRPasswordView))
                        mCaptchaView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mCaptchaView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    attemptReset();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.button_get_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckUtil.checkPhoneNumber(mPhoneNumberView)){
                    HttpAPIUtil.sendCaptcha(ResetPasswordActivity.this, new PhoneCaptchaRequest(mPhoneNumberView.getText().toString()));
                }
            }
        });

        Button resetButton = (Button) findViewById(R.id.button_reset_password);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReset();
            }
        });
    }

    private void attemptReset(){
        mPhoneNumberView.setError(null);
        mPasswordView.setError(null);
        mRPasswordView.setError(null);
        mCaptchaView.setError(null);

        if(!CheckUtil.checkPhoneNumber(mPhoneNumberView))
            return;
        if(!CheckUtil.checkPassword(mPasswordView))
            return;
        if(!CheckUtil.recheckPassword(mPasswordView, mRPasswordView))
            return;
        if(!CheckUtil.checkCaptcha(mCaptchaView))
            return;

        new MicronurseAPI<Result>(this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.RESET_PASSWORD), Request.Method.PUT, new ResetPasswordRequest(
                mPhoneNumberView.getText().toString(), mPasswordView.getText().toString(), mCaptchaView.getText().toString()
        ), null, new Response.Listener<Result>(){
            @Override
            public void onResponse(Result response) {
                Toast.makeText(ResetPasswordActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.BUNDLE_AUTO_LOGIN_KEY, true);
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, mPhoneNumberView.getText().toString());
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PASSWORD_KEY, mPasswordView.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ResetPasswordActivity.this.startActivity(intent);
            }
        }, new APIErrorListener(){
            @Override
            public boolean onErrorResponse(VolleyError err, Result result) {
                if(result != null) {
                    switch (result.getResultCode()){
                        case PublicResultCode.LOGIN_USER_NOT_EXIST:
                            mPhoneNumberView.setError(result.getMessage());
                            mPhoneNumberView.requestFocus();
                            return true;
                        case PublicResultCode.PASSWORD_LENGTH_ILLEGAL:
                        case PublicResultCode.PASSWORD_FORMAT_ILLEGAL:
                            mPasswordView.setError(result.getMessage());
                            mPasswordView.requestFocus();
                            return true;
                        case PublicResultCode.PHONE_CAPTCHA_INCORRECT:
                            mCaptchaView.setError(result.getMessage());
                            mCaptchaView.requestFocus();
                            return true;
                    }
                }
                return false;
            }
        }, Result.class, true, getString(R.string.action_resetting)).startRequest();
    }
}
