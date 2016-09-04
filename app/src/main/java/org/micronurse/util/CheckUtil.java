package org.micronurse.util;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.Humidometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.Turgoscope;

import java.util.List;

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


    public static final int SAFETY_LEVEL_UNKNOWN = -1;
    public static final int SAFETY_LEVEL_SAFE = 1;
    public static final int SAFETY_LEVEL_HIDDEN_IN_DANGER = 2;
    public static final int SAFEFT_LEVEL_DANGER = 3;

    @SuppressWarnings("deprecation")
    private static int getSafetyLevelColor(Resources r, int safetyLevel){
        switch (safetyLevel){
            case SAFETY_LEVEL_SAFE:
                return r.getColor(R.color.green_500);
            case SAFETY_LEVEL_HIDDEN_IN_DANGER:
                return r.getColor(R.color.orange_500);
            case SAFEFT_LEVEL_DANGER:
                return r.getColor(R.color.red_500);
        }
        return r.getColor(R.color.grey_500);
    }

    public static int checkThermometerSafetyLevel(Thermometer thermometer){
        //TODO: Return the corresponding safe level value according to the temperature.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkThermometerSafetyLevel(TextView tv, Thermometer thermometer){
        int checkResult = checkThermometerSafetyLevel(thermometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkHumidometerSafetyLevel(Humidometer humidometer){
        //TODO: Return the corresponding safe level value according to the humidity.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkHumidometerSafetyLevel(TextView tv, Humidometer humidometer){
        int checkResult = checkHumidometerSafetyLevel(humidometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkSmokeTransducerSafetyLevel(SmokeTransducer smokeTransducer){
        //TODO: Return the corresponding safe level value according to the smoke.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkSmokeTransducerSafetyLevel(TextView tv, SmokeTransducer smokeTransducer){
        int checkResult = checkSmokeTransducerSafetyLevel(smokeTransducer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkFeverThermometerSafetyLevel(FeverThermometer feverThermometer){
        //TODO: Return the corresponding safe level value according to the body temperature.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkFeverThermometerSafetyLevel(TextView tv, FeverThermometer feverThermometer){
        int checkResult = checkFeverThermometerSafetyLevel(feverThermometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkPulseTransducerSafetyLevel(PulseTransducer pulseTransducer){
        //TODO: Return the corresponding safe level value according to the pulse.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkPulseTransducerSafetyLevel(TextView tv, PulseTransducer pulseTransducer){
        int checkResult = checkPulseTransducerSafetyLevel(pulseTransducer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkTurgoscopeSafetyLevel(Turgoscope turgoscope){
        //TODO: Return the corresponding safe level value according to the blood pressure.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkTurgoscopeSafetyLevel(TextView tv, Turgoscope turgoscope){
        int checkResult = checkTurgoscopeSafetyLevel(turgoscope);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkFamilySafetyLevel(@Nullable List<Thermometer> thermometer, @Nullable List<Humidometer> humidometer, @Nullable List<SmokeTransducer> smokeTransducer){
        //TODO: Check the family safety level.
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkFamilySafetyLevel(TextView tv, View bgView, @Nullable List<Thermometer> thermometer, @Nullable List<Humidometer> humidometer, @Nullable List<SmokeTransducer> smokeTransducer){
        int checkResult = checkFamilySafetyLevel(thermometer, humidometer, smokeTransducer);
        bgView.setBackgroundColor(getSafetyLevelColor(bgView.getResources(), checkResult));
        switch (checkResult){
            case SAFETY_LEVEL_SAFE:
                tv.setText(R.string.safe);
                break;
            case SAFETY_LEVEL_HIDDEN_IN_DANGER:
                tv.setText(R.string.hidden_in_danger);
                break;
            case SAFEFT_LEVEL_DANGER:
                tv.setText(R.string.danger);
                break;
        }
        return checkResult;
    }

    public static int checkHealthSafetyLevel(@Nullable FeverThermometer feverThermometer, @Nullable PulseTransducer pulseTransducer, @Nullable Turgoscope turgoscope){
        //TODO: Check the health safety level
        return SAFETY_LEVEL_SAFE;
    }

    public static int checkHealthSafetyLevel(TextView tv, View bgView, @Nullable FeverThermometer feverThermometer, @Nullable PulseTransducer pulseTransducer, @Nullable Turgoscope turgoscope){
        int checkResult = checkHealthSafetyLevel(feverThermometer, pulseTransducer, turgoscope);
        bgView.setBackgroundColor(getSafetyLevelColor(bgView.getResources(), checkResult));
        switch (checkResult){
            case SAFETY_LEVEL_SAFE:
                tv.setText(R.string.health_good);
                break;
            case SAFETY_LEVEL_HIDDEN_IN_DANGER:
                tv.setText(R.string.health_so);
                break;
            case SAFEFT_LEVEL_DANGER:
                tv.setText(R.string.health_bad);
                break;
        }
        return checkResult;
    }
}
