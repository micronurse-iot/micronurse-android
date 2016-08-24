package org.micronurse.http.model.result;

import org.micronurse.model.PulseTransducer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/24/16.
 */
public class PulseTransducerDataListResult extends Result{
    private List<PulseTransducer> dataList;

    public PulseTransducerDataListResult(int resultCode, String message, List<PulseTransducer> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<PulseTransducer> getDataList() {
        return dataList;
    }

    public void setDataList(List<PulseTransducer> dataList) {
        this.dataList = dataList;
    }
}
