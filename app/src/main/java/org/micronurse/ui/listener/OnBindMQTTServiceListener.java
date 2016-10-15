package org.micronurse.ui.listener;

import org.micronurse.service.MQTTService;

/**
 * Created by zhou-shengyun on 16-10-14.
 */

public interface OnBindMQTTServiceListener {
    void onBind(MQTTService service);
}
