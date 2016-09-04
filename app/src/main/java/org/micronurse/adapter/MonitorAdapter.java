package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.micronurse.R;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.Humidometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.Turgoscope;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;
import java.util.List;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.ViewHolder> {
    private List<Object> dataList;
    private Context context;

    public MonitorAdapter(Context context, List dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.monitor_data_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object data = dataList.get(position);
        if(data instanceof Sensor){
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp()));
        }
        if(data instanceof Thermometer){
            holder.dataView.setText(String.valueOf(((Thermometer) data).getTemperature()) + "°C");
            CheckUtil.checkThermometerSafetyLevel(holder.dataView, (Thermometer) data);
            holder.dataNameView.setText(((Thermometer) data).getName());
        }else if(data instanceof Humidometer){
            holder.dataView.setText(String.valueOf(((Humidometer) data).getHumidity()) + '%');
            CheckUtil.checkHumidometerSafetyLevel(holder.dataView, (Humidometer) data);
            holder.dataNameView.setText(((Humidometer) data).getName());
        }else if(data instanceof SmokeTransducer){
            holder.dataView.setText(String.valueOf(((SmokeTransducer) data).getSmoke()));
            CheckUtil.checkSmokeTransducerSafetyLevel(holder.dataView, (SmokeTransducer) data);
            holder.dataNameView.setText(((SmokeTransducer) data).getName());
        }else if(data instanceof FeverThermometer){
            holder.dataNameView.setText(R.string.fever);
            holder.dataView.setText(((FeverThermometer) data).getTemperature() + "°C");
            CheckUtil.checkFeverThermometerSafetyLevel(holder.dataView, (FeverThermometer) data);
        }else if(data instanceof PulseTransducer){
            holder.dataNameView.setText(R.string.pulse);
            holder.dataView.setText(((PulseTransducer) data).getPulse() + "bpm");
            CheckUtil.checkPulseTransducerSafetyLevel(holder.dataView, (PulseTransducer) data);
        }else if(data instanceof Turgoscope){
            holder.dataNameView.setText(R.string.blood_pressure);
            holder.dataView.setText(((Turgoscope) data).getLowBloodPressure() + '/' +
                                    ((Turgoscope) data).getHighBloodPressure() + "Pa");
            CheckUtil.checkTurgoscopeSafetyLevel(holder.dataView, (Turgoscope) data);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView dataView;
        private TextView dataNameView;
        private TextView dataUpdateTimeView;

        public ViewHolder(View itemView) {
            super(itemView);
            dataView = (TextView) itemView.findViewById(R.id.data);
            dataNameView = (TextView) itemView.findViewById(R.id.data_name);
            dataUpdateTimeView = (TextView) itemView.findViewById(R.id.data_update_time);
        }
    }
}
