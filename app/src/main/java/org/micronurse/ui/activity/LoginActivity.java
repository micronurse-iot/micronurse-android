package org.micronurse.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.LoginRequest;
import org.micronurse.http.model.result.LoginResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.UserResult;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.model.User;
import org.micronurse.ui.activity.older.OlderMainActivity;
import org.micronurse.ui.activity.sms.SmsVerifyActivity;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via phone number/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String BUNDLE_PREFER_PHONE_NUMBER_KEY = "PreferPhoneNum";
    public static final String BUNDLE_PREFER_PASSWORD_KEY = "PreferPassword";
    public static final String BUNDLE_AUTO_LOGIN_KEY = "AutoLogin";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private boolean mLoginTask = false;

    // UI references.
    private AutoCompleteTextView mPhoneNumberView;
    private EditText mPasswordView;
    private ImageView mPortraitImageView;

    Intent intent1 = null;
    Intent intent2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        mPortraitImageView = (ImageView) findViewById(R.id.login_portrait);

        final List<LoginUserRecord> loginUserRecords = new Select().from(LoginUserRecord.class)
                .orderBy("LastLoginTime DESC").execute();
        List<String> phoneNumberRecords = new ArrayList<String>();
        for(LoginUserRecord lur : loginUserRecords){
            phoneNumberRecords.add(lur.getPhoneNumber());
        }

        mPhoneNumberView = (AutoCompleteTextView) findViewById(R.id.login_phone_number);
        mPhoneNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String tempPhoneNum = s.toString();
                for(LoginUserRecord lur : loginUserRecords){
                    if(lur.getPhoneNumber().equals(tempPhoneNum)) {
                        mPortraitImageView.setImageBitmap(lur.getPortrait());
                        return;
                    }
                }
                mPortraitImageView.setImageResource(R.mipmap.default_portrait);
            }
        });
        mPhoneNumberView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, phoneNumberRecords));
        mPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.button_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mForgetPassword = (Button) findViewById(R.id.button_forget_password);
        mForgetPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intent1 = new Intent(LoginActivity.this,SmsVerifyActivity.class);
                startActivity(intent1);
            }
        });
        Button mNewUser = (Button) findViewById(R.id.button_new_user);
        mNewUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intent2 = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent2);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String preferPhoneNum = intent.getStringExtra(BUNDLE_PREFER_PHONE_NUMBER_KEY);
        if(preferPhoneNum != null)
            mPhoneNumberView.setText(preferPhoneNum);
        String preferPassword = intent.getStringExtra(BUNDLE_PREFER_PASSWORD_KEY);
        if(preferPassword != null)
            mPasswordView.setText(preferPassword);
        if(intent.getBooleanExtra(BUNDLE_AUTO_LOGIN_KEY, false))
            attemptLogin();
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid phone number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mLoginTask) {
            return;
        }

        // Reset errors.
        mPhoneNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid phone number.
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_phone_number_required));
            focusView = mPhoneNumberView;
            cancel = true;
        } else if (!isPhoneNumValid(phoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_invalid_phone_number));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Kick off a background task to perform the user login attempt.

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(getString(R.string.action_logining));
            progressDialog.show();

            LoginRequest loginRequest = new LoginRequest(phoneNumber, password);
            final MicronurseAPI request = new MicronurseAPI(LoginActivity.this, "/account/login", Request.Method.PUT, loginRequest, null,
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            LoginResult result = (LoginResult)response;
                            GlobalInfo.token = result.getToken();
                            progressDialog.setCancelable(false);
                            new MicronurseAPI(LoginActivity.this, "/account/user_basic_info/by_phone/" + phoneNumber, Request.Method.GET, null, null,
                                new Response.Listener<Result>(){
                                    @Override
                                    public void onResponse(Result response) {
                                        progressDialog.dismiss();
                                        UserResult userResult = (UserResult)response;
                                        GlobalInfo.user = userResult.getUser();
                                        GlobalInfo.user.setPhoneNumber(phoneNumber);
                                        switch (GlobalInfo.user.getAccountType()){
                                            case User.ACCOUNT_TPYE_OLDER:
                                                Intent intent = new Intent(LoginActivity.this, OlderMainActivity.class);
                                                finish();
                                                startActivity(intent);
                                                break;
                                        }
                                    }
                                }, new APIErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError err, Result result) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }, UserResult.class).startRequest();
                        }
                    }, new APIErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Result result) {
                            progressDialog.dismiss();
                            if(result == null)
                                return;
                            switch (result.getResultCode()){
                                case PublicResultCode.LOGIN_USER_NOT_EXIST:
                                    mPhoneNumberView.setError(result.getMessage());
                                    mPhoneNumberView.requestFocus();
                                    break;
                                case PublicResultCode.LOGIN_INCORRECT_PASSWORD:
                                    mPasswordView.setError(result.getMessage());
                                    mPasswordView.requestFocus();
                                    break;
                            }
                    }
            }, LoginResult.class);
            request.startRequest();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    request.cancelRequest();
                }
            });
        }
    }

    private boolean isPhoneNumValid(String phoneNumber){
        for(int i = 0; i < phoneNumber.length(); i++){
            if(!(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9'))
                return false;
        }
        return true;
    }

}

