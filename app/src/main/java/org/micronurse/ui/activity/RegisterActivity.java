package org.micronurse.ui.activity;
/**
 * Created by 1111 on 2016/7/30.
 */

import android.app.ProgressDialog;
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
import org.micronurse.database.model.PhoneCaptchaRequest;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.RegisterRequest;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.User;

public class RegisterActivity extends AppCompatActivity{

    // UI references.
    private EditText actvPhoneNumberView;
    private EditText etPasswordView;
    private EditText etrPasswordView;
    private EditText mNicknameView;
    private EditText mCaptchaView;
    private RadioGroup mGenderGroup;
    private RadioGroup mAccountTypeGroup;
    private ProgressDialog mProgressDialog;

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
                    if(checkPhonenumber())
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
                    if(checkPassword())
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
                    if(recheckPassword())
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

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

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

    private boolean isPhoneNumValid(String phoneNumber){
        for(int i = 0; i < phoneNumber.length(); i++){
            if(!(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9'))
                return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password){
        return !(password.length() < 6 || password.length() > 20);

    }

    private boolean checkPhonenumber(){
        // Reset errors.
        actvPhoneNumberView.setError(null);

        // Store values at the time of the login attempt.
        final String phoneNumber = actvPhoneNumberView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid phone number.
        if (TextUtils.isEmpty(phoneNumber)) {
            actvPhoneNumberView.setError(getString(R.string.error_phone_number_required));
            focusView = actvPhoneNumberView;
            cancel = true;
        }
        else if (!isPhoneNumValid(phoneNumber)) {
            actvPhoneNumberView.setError(getString(R.string.error_invalid_phone_number));
            focusView = actvPhoneNumberView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        }
        else
            return true;
    }

    private  boolean checkPassword(){
        etrPasswordView.setError(null);
        etPasswordView.setError(null);
        String password = etPasswordView.getText().toString();
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            etPasswordView.setError(getString(R.string.error_password_required));
            etPasswordView.requestFocus();
            return true;
        }
        return true;

    }

    private boolean recheckPassword(){
        etrPasswordView.setError(null);
        etPasswordView.setError(null);
        String rpassword = etrPasswordView.getText().toString();
        String password = etPasswordView.getText().toString();

        if(!rpassword.equals(password)){
            etPasswordView.setError(getString(R.string.error_password_inconstancy));
            etPasswordView.requestFocus();
            etrPasswordView.setError(getString(R.string.error_password_inconstancy));
            etrPasswordView.requestFocus();
            return false;
        }
        return true;
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


    private boolean checkCaptcha(){
        mCaptchaView.setError(null);
        if(TextUtils.isEmpty(mCaptchaView.getText())){
            mCaptchaView.setError(getString(R.string.error_captcha_empty));
            mCaptchaView.requestFocus();
            return false;
        }
        return true;
    }

    private void sendCaptcha(){
        if(!checkPhonenumber()) {
            return;
        }
        new MicronurseAPI(RegisterActivity.this, "/account/send_captcha", Request.Method.PUT,
                new PhoneCaptchaRequest(actvPhoneNumberView.getText().toString()), null,
                new Response.Listener<Result>() {
                    @Override
                    public void onResponse(Result response) {
                        Toast.makeText(RegisterActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new APIErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err, Result result) {
                        if(result != null)
                            Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, Result.class).startRequest();
    }

    private void register(){
        if(!checkPhonenumber())
            return;
        if(!checkPassword())
            return;
        if(!recheckPassword())
            return;
        if(!checkNickname())
            return;
        if(!checkCaptcha())
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
        mProgressDialog.setMessage(getString(R.string.action_registering));
        mProgressDialog.show();
        new MicronurseAPI(this, "/account/register", Request.Method.POST, new RegisterRequest(
                actvPhoneNumberView.getText().toString(),
                etPasswordView.getText().toString(),
                mNicknameView.getText().toString(),
                gender, accountType, mCaptchaView.getText().toString()
        ), null, new Response.Listener<Result>() {
            @Override
            public void onResponse(Result response) {
                mProgressDialog.dismiss();
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
                mProgressDialog.dismiss();
                if(result == null)
                    return;
                switch (result.getResultCode()){
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
        }, Result.class).startRequest();
    }
}
