package org.micronurse.http;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import org.micronurse.http.model.result.Result;
import org.micronurse.util.GsonUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
class JSONRequest extends Request<Result> {
    private Response.Listener<Result> mListener;
    private Class<? extends Result> mResultType;
    private Object mRequestData;
    private Map<String, String> mHeaderMap = new HashMap<>();

    public JSONRequest(String url, int method, String token, Response.Listener<Result> listener, Response.ErrorListener errorListener,
                       Object requestData, Class<? extends Result> resultType) {
        super(method, url, errorListener);
        mRequestData = requestData;
        mListener = listener;
        mResultType = resultType;
        if(token != null && !token.isEmpty())
            mHeaderMap.put("Auth-Token", token);
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return GsonUtil.getGson().toJson(mRequestData).getBytes();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        mHeaderMap.putAll(super.getHeaders());
        return mHeaderMap;
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
