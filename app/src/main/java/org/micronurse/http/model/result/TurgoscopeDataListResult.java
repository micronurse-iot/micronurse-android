package org.micronurse.http.model.result;

import org.micronurse.model.Turgoscope;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/24/16.
 */
public class TurgoscopeDataListResult extends Result {
    private List<Turgoscope> dataList;

    public TurgoscopeDataListResult(int resultCode, String message, List<Turgoscope> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<Turgoscope> getDataList() {
        return dataList;
    }

    public void setDataList(List<Turgoscope> dataList) {
        this.dataList = dataList;
    }
}
