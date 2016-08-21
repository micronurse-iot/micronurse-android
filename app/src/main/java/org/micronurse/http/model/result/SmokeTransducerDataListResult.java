package org.micronurse.http.model.result;

import org.micronurse.model.SmokeTransducer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/21/16.
 */
public class SmokeTransducerDataListResult extends Result {
    public List<SmokeTransducer> dataList;

    public SmokeTransducerDataListResult(int resultCode, String message, List<SmokeTransducer> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<SmokeTransducer> getDataList() {
        return dataList;
    }

    public void setDataList(List<SmokeTransducer> dataList) {
        this.dataList = dataList;
    }
}
