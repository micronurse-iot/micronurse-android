package org.micronurse.adapter;

import android.annotation.SuppressLint;
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
import org.micronurse.model.InfraredTransducer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.Turgoscope;
import org.micronurse.ui.activity.MonitorDetailActivity;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;

import java.util.Date;
import java.util.List;

public class MonitorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TIMELINE_HEADER = 1;
    private static final int VIEW_TYPE_MONITOR_ITEM = 2;

    private boolean showTimeline;
    private List<Object> dataList;
    private Context context;
    private Intent intent;
    private String txtDatatime;

    public MonitorAdapter(Context context, List dataList, boolean showTimeline) {
        this.context = context;
        this.dataList = dataList;
        this.showTimeline = showTimeline;
        if (context != null) {
            txtDatatime = context.getString(R.string.update_time);
            intent = new Intent(context, MonitorDetailActivity.class);
        }
    }

    public MonitorAdapter(Context context, List dataList) {
        this(context, dataList, false);
    }

    public void setDataList(List<Object> dataList) {
        this.dataList = dataList;
    }

    public void setTxtDatatime(String txtDatatime) {
        this.txtDatatime = txtDatatime;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(dataList.get(position) instanceof Date)
            return VIEW_TYPE_TIMELINE_HEADER;
        else if(dataList.get(position) instanceof Sensor)
            return VIEW_TYPE_MONITOR_ITEM;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_TIMELINE_HEADER:
                return new TimelineHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.timeline_header, parent, false));
            case VIEW_TYPE_MONITOR_ITEM:
                return new SensorItemViewHolder(LayoutInflater.from(context).inflate(R.layout.monitor_data_item, parent, false));

        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Object data = dataList.get(position);
        if(data instanceof Date){
            TimelineHeaderViewHolder holder = (TimelineHeaderViewHolder)viewHolder;
            holder.timeView.setText(DateTimeUtil.convertTimestamp(context, (Date) data, true, false));
            if(showTimeline){
                holder.itemView.findViewById(R.id.timeline_divider).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.timeline_circle).setVisibility(View.VISIBLE);
            }
        }
        else if(data instanceof Sensor) {
            SensorItemViewHolder holder = (SensorItemViewHolder)viewHolder;
            ((TextView)holder.itemView.findViewById(R.id.txt_data_time)).setText(txtDatatime);
            if(showTimeline){
                holder.itemView.findViewById(R.id.timeline_divider).setVisibility(View.VISIBLE);
            }
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp()));
            if (data instanceof Thermometer) {
                holder.dataView.setText(String.valueOf(((Thermometer) data).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel(holder.dataView, (Thermometer) data);
                holder.dataNameView.setText(((Thermometer) data).getName());
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_THERMOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Thermometer) data).getName());
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof Humidometer) {
                holder.dataView.setText(String.valueOf(((Humidometer) data).getHumidity()) + '%');
                CheckUtil.checkSafetyLevel(holder.dataView, (Humidometer) data);
                holder.dataNameView.setText(((Humidometer) data).getName());
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_HUMIDOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Humidometer) data).getName());
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof SmokeTransducer) {
                holder.dataView.setText(String.valueOf(((SmokeTransducer) data).getSmoke()) + "ppm");
                CheckUtil.checkSafetyLevel(holder.dataView, (SmokeTransducer) data);
                holder.dataNameView.setText(((SmokeTransducer) data).getName());
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((SmokeTransducer) data).getName());
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof InfraredTransducer){
                if(((InfraredTransducer) data).isWarning()) {
                    holder.dataView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning_red_32dp, 0, 0, 0);
                }
                holder.dataNameView.setText(((InfraredTransducer) data).getName());
            } else if (data instanceof FeverThermometer) {
                holder.dataNameView.setText(R.string.fever);
                holder.dataView.setText(((FeverThermometer) data).getTemperature() + "°C");
                CheckUtil.checkSafetyLevel(holder.dataView, (FeverThermometer) data);
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_FEVER_THERMOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.fever));
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof PulseTransducer) {
                holder.dataNameView.setText(R.string.pulse);
                holder.dataView.setText(((PulseTransducer) data).getPulse() + "bpm");
                CheckUtil.checkSafetyLevel(holder.dataView, (PulseTransducer) data);
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.pulse));
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof Turgoscope) {
                holder.dataNameView.setText(R.string.blood_pressure);
                holder.dataView.setText(String.valueOf(((Turgoscope) data).getLowBloodPressure()) + '/' +
                        ((Turgoscope) data).getHighBloodPressure() + "Pa");
                CheckUtil.checkSafetyLevel(holder.dataView, (Turgoscope) data);
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_TURGOSCOPE);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.blood_pressure));
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    class TimelineHeaderViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private TextView timeView;

        public TimelineHeaderViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
            timeView = (TextView) itemView.findViewById(R.id.timeline_time);
        }
    }

    class SensorItemViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private View itemCardView;
        private TextView dataView;
        private TextView dataNameView;
        private TextView dataUpdateTimeView;

        public SensorItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemCardView = itemView.findViewById(R.id.data_item_card);
            dataView = (TextView) itemView.findViewById(R.id.data);
            dataNameView = (TextView) itemView.findViewById(R.id.data_name);
            dataUpdateTimeView = (TextView) itemView.findViewById(R.id.data_update_time);
        }
    }
}
