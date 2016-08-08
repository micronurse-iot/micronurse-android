package org.micronurse.ui.activity.older.main.monitor;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.micronurse.R;

import java.util.HashMap;
import java.util.List;

public class FamilyMonitorAdapter extends BaseAdapter {
    private List<HashMap<String, String>> list;
    private Context context;
    private int[] type;

    public FamilyMonitorAdapter(Context context, List<HashMap<String, String>> list,
                                int[] type) {
        this.context = context;
        this.list = list;
        this.type = type;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
        LayoutInflater mInflater = LayoutInflater.from(context);
        View view = null;
        if (type[position] == 0) {
            view = mInflater.inflate(R.layout.family_safe_level_item, null);
            TextView safe_level_view = (TextView) view.findViewById(R.id.safe_level);

            //TODO value and set the safe value
            safe_level_view.setText(R.string.family_monitor_safe_level + R.string.safe);
        } else if (type[position] == 1) {
            view = mInflater.inflate(R.layout.family_monitor_prompt_item, null);
            TextView prompt_view = (TextView) view.findViewById(R.id.prompt);
            if (position == 1)
                prompt_view.setText(R.string.prompt_temperature);
            else
                prompt_view.setText(R.string.prompt_humidity);
        } else {
            view = mInflater.inflate(R.layout.family_monitor_data_item, null);
//获取数据
            String content = list.get(position).get("data");
//分离数据
            String[] items = content.split(" ");

            TextView data = (TextView) view.findViewById(R.id.family_monitor_info);
            data.setText(items[0] + "：" + items[1] + items[2] + "更新于：" + items[3]);
        }

        return view;
    }
}
