package org.micronurse.ui.activity.sms;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.micronurse.R;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText firstInputView;
    private EditText secondInputView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        firstInputView = (EditText)findViewById(R.id.first_input);
        secondInputView = (EditText)findViewById(R.id.second_input);
        Button resetButton = (Button) findViewById(R.id.reset);
        firstInputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT)
                    secondInputView.requestFocus();
                return true;
            }
        });
        secondInputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE)
                    attemptReset();
                return true;
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReset();
            }
        });
    }
    private void attemptReset(){
        String firstInput = firstInputView.getText().toString();
        String secondInput = secondInputView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(firstInput)) {
            Toast.makeText(ResetPasswordActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
            focusView = firstInputView;
            cancel = true;
        }
        if (TextUtils.isEmpty(secondInput)) {
            Toast.makeText(ResetPasswordActivity.this, "请再次输入密码", Toast.LENGTH_LONG).show();
            focusView = secondInputView;
            cancel = true;
        }
        if(!cancel){
            if(!firstInput.equals(secondInput)) {
                Toast.makeText(ResetPasswordActivity.this, "两次输入需一致", Toast.LENGTH_LONG).show();
                secondInputView.setText("");
                focusView = secondInputView;
                cancel = true;
            }
        }
        if (cancel) {
            // There was an error; don't attempt verify and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

        }
    }
}
