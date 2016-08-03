package org.micronurse.util;

import android.text.TextUtils;
import android.widget.TextView;

import org.micronurse.R;

/**
 * Created by zhou-shengyun on 8/3/16.
 */
public class CheckUtil {
    public static boolean checkPhoneNumber(TextView v){
        v.setError(null);
        if(TextUtils.isEmpty(v.getText())){
            v.setError(v.getContext().getString(R.string.error_phone_number_required));
            v.requestFocus();
            return false;
        }
        if(!checkPhoneNumber(v.getText().toString())){
            v.setError(v.getContext().getString(R.string.error_invalid_phone_number));
            v.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean checkPhoneNumber(String phoneNumber){
        if(phoneNumber == null || phoneNumber.isEmpty())
            return false;
        for(int i = 0; i < phoneNumber.length(); i++){
            if(!(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9'))
                return false;
        }
        return true;
    }

    public static final int PASSWORD_MIN_LEN = 6;
    public static final int PASSWORD_MAX_LEN = 20;

    public static boolean checkPassword(TextView v){
        v.setError(null);
        if(TextUtils.isEmpty(v.getText())){
            v.setError(v.getContext().getString(R.string.error_password_required));
            v.requestFocus();
            return false;
        }
        if(!checkPassword(v.getText().toString())){
            v.setError(v.getContext().getString(R.string.error_password_invalid_len));
            v.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean checkPassword(String password){
        if(password == null)
            return false;
        return !(password.length() < PASSWORD_MIN_LEN || password.length() > PASSWORD_MAX_LEN);
    }

    public static boolean recheckPassword(TextView v1, TextView v2){
        v1.setError(null);
        v2.setError(null);
        if(!recheckPassword(v1.getText().toString(), v2.getText().toString())){
            v1.setError(v1.getContext().getString(R.string.error_password_inconstancy));
            v2.setError(v2.getContext().getString(R.string.error_password_inconstancy));
            v2.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean recheckPassword(String password1, String password2){
        if(password1 == null || password1.isEmpty() || password2 == null || password2.isEmpty())
            return false;
        return password1.equals(password2);
    }

    public static boolean checkNickname(TextView v){
        v.setError(null);
        if(TextUtils.isEmpty(v.getText())){
            v.setError(v.getContext().getString(R.string.error_nickname_empty));
            v.requestFocus();
            return false;
        }
        return checkNickname(v.getText().toString());
    }

    public static boolean checkNickname(String nickname){
        return !(nickname == null || nickname.isEmpty());
    }

    public static boolean checkCaptcha(TextView v){
        v.setError(null);
        if(TextUtils.isEmpty(v.getText())){
            v.setError(v.getContext().getString(R.string.error_captcha_empty));
            v.requestFocus();
            return false;
        }
        return checkCaptcha(v.getText().toString());
    }

    public static boolean checkCaptcha(String captcha){
        return !(captcha == null || captcha.isEmpty());
    }
}
