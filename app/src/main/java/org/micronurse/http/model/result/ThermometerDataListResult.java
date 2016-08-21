package org.micronurse.http.model.result;

import org.micronurse.model.Thermometer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/21/16.
 */
public class ThermometerDataListResult extends Result {
    private List<Thermometer> dataList;

    public ThermometerDataListResult(int resultCode, String message, List<Thermometer> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<Thermometer> getDataList() {
        return dataList;
    }

    public void setDataList(List<Thermometer> dataList) {
        this.dataList = dataList;
    }
}
