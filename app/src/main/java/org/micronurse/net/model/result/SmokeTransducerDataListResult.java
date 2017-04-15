package org.micronurse.net.model.result;

import org.micronurse.model.SmokeTransducer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/21/16.
 */
public class SmokeTransducerDataListResult extends SensorDataListResult<SmokeTransducer> {
    public SmokeTransducerDataListResult(int resultCode, String message, List<SmokeTransducer> dataList) {
        super(resultCode, message, dataList);
    }
}
