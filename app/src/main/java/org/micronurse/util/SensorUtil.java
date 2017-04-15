package org.micronurse.util;

import org.micronurse.model.FeverThermometer;
import org.micronurse.model.GPS;
import org.micronurse.model.Humidometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.RawSensorData;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-04-14
 */
public class SensorUtil {
    public static Sensor parseRawSensorData(RawSensorData rawSensorData){
        try {
            if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_THERMOMETER))
                return new Thermometer(rawSensorData.getTimestamp(), rawSensorData.getName(), Float.valueOf(rawSensorData.getValue()));
            else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_HUMIDOMETER))
                return new Humidometer(rawSensorData.getTimestamp(), rawSensorData.getName(), Float.valueOf(rawSensorData.getValue()));
            else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER))
                return new SmokeTransducer(rawSensorData.getTimestamp(), rawSensorData.getName(), Integer.valueOf(rawSensorData.getValue()));
            else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_FEVER_THERMOMETER))
                return new FeverThermometer(rawSensorData.getTimestamp(), Float.valueOf(rawSensorData.getValue()));
            else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_PULSE_TRANSDUCER))
                return new PulseTransducer(rawSensorData.getTimestamp(), Integer.valueOf(rawSensorData.getValue()));
            else if(rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_GPS)){
                String[] splitStr = rawSensorData.getValue().split(",", 3);
                if(splitStr.length != 3)
                    return null;
                return new GPS(rawSensorData.getTimestamp(), Double.valueOf(splitStr[0]),
                        Double.valueOf(splitStr[1]), splitStr[2]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
