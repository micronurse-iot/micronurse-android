package org.micronurse.net.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;

import org.micronurse.R;
import org.micronurse.net.DataCorruptionException;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class HttpApiJsonRequest extends HttpApiRequest {
    private Object mRequestData;

    public HttpApiJsonRequest(final Context context, String url, int method, @Nullable String token, @Nullable Object requestData,
                              final HttpApiJsonListener listener) {
        super(context, url, method, token, new HttpListener() {
            @Override
            public void onResponse() {
                if(listener != null)
                    listener.onResponse();
            }

            @Override
            public void onDataResponse(@NonNull byte[] data) throws DataCorruptionException {
                if(listener != null)
                    listener.onDataResponse(data);
            }

            @Override
            public void onErrorResponse() {
                if(listener != null)
                    listener.onErrorResponse();
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                return listener != null && listener.onDataCorrupted(e);
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException {
                if(listener != null){
                    if(listener.onErrorDataResponse(statusCode, data))
                        return true;
                    if(statusCode == 401 && listener.getParsedData().getResultCode() == 401){
                        requireLoginAgain(context);
                    }
                    else if(statusCode != 500)
                        showError(context, listener.getParsedData().getMessage());
                    else
                        showError(context, context.getString(R.string.server_internal_error));
                }
                return false;
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                return listener != null && listener.onNetworkError(e);
            }
        });
        mRequestData = requestData;
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
}
