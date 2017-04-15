package org.micronurse.net.model.result;

import org.micronurse.model.PulseTransducer;

import java.util.List;

/**
 * Created by zhou-shengyun on 8/24/16.
 */
public class PulseTransducerDataListResult extends SensorDataListResult<PulseTransducer>{
    public PulseTransducerDataListResult(int resultCode, String message, List<PulseTransducer> dataList) {
        super(resultCode, message, dataList);
    }
}
