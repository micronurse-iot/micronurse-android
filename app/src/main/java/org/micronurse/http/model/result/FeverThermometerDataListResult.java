package org.micronurse.http.model.result;

import org.micronurse.model.FeverThermometer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/24/16.
 */
public class FeverThermometerDataListResult extends Result {
    private List<FeverThermometer> dataList;

    public FeverThermometerDataListResult(int resultCode, String message, List<FeverThermometer> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<FeverThermometer> getDataList() {
        return dataList;
    }

    public void setDataList(List<FeverThermometer> dataList) {
        this.dataList = dataList;
    }
}
