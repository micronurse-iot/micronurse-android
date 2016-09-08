package org.micronurse.http.model;

/**
 * Created by zhou-shengyun on 7/30/16.
 */

public class PublicResultCode {
    public static final int LOGIN_USER_NOT_EXIST = 200001;
    public static final int LOGIN_INCORRECT_PASSWORD = 200002;
    public static final int RESULT_NOT_FOUND = 200003;

    public static final int PHONE_NUM_INVALID = 200004;
    public static final int PHONE_NUM_REGISTERED = 200005;
    public static final int NICKNAME_REGISTERED = 200006;
    public static final int PASSWORD_LENGTH_ILLEGAL = 200007;
    public static final int PASSWORD_FORMAT_ILLEGAL = 200008;
    public static final int GENDER_ILLEGAL = 200009;
    public static final int ACCOUNT_TYPE_INVALID = 200010;

    public static final int PHONE_CAPTCHA_INCORRECT = 200011;
    public static final int PHONE_CAPTCHA_SEND_TOO_FREQUENTLY = 200012;
    public static final int PHONE_CAPTCHA_SEND_FAILED = 200013;

    public static final int MOBILE_SENSOR_TYPE_UNSUPPORTED = 200101;
    public static final int MOBILE_SENSOR_DATA_NOT_FOUND = 200102;
    public static final int MOBILE_SENSOR_WARNING_NOT_FOUND = 200103;

    public static final int MOBILE_GUARDIANSHIP_NOT_EXIST = 200201;
}
