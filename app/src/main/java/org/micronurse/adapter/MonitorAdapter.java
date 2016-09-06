package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import org.micronurse.ui.activity.MonitorDetailActivity;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;
import java.util.List;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.ViewHolder> {
    private List<Object> dataList;
    private Activity context;
    private Intent intent;

    public MonitorAdapter(Activity context, List dataList) {
        this.context = context;
        this.dataList = dataList;
        intent = new Intent(context, MonitorDetailActivity.class);
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
        final Object data = dataList.get(position);
        if(data instanceof Sensor){
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp()));
        }
        if(data instanceof Thermometer){
            holder.dataView.setText(String.valueOf(((Thermometer) data).getTemperature()) + "°C");
            CheckUtil.checkSafetyLevel(holder.dataView, (Thermometer) data);
            holder.dataNameView.setText(((Thermometer) data).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_THERMOMETER);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Thermometer) data).getName());
                    context.startActivity(intent);
                }
            });
        }else if(data instanceof Humidometer){
            holder.dataView.setText(String.valueOf(((Humidometer) data).getHumidity()) + '%');
            CheckUtil.checkSafetyLevel(holder.dataView, (Humidometer) data);
            holder.dataNameView.setText(((Humidometer) data).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_HUMIDOMETER);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Humidometer) data).getName());
                    context.startActivity(intent);
                }
            });
        }else if(data instanceof SmokeTransducer){
            holder.dataView.setText(String.valueOf(((SmokeTransducer) data).getSmoke()));
            CheckUtil.checkSafetyLevel(holder.dataView, (SmokeTransducer) data);
            holder.dataNameView.setText(((SmokeTransducer) data).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((SmokeTransducer) data).getName());
                    context.startActivity(intent);
                }
            });
        }else if(data instanceof FeverThermometer){
            holder.dataNameView.setText(R.string.fever);
            holder.dataView.setText(((FeverThermometer) data).getTemperature() + "°C");
            CheckUtil.checkSafetyLevel(holder.dataView, (FeverThermometer) data);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_FEVER_THERMOMETER);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.fever));
                    context.startActivity(intent);
                }
            });
        }else if(data instanceof PulseTransducer){
            holder.dataNameView.setText(R.string.pulse);
            holder.dataView.setText(((PulseTransducer) data).getPulse() + "bpm");
            CheckUtil.checkSafetyLevel(holder.dataView, (PulseTransducer) data);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.pulse));
                    context.startActivity(intent);
                }
            });
        }else if(data instanceof Turgoscope){
            holder.dataNameView.setText(R.string.blood_pressure);
            holder.dataView.setText(String.valueOf(((Turgoscope) data).getLowBloodPressure()) + '/' +
                                    ((Turgoscope) data).getHighBloodPressure() + "Pa");
            CheckUtil.checkSafetyLevel(holder.dataView, (Turgoscope) data);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_TURGOSCOPE);
                    intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.blood_pressure));
                    context.startActivity(intent);
                }
            });
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private TextView dataView;
        private TextView dataNameView;
        private TextView dataUpdateTimeView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            dataView = (TextView) itemView.findViewById(R.id.data);
            dataNameView = (TextView) itemView.findViewById(R.id.data_name);
            dataUpdateTimeView = (TextView) itemView.findViewById(R.id.data_update_time);
        }
    }
}
