package org.micronurse.net.model.result;

import org.micronurse.model.Sensor;

import java.util.List;

/**
 * Created by zhou-shengyun on 16-10-30.
 */

public abstract class SensorDataListResult<T extends Sensor> extends Result {
    protected List<T> dataList;


    public SensorDataListResult(int resultCode, String message, List<T> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }
}
