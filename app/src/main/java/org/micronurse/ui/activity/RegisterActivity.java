package org.micronurse.ui.activity;
/**
 * Created by 1111 on 2016/7/30.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.model.request.PhoneCaptchaRequest;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.RegisterRequest;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.HttpAPIUtil;

public class RegisterActivity extends AppCompatActivity{

    // UI references.
    private EditText actvPhoneNumberView;
    private EditText etPasswordView;
    private EditText etrPasswordView;
    private EditText mNicknameView;
    private EditText mCaptchaView;
    private RadioGroup mGenderGroup;
    private RadioGroup mAccountTypeGroup;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        actvPhoneNumberView = (EditText) findViewById(R.id.register_phone_number);
        etPasswordView = (EditText) findViewById(R.id.register_password);
        etrPasswordView = (EditText) findViewById(R.id.register_reconfirm_password);
        mNicknameView = (EditText) findViewById(R.id.register_name);
        mCaptchaView = (EditText) findViewById(R.id.register_identifying_code);
        mGenderGroup = (RadioGroup) findViewById(R.id.register_sex_group);
        mAccountTypeGroup = (RadioGroup) findViewById(R.id.register_account_type_group);

        actvPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(CheckUtil.checkPhoneNumber(actvPhoneNumberView))
                        etPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        etPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_NEXT){
                    if(CheckUtil.checkPassword(etPasswordView))
                        etrPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        etrPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_NEXT){
                    if(CheckUtil.recheckPassword(etPasswordView, etrPasswordView))
                        mNicknameView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mNicknameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    if(checkNickname())
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
                    register();
                }
                return false;
            }
        });

        ((Button)findViewById(R.id.button_get_code)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCaptcha();
            }
        });

        ((Button)findViewById(R.id.button_register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private boolean checkNickname(){
        mNicknameView.setError(null);
        if(TextUtils.isEmpty(mNicknameView.getText())){
            mNicknameView.setError(getString(R.string.error_nickname_empty));
            mNicknameView.requestFocus();
            return false;
        }
        return true;
    }

    private void sendCaptcha(){
        if(!CheckUtil.checkPhoneNumber(actvPhoneNumberView)) {
            return;
        }
        HttpAPIUtil.sendCaptcha(this, new PhoneCaptchaRequest(actvPhoneNumberView.getText().toString()));
    }

    private void register(){
        if(!CheckUtil.checkPhoneNumber(actvPhoneNumberView))
            return;
        if(!CheckUtil.checkPassword(etPasswordView))
            return;
        if(!CheckUtil.recheckPassword(etPasswordView, etrPasswordView))
            return;
        if(!CheckUtil.checkNickname(mNicknameView))
            return;
        if(!CheckUtil.checkCaptcha(mCaptchaView))
            return;
        char gender = User.GENDER_MALE;
        switch (mGenderGroup.getCheckedRadioButtonId()){
            case R.id.register_male:
                gender = User.GENDER_MALE;
                break;
            case R.id.register_female:
                gender = User.GENDER_FEMALE;
                break;
        }
        char accountType = User.ACCOUNT_TPYE_OLDER;
        switch (mAccountTypeGroup.getCheckedRadioButtonId()){
            case R.id.register_account_elder:
                accountType = User.ACCOUNT_TPYE_OLDER;
                break;
            case R.id.register_account_guardian:
                accountType = User.ACCOUNT_TYPE_GUARDIAN;
                break;
        }
        new MicronurseAPI<Result>(this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.REGISTER), Request.Method.POST, new RegisterRequest(
                actvPhoneNumberView.getText().toString(),
                etPasswordView.getText().toString(),
                mNicknameView.getText().toString(),
                gender, accountType, mCaptchaView.getText().toString()
        ), null, new Response.Listener<Result>() {
            @Override
            public void onResponse(Result response) {
                Toast.makeText(RegisterActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.BUNDLE_AUTO_LOGIN_KEY, true);
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, actvPhoneNumberView.getText().toString());
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PASSWORD_KEY, etPasswordView.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                RegisterActivity.this.startActivity(intent);
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                if (result == null)
                    return;
                switch (result.getResultCode()) {
                    case PublicResultCode.PHONE_NUM_INVALID:
                    case PublicResultCode.PHONE_NUM_REGISTERED:
                        actvPhoneNumberView.setError(result.getMessage());
                        actvPhoneNumberView.requestFocus();
                        break;
                    case PublicResultCode.PASSWORD_LENGTH_ILLEGAL:
                    case PublicResultCode.PASSWORD_FORMAT_ILLEGAL:
                        etPasswordView.setError(result.getMessage());
                        etPasswordView.requestFocus();
                        break;
                    case PublicResultCode.NICKNAME_REGISTERED:
                        mNicknameView.setError(result.getMessage());
                        mNicknameView.requestFocus();
                        break;
                    case PublicResultCode.PHONE_CAPTCHA_INCORRECT:
                        mCaptchaView.setError(result.getMessage());
                        mCaptchaView.requestFocus();
                        break;
                    default:
                        Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        },Result.class, true, getString(R.string.action_registering)).startRequest();
    }
}
