package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.micronurse.R;
import org.micronurse.model.Humidometer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.util.DateTimeUtil;
import java.util.List;

public class FamilyMonitorAdapter extends RecyclerView.Adapter<FamilyMonitorAdapter.ViewHolder> {
    private List<Object> dataList;
    private Context context;

    public FamilyMonitorAdapter(Context context, List dataList) {
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
        if(data instanceof Thermometer){
            holder.dataView.setText(String.valueOf(((Thermometer) data).getTemperature()) + "°C");
            holder.dataNameView.setText(((Thermometer) data).getName());
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Thermometer) data).getTimestamp()));
        }else if(data instanceof Humidometer){
            holder.dataView.setText(String.valueOf(((Humidometer) data).getHumidity()) + '%');
            holder.dataNameView.setText(((Humidometer) data).getName());
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp()));
        }else if(data instanceof SmokeTransducer){
            holder.dataView.setText(String.valueOf(((SmokeTransducer) data).getSmoke()));
            holder.dataNameView.setText(((SmokeTransducer) data).getName());
            holder.dataUpdateTimeView.setText(DateTimeUtil.convertTimestamp(context, ((Sensor) data).getTimestamp()));
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
