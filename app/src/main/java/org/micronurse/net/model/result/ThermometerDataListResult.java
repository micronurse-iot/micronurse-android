package org.micronurse.net.model.result;

import org.micronurse.model.Thermometer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/21/16.
 */
public class ThermometerDataListResult extends SensorDataListResult<Thermometer> {
    public ThermometerDataListResult(int resultCode, String message, List<Thermometer> dataList) {
        super(resultCode, message, dataList);
    }
}
