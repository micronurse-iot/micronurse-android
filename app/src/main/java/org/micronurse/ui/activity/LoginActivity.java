package org.micronurse.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.LoginRequest;
import org.micronurse.http.model.result.LoginResult;
import org.micronurse.http.model.result.Result;

/**
 * A login screen that offers login via phone number/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private boolean mLoginTask = false;

    // UI references.
    private AutoCompleteTextView mPhoneNumberView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mPhoneNumberView = (AutoCompleteTextView) findViewById(R.id.login_phone_number);
        mPasswordView = (EditText) findViewById(R.id.login_password);

        mPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT)
                    mPasswordView.requestFocus();
                return true;
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE)
                    attemptLogin();
                return true;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.button_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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
        String phoneNumber = mPhoneNumberView.getText().toString();
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
            final MicronurseAPI request = new MicronurseAPI(LoginActivity.this, "/v1/mobile/login", Request.Method.PUT, loginRequest, null,
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            progressDialog.dismiss();
                            LoginResult result = (LoginResult)response;
                            Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, new APIErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Result result) {
                            progressDialog.dismiss();
                            if(result == null)
                                return;
                            switch (result.getResultCode()){
                                case LoginResult.LOGIN_USER_NOT_EXISTS:
                                    mPhoneNumberView.setError(result.getMessage());
                                    mPhoneNumberView.requestFocus();
                                    break;
                                case LoginResult.LOGIN_PASSWORD_INCORRECT:
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

