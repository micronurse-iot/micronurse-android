package org.micronurse.net.http;

import android.support.annotation.NonNull;

import org.micronurse.net.DataCorruptionException;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public interface HttpListener {
    /**
     * Invoked when request has finished
     */
    void onResponse();

    /**
     * Invoked after {@link #onResponse()} if server response is normal
     * @param data Response data
     * @throws DataCorruptionException If there is an error when parsing response data
     */
    void onDataResponse(@NonNull byte[] data) throws DataCorruptionException;

    /**
     * Invoked after {@link #onResponse()} if server response is abnormal or there is a network error
     */
    void onErrorResponse();

    /**
     * Invoked after {@link #onErrorResponse()} if server response is abnormal
     * @param statusCode HTTP status code
     * @param data Response data that contains corresponding error info
     * @return True if the error has been handled
     * @throws DataCorruptionException If there is an error when parsing response data
     */
    boolean onErrorDataResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException;

    /**
     * Invoked when {@link DataCorruptionException} is thrown
     * @param e Exception
     * @return True if the exception has been handled
     */
    boolean onDataCorrupted(Throwable e);

    /**
     * Invoked after {@link #onErrorResponse()} if there is an network error(no network, connection refused, connection timeout, etc)
     * @param e Exception
     * @return True if the exception has been handled
     */
    boolean onNetworkError(Throwable e);    //No network or connect timeout
}
