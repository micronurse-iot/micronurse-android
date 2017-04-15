package org.micronurse.net.model.result;

import org.micronurse.model.GPS;
import java.util.List;

/**
 * Created by zhou-shengyun on 9/3/16.
 */
public class GPSDataListResult extends SensorDataListResult<GPS>{
    public GPSDataListResult(int resultCode, String message, List<GPS> dataList) {
        super(resultCode, message, dataList);
    }
}
