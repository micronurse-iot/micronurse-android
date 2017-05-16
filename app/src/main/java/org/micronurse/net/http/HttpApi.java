package org.micronurse.net.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class HttpApi {
    public static String BASE_URL_V1;
    private static RequestQueue requestQueue = null;

    public static String getApiUrl(String... urlParam){
        String url = BASE_URL_V1;
        for(String s : urlParam){
            if(s != null && !s.isEmpty())
                url += '/' + s;
        }
        return url;
    }

    public static void startRequest(HttpApiRequest req){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(req.getContext());
        requestQueue.add(req);
    }

    public static class AccountAPI{
        public static final String CHECK_LOGIN = "account/check_login";
        public static final String LOGIN = "account/login";
        public static final String USER_BASIC_INFO_BY_PHONE = "account/user_basic_info/by_phone";
        public static final String REGISTER = "account/register";
        public static final String SEND_CAPTCHA = "account/send_captcha";
        public static final String LOGOUT = "account/logout";
        public static final String RESET_PASSWORD = "account/reset_password";
        public static final String GUARDIANSHIP = "account/guardianship";
        public static final String HOME_ADDRESS = "account/home_address";
        public static final String LOGIN_IOT = "account/iot_login";
        public static final String LOGOUT_IOT = "account/iot_logout";
    }

    public static class OlderAccountAPI{
        public static final String SET_HOME_LOCATION ="account/set_home_address";
    }

    public static class OlderFriendJuanAPI{
        public static final String FRIENDSHIP = "friend_juan/friendship";
        public static final String FRIEND_MOMENT ="friend_juan/moment";
        public static final String POST_FRIEND_MOMENT = "friend_juan/post_moment";
    }

    public static class SensorAPI{
        public static final String LATEST_SENSOR_DATA = "sensor/sensor_data/latest";
        public static final String LATEST_SENSOR_DATA_BY_NAME = "sensor/sensor_data/latest/by_name";
        public static final String SENSOR_WARNING = "sensor/warning";
        public static final String SENSOR_CONFIG = "sensor/config";
        public static final String CHANGE_SENSOR_CONFIG = "sensor/config/new";
    }
}
