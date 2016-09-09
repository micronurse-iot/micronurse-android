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
class JSONRequest<T extends Result> extends Request<T> {
    private Response.Listener<T> mListener;
    private Class<T> mResultClass;
    private Object mRequestData;
    private Map<String, String> mHeaderMap = new HashMap<>();
    private JSONParser<T> jsonParser;

    public JSONRequest(String url, int method, String token, Response.Listener<T> listener, Response.ErrorListener errorListener,
                       Object requestData, Class<T> resultType) {
        super(method, url, errorListener);
        mResultClass = resultType;
        mRequestData = requestData;
        mListener = listener;
        if(token != null && !token.isEmpty())
            mHeaderMap.put("Auth-Token", token);
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(mRequestData == null)
            return new byte[0];
        return GsonUtil.getGson().toJson(mRequestData).getBytes();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        mHeaderMap.putAll(super.getHeaders());
        return mHeaderMap;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if(response.statusCode == 204){
                return Response.success(null,
                        HttpHeaderParser.parseCacheHeaders(response));
            }
            if(jsonParser == null)
                return Response.success(GsonUtil.getGson().fromJson(jsonString, mResultClass),
                        HttpHeaderParser.parseCacheHeaders(response));
            else
                return Response.success(jsonParser.fromJson(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException jse) {
            return Response.error(new ParseError(jse));
        }
    }

    @Override
    protected void deliverResponse(T result) {
        mListener.onResponse(result);
    }

    public void setJsonParser(JSONParser<T> jsonParser) {
        this.jsonParser = jsonParser;
    }
}
