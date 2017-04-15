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
        public static String CHECK_LOGIN = "account/check_login";
        public static String LOGIN = "account/login";
        public static String USER_BASIC_INFO_BY_PHONE = "account/user_basic_info/by_phone";
        public static String REGISTER = "account/register";
        public static String SEND_CAPTCHA = "account/send_captcha";
        public static String LOGOUT = "account/logout";
        public static String RESET_PASSWORD = "account/reset_password";
        public static String GUARDIANSHIP = "account/guardianship";
        public static String HOME_ADDRESS = "account/home_address";
    }

    public static class OlderAccountAPI{
        public static String SET_HOME_LOCATION ="account/set_home_address";
    }

    public static class OlderFriendJuanAPI{
        public static String FRIENDSHIP = "friend_juan/friendship";
        public static String FRIEND_MOMENT ="friend_juan/moment";
        public static String POST_FRIEND_MOMENT = "friend_juan/post_moment";
    }

    public static class SensorAPI{
        public static String LATEST_SENSOR_DATA = "sensor/sensor_data/latest";
        public static String LATEST_SENSOR_DATA_BY_NAME = "sensor/sensor_data/latest/by_name";
        public static String SENSOR_WARNING = "sensor/warning";
    }
}
