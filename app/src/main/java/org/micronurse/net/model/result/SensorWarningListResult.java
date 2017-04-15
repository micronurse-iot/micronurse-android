package org.micronurse.net.model.result;

import org.micronurse.model.SensorWarning;

import java.util.List;

/**
 * Created by zhou-shengyun on 9/8/16.
 */
public class SensorWarningListResult extends Result {
    private List<SensorWarning> warningList;

    public SensorWarningListResult(int resultCode, String message, List<SensorWarning> warningList) {
        super(resultCode, message);
        this.warningList = warningList;
    }

    public List<SensorWarning> getWarningList() {
        return warningList;
    }

    public void setWarningList(List<SensorWarning> warningList) {
        this.warningList = warningList;
    }
}
