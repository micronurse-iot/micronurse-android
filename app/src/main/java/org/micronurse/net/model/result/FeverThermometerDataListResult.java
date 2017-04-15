package org.micronurse.net.model.result;

import org.micronurse.model.FeverThermometer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/24/16.
 */
public class FeverThermometerDataListResult extends SensorDataListResult<FeverThermometer> {
    public FeverThermometerDataListResult(int resultCode, String message, List<FeverThermometer> dataList) {
        super(resultCode, message, dataList);
    }
}
