package org.micronurse.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author buptsse-zero <GGGZ-1101-28@Live.cn>
 */
public class GsonUtil {
    private static GsonBuilder getDefaultGsonBuilder(){
        return new GsonBuilder()
                   .setDateFormat("yyyy-MM-dd HH:mm:ss")
                   .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    }
    
    public static Gson getGson(){
        return getDefaultGsonBuilder().create();
    }
}
