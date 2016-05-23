package org.micronurse.http;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import org.micronurse.http.model.Result;
import org.micronurse.util.GsonUtil;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
class PostRequest extends Request<Result> {
    private Map<String, String> mMap;
    private Response.Listener<Result> mListener;
    private Class<? extends Result> mResultType;

    public PostRequest(String url, Response.Listener<Result> listener, Response.ErrorListener errorListener,
                       Map<String, String> map, Class<? extends Result> resultType) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mMap = map;
        mResultType = resultType;
    }

    @Override
    protected Map<String, String> getParams() {
        return mMap;
    }

    @Override
    protected Response<Result> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(GsonUtil.getGson().fromJson(jsonString, mResultType),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException jse) {
            return Response.error(new ParseError(jse));
        }
    }

    @Override
    protected void deliverResponse(Result result) {
        mListener.onResponse(result);
    }
}
