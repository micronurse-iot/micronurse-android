package org.micronurse.ui.listener;

import org.micronurse.model.RawSensorData;

/**
 * Created by zhou-shengyun on 16-10-20.
 */

public interface OnSensorDataReceivedListener {
    void onSensorDataReceived(RawSensorData rawSensorData);
}
