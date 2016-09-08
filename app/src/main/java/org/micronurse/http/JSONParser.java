package org.micronurse.http;

/**
 * Created by zhou-shengyun on 9/8/16.
 */
public interface JSONParser<T> {
    T fromJson(String jsonStr);
}
