package org.micronurse.ui.listener;

import org.micronurse.model.Sensor;

/**
 * Created by zhou-shengyun on 16-10-20.
 */

public interface OnSensorDataReceivedListener {
    void onSensorDataReceived(Sensor sensor);
}
