package org.micronurse.util;

import android.content.res.Resources;
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
    public static final int SAFETY_LEVEL_DANGER = 3;

    @SuppressWarnings("deprecation")
    private static int getSafetyLevelColor(Resources r, int safetyLevel){
        switch (safetyLevel){
            case SAFETY_LEVEL_SAFE:
                return r.getColor(R.color.green_500);
            case SAFETY_LEVEL_HIDDEN_IN_DANGER:
                return r.getColor(R.color.orange_500);
            case SAFETY_LEVEL_DANGER:
                return r.getColor(R.color.red_500);
        }
        return r.getColor(R.color.grey_500);
    }

    public static int checkSafetyLevel(Thermometer thermometer){
        float temperature = thermometer.getTemperature();
        if(temperature >= 12.0 && temperature <= 38.0)
            return SAFETY_LEVEL_SAFE;
        else if(temperature >= 54.0)
            return SAFETY_LEVEL_DANGER;
        else
            return SAFETY_LEVEL_HIDDEN_IN_DANGER;
    }

    public static int checkSafetyLevel(TextView tv, Thermometer thermometer){
        int checkResult = checkSafetyLevel(thermometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkSafetyLevel(Humidometer humidometer){
        float humidity = humidometer.getHumidity();
        if(humidity >= 20 && humidity <= 80)
            return SAFETY_LEVEL_SAFE;
        else if(humidity >= 90 || humidity <= 10)
            return SAFETY_LEVEL_DANGER;
        else
            return SAFETY_LEVEL_HIDDEN_IN_DANGER;
    }

    public static int checkSafetyLevel(TextView tv, Humidometer humidometer){
        int checkResult = checkSafetyLevel(humidometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkSafetyLevel(SmokeTransducer smokeTransducer){
        int smoke = smokeTransducer.getSmoke();
        if(smoke <= 50)
            return SAFETY_LEVEL_SAFE;
        else if(smoke > 50 && smoke < 300)
            return SAFETY_LEVEL_HIDDEN_IN_DANGER;
        else
            return SAFETY_LEVEL_DANGER;

    }

    public static int checkSafetyLevel(TextView tv, SmokeTransducer smokeTransducer){
        int checkResult = checkSafetyLevel(smokeTransducer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkSafetyLevel(FeverThermometer feverThermometer){
        float bodyHeat = feverThermometer.getTemperature();
        if(bodyHeat >= 36.0 && bodyHeat <= 37.0)
            return SAFETY_LEVEL_SAFE;
        else if(bodyHeat <= 35.5 || bodyHeat >= 37.3)
            return SAFETY_LEVEL_DANGER;
        else
            return SAFETY_LEVEL_HIDDEN_IN_DANGER;
    }

    public static int checkSafetyLevel(TextView tv, FeverThermometer feverThermometer){
        int checkResult = checkSafetyLevel(feverThermometer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }

    public static int checkSafetyLevel(PulseTransducer pulseTransducer){
        int pulse = pulseTransducer.getPulse();
        if(pulse >= 55 && pulse <= 100)
            return SAFETY_LEVEL_SAFE;
        else if(pulse <= 45 || pulse >= 110)
            return SAFETY_LEVEL_DANGER;
        else
            return SAFETY_LEVEL_HIDDEN_IN_DANGER;

    }

    public static int checkSafetyLevel(TextView tv, PulseTransducer pulseTransducer){
        int checkResult = checkSafetyLevel(pulseTransducer);
        tv.setTextColor(getSafetyLevelColor(tv.getResources(), checkResult));
        return checkResult;
    }


    public static int checkFamilySafetyLevel(@Nullable List<Thermometer> thermometer, @Nullable List<Humidometer> humidometer, @Nullable List<SmokeTransducer> smokeTransducer){
        int result = SAFETY_LEVEL_UNKNOWN;

        if(thermometer != null){
            for(Thermometer t : thermometer){
                result = checkSafetyLevel(t);
                if(result == SAFETY_LEVEL_HIDDEN_IN_DANGER || result == SAFETY_LEVEL_DANGER)
                    return result;
            }
        }

        if(humidometer != null){
            for(Humidometer h : humidometer){
                result = checkSafetyLevel(h);
                if(result == SAFETY_LEVEL_HIDDEN_IN_DANGER || result == SAFETY_LEVEL_DANGER)
                    return result;
            }
        }

        if(smokeTransducer != null){
            for(SmokeTransducer st : smokeTransducer){
                result = checkSafetyLevel(st);
                if(result == SAFETY_LEVEL_HIDDEN_IN_DANGER || result == SAFETY_LEVEL_DANGER)
                    return result;
            }
        }

        return result;
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
            case SAFETY_LEVEL_UNKNOWN:
                tv.setText(R.string.unknown);
                break;
            case SAFETY_LEVEL_DANGER:
                tv.setText(R.string.danger);
                break;
        }
        return checkResult;
    }

    public static int checkHealthSafetyLevel(@Nullable FeverThermometer feverThermometer, @Nullable PulseTransducer pulseTransducer){
        int result = SAFETY_LEVEL_UNKNOWN;
        if(feverThermometer != null){
            result = checkSafetyLevel(feverThermometer);
            if(result == SAFETY_LEVEL_HIDDEN_IN_DANGER || result == SAFETY_LEVEL_DANGER)
                return result;
        }

        if(pulseTransducer != null){
            result = checkSafetyLevel(pulseTransducer);
            if(result == SAFETY_LEVEL_HIDDEN_IN_DANGER || result == SAFETY_LEVEL_DANGER)
                return result;
        }

        return result;

    }

    public static int checkHealthSafetyLevel(TextView tv, View bgView, @Nullable FeverThermometer feverThermometer, @Nullable PulseTransducer pulseTransducer){
        int checkResult = checkHealthSafetyLevel(feverThermometer, pulseTransducer);
        bgView.setBackgroundColor(getSafetyLevelColor(bgView.getResources(), checkResult));
        switch (checkResult){
            case SAFETY_LEVEL_SAFE:
                tv.setText(R.string.health_good);
                break;
            case SAFETY_LEVEL_HIDDEN_IN_DANGER:
                tv.setText(R.string.health_so);
                break;
            case SAFETY_LEVEL_UNKNOWN:
                tv.setText(R.string.unknown);
                break;
            case SAFETY_LEVEL_DANGER:
                tv.setText(R.string.health_bad);
                break;
        }
        return checkResult;
    }
}
