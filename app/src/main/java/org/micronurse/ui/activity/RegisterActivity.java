package org.micronurse.ui.activity;
/**
 * Created by 1111 on 2016/7/30.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.android.volley.Request;

import org.micronurse.R;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.PhoneCaptchaRequest;
import org.micronurse.net.model.request.RegisterRequest;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.model.result.Result;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.HttpAPIUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity{
    @BindView(R.id.edit_phone_number)
    EditText editPhoneNumber;
    @BindView(R.id.edit_password)
    EditText editPassword;
    @BindView(R.id.edit_repassword)
    EditText editRepassword;
    @BindView(R.id.edit_nickname)
    EditText editNickname;
    @BindView(R.id.edit_captcha)
    EditText editCaptcha;
    @BindView(R.id.radiogroup_gender)
    RadioGroup mGenderGroup;
    @BindView(R.id.radiogroup_account_type)
    RadioGroup mAccountTypeGroup;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btn_get_captcha)
    void onBtnGetCpatchaClick(View v){
        sendCaptcha();
    }

    @OnClick(R.id.btn_sign_up)
    void onBtnSignupClick(View v){
        register();
    }

    private void sendCaptcha(){
        if(!CheckUtil.checkPhoneNumber(editPhoneNumber)) {
            return;
        }
        HttpAPIUtil.sendCaptcha(this, new PhoneCaptchaRequest(editPhoneNumber.getText().toString()));
    }

    private void register(){
        if(!CheckUtil.checkPhoneNumber(editPhoneNumber))
            return;
        if(!CheckUtil.checkPassword(editPassword))
            return;
        if(!CheckUtil.recheckPassword(editPassword, editRepassword))
            return;
        if(!CheckUtil.checkNickname(editNickname))
            return;
        if(!CheckUtil.checkCaptcha(editCaptcha))
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
        char accountType = User.ACCOUNT_TYPE_OLDER;
        switch (mAccountTypeGroup.getCheckedRadioButtonId()){
            case R.id.register_account_elder:
                accountType = User.ACCOUNT_TYPE_OLDER;
                break;
            case R.id.register_account_guardian:
                accountType = User.ACCOUNT_TYPE_GUARDIAN;
                break;
        }

        RegisterRequest request = new RegisterRequest(editPhoneNumber.getText().toString(),
                editPassword.getText().toString(),
                editNickname.getText().toString(),
                gender, accountType, editCaptcha.getText().toString());

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.action_registering));
        pd.show();

        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.AccountAPI.REGISTER), Request.Method.POST, null,
                request, new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onResponse() {
                pd.dismiss();
            }

            @Override
            public void onDataResponse(Result data) {
                Toast.makeText(RegisterActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.BUNDLE_AUTO_LOGIN_KEY, true);
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, editPhoneNumber.getText().toString());
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PASSWORD_KEY, editPassword.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                RegisterActivity.this.startActivity(intent);
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                switch (errorInfo.getResultCode()) {
                    case PublicResultCode.PHONE_NUM_INVALID:
                    case PublicResultCode.PHONE_NUM_REGISTERED:
                        editPhoneNumber.setError(errorInfo.getMessage());
                        editPhoneNumber.requestFocus();
                        return true;
                    case PublicResultCode.PASSWORD_LENGTH_ILLEGAL:
                    case PublicResultCode.PASSWORD_FORMAT_ILLEGAL:
                        editPassword.setError(errorInfo.getMessage());
                        editPassword.requestFocus();
                        return true;
                    case PublicResultCode.NICKNAME_REGISTERED:
                        editNickname.setError(errorInfo.getMessage());
                        editNickname.requestFocus();
                        return true;
                    case PublicResultCode.PHONE_CAPTCHA_INCORRECT:
                        editCaptcha.setError(errorInfo.getMessage());
                        editCaptcha.requestFocus();
                        return true;
                }
                return super.onErrorDataResponse(statusCode, errorInfo);
            }
        }));
    }
}
