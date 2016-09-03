package org.micronurse.http.model.result;

import org.micronurse.model.GPS;
import java.util.List;

/**
 * Created by zhou-shengyun on 9/3/16.
 */
public class GPSDataListResult extends Result{
    private List<GPS> dataList;

    public GPSDataListResult(int resultCode, String message, List<GPS> dataList) {
        super(resultCode, message);
        this.dataList = dataList;
    }

    public List<GPS> getDataList() {
        return dataList;
    }

    public void setDataList(List<GPS> dataList) {
        this.dataList = dataList;
    }
}
