package org.micronurse.net;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class DataCorruptionException extends Exception {
    public DataCorruptionException(Throwable cause){
        super(cause);
    }

    public DataCorruptionException(String message, Throwable cause){
        super(message, cause);
    }
}
