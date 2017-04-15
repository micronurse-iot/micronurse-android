package org.micronurse.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;

import org.micronurse.R;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.PhoneCaptchaRequest;
import org.micronurse.net.model.request.ResetPasswordRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.net.PublicResultCode;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.HttpAPIUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetPasswordActivity extends AppCompatActivity {
    @BindView(R.id.edit_phone_number)
    EditText editPhoneNumber;
    @BindView(R.id.edit_password)
    EditText editPassword;
    @BindView(R.id.edit_repassword)
    EditText editRepassword;
    @BindView(R.id.edit_captcha)
    EditText editCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btn_get_captcha)
    void onBtnGetCaptchaClick(View v){
        if(CheckUtil.checkPhoneNumber(editPhoneNumber)){
            HttpAPIUtil.sendCaptcha(ResetPasswordActivity.this, new PhoneCaptchaRequest(editPhoneNumber.getText().toString()));
        }
    }

    @OnClick(R.id.btn_reset_password)
    void onBtnResetPasswordClick(View v){
        attemptReset();
    }

    private void attemptReset(){
        editPhoneNumber.setError(null);
        editPassword.setError(null);
        editRepassword.setError(null);
        editCaptcha.setError(null);

        if(!CheckUtil.checkPhoneNumber(editPhoneNumber))
            return;
        if(!CheckUtil.checkPassword(editPassword))
            return;
        if(!CheckUtil.recheckPassword(editPassword, editRepassword))
            return;
        if(!CheckUtil.checkCaptcha(editCaptcha))
            return;

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.action_resetting));
        pd.show();

        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.AccountAPI.RESET_PASSWORD), Request.Method.PUT, null, new ResetPasswordRequest(
                editPhoneNumber.getText().toString(), editPassword.getText().toString(), editCaptcha.getText().toString()
        ), new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onResponse() {
                pd.dismiss();
            }

            @Override
            public void onDataResponse(Result data) {
                Toast.makeText(ResetPasswordActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.BUNDLE_AUTO_LOGIN_KEY, true);
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, editPhoneNumber.getText().toString());
                intent.putExtra(LoginActivity.BUNDLE_PREFER_PASSWORD_KEY, editPassword.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ResetPasswordActivity.this.startActivity(intent);
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                switch (errorInfo.getResultCode()){
                    case PublicResultCode.LOGIN_USER_NOT_EXIST:
                        editPhoneNumber.setError(errorInfo.getMessage());
                        editPhoneNumber.requestFocus();
                        return true;
                    case PublicResultCode.PASSWORD_LENGTH_ILLEGAL:
                    case PublicResultCode.PASSWORD_FORMAT_ILLEGAL:
                        editPassword.setError(errorInfo.getMessage());
                        editPassword.requestFocus();
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
