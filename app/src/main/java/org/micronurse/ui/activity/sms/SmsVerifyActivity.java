package org.micronurse.ui.activity.sms;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;

        import android.os.Bundle;
        import android.text.TextUtils;
        import android.view.KeyEvent;
        import android.view.View;
        import android.view.inputmethod.EditorInfo;
        import android.widget.AutoCompleteTextView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.micronurse.R;



public class SmsVerifyActivity extends AppCompatActivity{
    private AutoCompleteTextView phoneNumberView;
    private EditText cordView;
    private Button getCordButton;
    private Button saveCordButton;
    private Button cancelButton;

    private String phoneNumber = "";
    private  String cordContext = "";
    private boolean isCanceled = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verify);

        phoneNumberView=(AutoCompleteTextView) findViewById(R.id.phone_number);
        phoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                    attemptGetCord();
                return true;
            }
        });

        cordView=(EditText) findViewById(R.id.cord);
        cordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                    attemptVerify();
                return true;
            }
        });

        getCordButton = (Button) findViewById(R.id.getcord);
        getCordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {attemptGetCord();
            }
        });

        saveCordButton = (Button) findViewById(R.id.savecord);
        saveCordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {attemptVerify();
            }
        });
        saveCordButton.setEnabled(false);

        cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumberView.setText("");
                cordView.setText("");
                getCordButton.setEnabled(true);
                saveCordButton.setEnabled(false);
                cancelButton.setEnabled(false);
                phoneNumberView.requestFocus();
            }
        });
        cancelButton.setEnabled(false);

    }

    private void attemptGetCord(){
        phoneNumber = phoneNumberView.getText().toString().trim();
        checkPhoneNumber();
        if(!isCanceled) {
            cordView.requestFocus();
            getCordButton.setEnabled(false);
            saveCordButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
        //send cord to the phone??
    }
    private void attemptVerify(){
        phoneNumber = phoneNumberView.getText().toString().trim();
        cordContext = cordView.getText().toString().trim();
        checkPhoneNumber();
        if(!isCanceled){
            if(TextUtils.isEmpty(cordContext)){
                Toast.makeText(SmsVerifyActivity.this, "请输入验证码", Toast.LENGTH_LONG).show();
                cordView.requestFocus();
                isCanceled = true;
            }
            else{
                isCanceled = false;
                //verify??
                Intent intent = new Intent();
                intent.setClass(SmsVerifyActivity.this, ResetPasswordActivity.class);
                SmsVerifyActivity.this.startActivity(intent);
            }
        }
    }
    private void checkPhoneNumber(){
        if(!TextUtils.isEmpty(phoneNumber)){
            if(!isPhoneNumValid(phoneNumber)){
                Toast.makeText(SmsVerifyActivity.this, "请输入正确的手机号", Toast.LENGTH_LONG).show();
                phoneNumberView.setText("");
                phoneNumberView.requestFocus();
                isCanceled = true;
            }
            else{
                isCanceled = false;
            }
        }
        else{
            Toast.makeText(SmsVerifyActivity.this, "请输入您的手机号", Toast.LENGTH_LONG).show();
            phoneNumberView.requestFocus();
            isCanceled = true;
        }
    }
    private boolean isPhoneNumValid(String phoneNumber){
        int i;
        for(i = 0; i < phoneNumber.length(); i++){
            if(!(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9'))
                return false;
        }
        if (i != 11)
            return false;
        return true;
    }

}
