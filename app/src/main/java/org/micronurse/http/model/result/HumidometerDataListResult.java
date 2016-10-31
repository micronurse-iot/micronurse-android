package org.micronurse.http.model.result;

import org.micronurse.model.Humidometer;
import org.micronurse.model.Thermometer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/21/16.
 */
public class HumidometerDataListResult extends SensorDataListResult<Humidometer> {
    public HumidometerDataListResult(int resultCode, String message, List<Humidometer> dataList) {
        super(resultCode, message, dataList);
    }
}
