package org.micronurse.net.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import org.micronurse.net.DataCorruptionException;
import org.micronurse.net.model.result.Result;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public abstract class HttpApiJsonListener<T extends Result> implements HttpListener {
    private final Class<T> resultType;
    private Result parsedData;

    @Override
    public void onResponse() {}

    @Override
    public void onErrorResponse() {}

    Result getParsedData() {
        return parsedData;
    }

    public HttpApiJsonListener(Class<T> resultType){
        this.resultType = resultType;
    }

    public abstract void onDataResponse(T data);

    @Override
    public void onDataResponse(@NonNull byte[] data) throws DataCorruptionException {
        try {
            parsedData = GsonUtil.getGson().fromJson(new String(data), resultType);
            onDataResponse((T)parsedData);
        }catch (JsonSyntaxException jse){
            throw new DataCorruptionException(jse.getMessage(), jse);
        }
    }

    public boolean onErrorDataResponse(int statusCode, Result errorInfo){
        return false;
    }

    @Override
    public final boolean onErrorDataResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException {
        try {
            parsedData = GsonUtil.getGson().fromJson(new String(data), Result.class);
            return onErrorDataResponse(statusCode, parsedData);
        }catch (JsonSyntaxException jse){
            throw new DataCorruptionException(jse.getMessage(), jse);
        }
    }

    @Override
    public boolean onDataCorrupted(Throwable e) {
        return false;
    }

    @Override
    public boolean onNetworkError(Throwable e) {
        return false;
    }
}
