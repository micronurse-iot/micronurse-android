package org.micronurse.ui.listener;

import android.content.Context;

/**
 * Created by zhou-shengyun on 16-10-2.
 */

public interface OnMessageArrivedListener {
    void onMessageArrived(Context context, String topic, String topicUserId, String message);
}
