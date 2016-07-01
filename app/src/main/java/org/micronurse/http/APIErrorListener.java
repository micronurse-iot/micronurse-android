package org.micronurse.http;

import com.android.volley.VolleyError;

import org.micronurse.http.model.result.Result;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public interface APIErrorListener {
    void onErrorResponse(VolleyError err, Result result);
}
