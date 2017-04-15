package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.micronurse.R;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.Humidometer;
import org.micronurse.model.InfraredTransducer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.ui.activity.MonitorDetailActivity;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.ImageUtil;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MonitorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TIMELINE_HEADER = 1;
    private static final int VIEW_TYPE_MONITOR_ITEM = 2;

    private boolean showTimeline;
    private List<Object> dataList;
    private Context context;

    public MonitorAdapter(Context context, List dataList, boolean showTimeline) {
        this.context = context;
        this.dataList = dataList;
        this.showTimeline = showTimeline;
    }

    public MonitorAdapter(Context context, List dataList) {
        this(context, dataList, false);
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
                return new TimelineHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_timeline_header, parent, false));
            case VIEW_TYPE_MONITOR_ITEM:
                return new SensorItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_monitor_data, parent, false));

        }
        return null;
    }

    public static void bindViewHolder(RecyclerView.ViewHolder viewHolder, Object data){
        bindViewHolder(viewHolder, data, false);
    }

    public static void bindViewHolder(RecyclerView.ViewHolder viewHolder, final Object data, boolean showTimeline){
        final Context context = viewHolder.itemView.getContext();
        if(data instanceof Date){
            TimelineHeaderViewHolder holder = (TimelineHeaderViewHolder)viewHolder;
            holder.txtDate.setText(DateTimeUtil.convertTimestamp(context, (Date) data, true, false));
            if(showTimeline){
                holder.itemView.findViewById(R.id.timeline_divider).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.timeline_circle).setVisibility(View.VISIBLE);
            }
        }
        else if(data instanceof Sensor) {
            final Intent intent = new Intent(context, MonitorDetailActivity.class);
            SensorItemViewHolder holder = (SensorItemViewHolder)viewHolder;
            if(showTimeline){
                holder.itemView.findViewById(R.id.timeline_divider).setVisibility(View.VISIBLE);
            }
            holder.txtDataUpdateTime.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp(), false, true, true));
            if (data instanceof Thermometer) {
                holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_gauge_teal_32dp));
                holder.txtDataValue.setText(String.format(context.getString(R.string.temperature_format), ((Thermometer) data).getTemperature()));
                CheckUtil.checkSafetyLevel(holder.txtDataValue, (Thermometer) data);
                holder.txtDataName.setText(((Thermometer) data).getName());
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_THERMOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Thermometer) data).getName());
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof Humidometer) {
                holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_humidity_indigo_32dp));
                holder.txtDataValue.setText(String.format(context.getString(R.string.humidity_format), ((Humidometer) data).getHumidity()));
                CheckUtil.checkSafetyLevel(holder.txtDataValue, (Humidometer) data);
                holder.txtDataName.setText(((Humidometer) data).getName());
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_HUMIDOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, ((Humidometer) data).getName());
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof SmokeTransducer) {
                holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_fire_amber_32dp));
                holder.txtDataValue.setText(String.format(context.getString(R.string.smoke_format), ((SmokeTransducer) data).getSmoke()));
                CheckUtil.checkSafetyLevel(holder.txtDataValue, (SmokeTransducer) data);
                holder.txtDataName.setText(((SmokeTransducer) data).getName());
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
                    holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_alarmlight_red_32dp));
                    holder.txtDataValue.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(),
                            ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_warning_red_32dp)), null, null, null);
                }
                holder.txtDataName.setText(((InfraredTransducer) data).getName());
            } else if (data instanceof FeverThermometer) {
                holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_thermometer_cyan_32dp));
                holder.txtDataName.setText(R.string.fever);
                holder.txtDataValue.setText(String.format(context.getString(R.string.temperature_format), ((FeverThermometer) data).getTemperature()));
                CheckUtil.checkSafetyLevel(holder.txtDataValue, (FeverThermometer) data);
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_FEVER_THERMOMETER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.fever));
                        context.startActivity(intent);
                    }
                });
            } else if (data instanceof PulseTransducer) {
                holder.iconSensor.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, R.drawable.ic_heartpulse_pink_32dp));
                holder.txtDataName.setText(R.string.pulse);
                holder.txtDataValue.setText(String.format(context.getString(R.string.pulse_format), ((PulseTransducer) data).getPulse()));
                CheckUtil.checkSafetyLevel(holder.txtDataValue, (PulseTransducer) data);
                holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_TYPE, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER);
                        intent.putExtra(MonitorDetailActivity.BUNDLE_KEY_SENSOR_NAME, context.getString(R.string.pulse));
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        bindViewHolder(viewHolder, dataList.get(position), showTimeline);
    }

    public static class TimelineHeaderViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private TextView txtDate;

        public TimelineHeaderViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
            txtDate = (TextView) itemView.findViewById(R.id.timeline_time);
        }
    }

    public static class SensorItemViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        @BindView(R.id.data_item_card)
        View itemCardView;
        @BindView(R.id.icon_sensor)
        ImageView iconSensor;
        @BindView(R.id.txt_data_value)
        TextView txtDataValue;
        @BindView(R.id.txt_data_name)
        TextView txtDataName;
        @BindView(R.id.txt_data_time)
        TextView txtDataUpdateTime;
        @BindView(R.id.txt_data_desc)
        TextView txtDataDesc;

        public SensorItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
