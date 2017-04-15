package org.micronurse.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.LoginRequest;
import org.micronurse.net.model.result.LoginResult;
import org.micronurse.net.model.result.Result;
import org.micronurse.net.PublicResultCode;
import org.micronurse.model.User;
import org.micronurse.net.model.result.UserListResult;
import org.micronurse.net.model.result.UserResult;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * A login screen that offers login via phone number/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String BUNDLE_PREFER_PHONE_NUMBER_KEY = "PreferPhoneNum";
    public static final String BUNDLE_PREFER_PASSWORD_KEY = "PreferPassword";
    public static final String BUNDLE_AUTO_LOGIN_KEY = "AutoLogin";

    // UI references.
    @BindView(R.id.edit_phone_number)
    AutoCompleteTextView editPhoneNum;
    @BindView(R.id.edit_password)
    EditText editPassword;
    @BindView(R.id.login_portrait)
    ImageView imgLoginPortrait;

    private List<LoginUserRecord> loginUserRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginUserRecords = DatabaseUtil.findAllLoginUserRecords(10);
        List<String> phoneNumberRecords = new ArrayList<String>();
        for(LoginUserRecord lur : loginUserRecords)
            phoneNumberRecords.add(lur.getPhoneNumber());
        editPhoneNum.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, phoneNumberRecords));
    }

    @OnTextChanged(value = R.id.edit_phone_number, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onPhoneNumChanged(Editable s){
        String tempPhoneNum = s.toString();
        for(LoginUserRecord lur : loginUserRecords){
            if(lur.getPhoneNumber().equals(tempPhoneNum)) {
                imgLoginPortrait.setImageBitmap(lur.getPortrait());
                return;
            }
        }
        imgLoginPortrait.setImageResource(R.mipmap.default_portrait);
    }

    @OnEditorAction(R.id.edit_password)
    boolean onPasswordDone(TextView textView, int id, KeyEvent keyEvent) {
        if(id == EditorInfo.IME_ACTION_DONE) {
            attemptLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.btn_sign_in)
    void onBtnSignInClick(View v){
        attemptLogin();
    }

    @OnClick(R.id.btn_sign_up)
    void onBtnSignUpClick(View v){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.btn_forget_password)
    void onBtnResetPasswordClick(View v){
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String preferPhoneNum = intent.getStringExtra(BUNDLE_PREFER_PHONE_NUMBER_KEY);
        if(preferPhoneNum != null)
            editPhoneNum.setText(preferPhoneNum);
        String preferPassword = intent.getStringExtra(BUNDLE_PREFER_PASSWORD_KEY);
        if(preferPassword != null)
            editPassword.setText(preferPassword);
        if(intent.getBooleanExtra(BUNDLE_AUTO_LOGIN_KEY, false))
            attemptLogin();
    }

    private void attemptLogin() {
        // Reset errors.
        editPhoneNum.setError(null);
        editPassword.setError(null);

        // Store values at the time of the login attempt.
        final String phoneNumber = editPhoneNum.getText().toString();
        String password = editPassword.getText().toString();

        // Check for a valid phone number.
        if(!CheckUtil.checkPhoneNumber(editPhoneNum))
            return;

        // Check for a valid password, if the user entered one.
        if (!CheckUtil.checkPassword(editPassword))
            return;

        // Kick off a background task to perform the user login attempt.
        LoginRequest loginRequest = new LoginRequest(phoneNumber, password);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.action_logining));
        pd.show();

        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.AccountAPI.LOGIN), Request.Method.PUT, null, loginRequest, new HttpApiJsonListener<LoginResult>(LoginResult.class) {
            @Override
            public void onDataResponse(LoginResult data) {
                GlobalInfo.token = data.getToken();
                afterLogin(LoginActivity.this, phoneNumber, new HttpApiJsonListener<Result>(Result.class) {
                    @Override
                    public void onDataResponse(Result data) {
                        pd.dismiss();
                        finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                    @Override
                    public void onErrorResponse() {
                        pd.dismiss();
                    }
                });
            }

            @Override
            public void onErrorResponse() {
                pd.dismiss();
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                switch (errorInfo.getResultCode()) {
                    case PublicResultCode.LOGIN_USER_NOT_EXIST:
                        editPhoneNum.setError(errorInfo.getMessage());
                        editPhoneNum.requestFocus();
                        return true;
                    case PublicResultCode.LOGIN_INCORRECT_PASSWORD:
                        editPassword.setError(errorInfo.getMessage());
                        editPassword.requestFocus();
                        return true;
                }
                return false;
            }
        }));
    }

    public static void afterLogin(final Activity activity, final String phoneNumber, final HttpApiJsonListener<Result> listener){
        HttpApi.startRequest(new HttpApiJsonRequest(activity, HttpApi.getApiUrl(HttpApi.AccountAPI.USER_BASIC_INFO_BY_PHONE, phoneNumber), Request.Method.GET, null, null,
                new HttpApiJsonListener<UserResult>(UserResult.class) {
                    @Override
                    public void onDataResponse(UserResult data) {
                        GlobalInfo.user = data.getUser();
                        GlobalInfo.user.setPhoneNumber(phoneNumber);
                        getGuardianship(activity, listener);
                    }

                    @Override
                    public void onErrorResponse() {
                        listener.onErrorResponse();
                    }
                }));

    }

    private static void getGuardianship(final Activity activity, final HttpApiJsonListener<Result> listener){
        HttpApi.startRequest(new HttpApiJsonRequest(activity, HttpApi.getApiUrl(HttpApi.AccountAPI.GUARDIANSHIP), Request.Method.GET, GlobalInfo.token, null, new HttpApiJsonListener<UserListResult>(UserListResult.class) {
            @Override
            public void onDataResponse(UserListResult data) {
                GlobalInfo.guardianshipList = data.getUserList();
                if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN)
                    listener.onDataResponse((Result) null);
                else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
                    getFriendList(activity, listener);
                }
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                if(errorInfo.getResultCode() == PublicResultCode.GUARDIANSHIP_NOT_EXIST){
                    if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN)
                        listener.onDataResponse((Result) null);
                    else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
                        getFriendList(activity, listener);
                    }
                    return true;
                }
                listener.onErrorResponse();
                return false;
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                listener.onErrorResponse();
                return false;
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                listener.onErrorResponse();
                return false;
            }
        }));
    }

    private static void getFriendList(final Activity activity, final HttpApiJsonListener<Result> listener){
        HttpApi.startRequest(new HttpApiJsonRequest(activity, HttpApi.getApiUrl(HttpApi.OlderFriendJuanAPI.FRIENDSHIP), Request.Method.GET, GlobalInfo.token, null, new HttpApiJsonListener<UserListResult>(UserListResult.class) {
            @Override
            public void onDataResponse(UserListResult data) {
                GlobalInfo.Older.friendList = data.getUserList();
                listener.onDataResponse((Result) null);
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                if(errorInfo.getResultCode() == PublicResultCode.FRIEND_JUAN_NO_FRIENDSHIP){
                    listener.onDataResponse((Result) null);
                    return true;
                }
                listener.onErrorResponse();
                return false;
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                listener.onErrorResponse();
                return false;
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                listener.onErrorResponse();
                return false;
            }
        }));
    }
}

