package org.micronurse.ui.activity;
/**
 * Created by 1111 on 2016/7/30.
 */

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.LoginRequest;
import org.micronurse.http.model.result.LoginResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.UserResult;
import org.micronurse.model.User;
import org.micronurse.ui.activity.older.OlderMainActivity;
import org.micronurse.util.GlobalInfo;

public class RegisterActivity extends AppCompatActivity{

    // UI references.
    private AutoCompleteTextView actvPhoneNumberView;
    private EditText etPasswordView;
    private EditText etrPasswordView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.

        actvPhoneNumberView = (AutoCompleteTextView) findViewById(R.id.register_phone_number);
        etPasswordView = (EditText) findViewById(R.id.register_password);
        etrPasswordView = (EditText) findViewById(R.id.register_reconfirm_password);

        actvPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    if(checkPhonenumber())
                        etPasswordView.requestFocus();
                }
                return true;
            }
        });

        etPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_NEXT){
                    if(checkPassword())
                        etrPasswordView.requestFocus();
                }
                return true;
            }
        });

        etrPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE){
                    //if(recheckPassword())
                    //此处代码需补充

                }
                return true;
            }
        });
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid phone number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */


    private boolean isPhoneNumValid(String phoneNumber){
        for(int i = 0; i < phoneNumber.length(); i++){
            if(!(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9'))
                return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password){
        if(password.length()< 6 || password.length() > 20)
            return false;
        else
            return true;

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
        etPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = etPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            etPasswordView.setError(getString(R.string.error_password_required));
            focusView = etPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(password)) {
            etPasswordView.setError(getString(R.string.error_invalid_password));
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

    private boolean recheckPassword(){
        etrPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String rpassword = etrPasswordView.getText().toString();
        String password = etPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!rpassword.equals(password)){
            etrPasswordView.setError(getString(R.string.error_password_inconstancy));
            focusView = etrPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        }
        else
            return true;
    }

}
