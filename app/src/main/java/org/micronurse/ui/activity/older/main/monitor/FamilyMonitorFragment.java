package org.micronurse.ui.activity.older.main.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.micronurse.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FamilyMonitorFragment extends Fragment {
    private ListView lv;
    private ArrayList<HashMap<String, String>>  listItem;
    private SwipeRefreshLayout refresh;
    public FamilyMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_older_family_monitor, container, false);
        refresh = (SwipeRefreshLayout)view.findViewById(R.id.swipeLayout);
        lv = (ListView)view.findViewById(R.id.lv);
        listItem = new ArrayList<HashMap<String, String>>();

        String []data={"安全", "温度", "卧室 25 摄氏度 2016-08-08", "厨房 26 摄氏度 2016-08-08","湿度",
                "卧室 70 % 2016-08-08", "厨房 70 % 2016-08-08"};
        int []type={0,1,2,2,1,2,2};
        int size=data.length;
        for(int i=0;i<size;i++){
            HashMap<String, String> map = new HashMap<String, String>();
//根据不同需求可以构造更复杂的数据,目前只构造一个数据
            map.put("data", data[i]);
            listItem.add(map);
        }
        FamilyMonitorAdapter listItemAdapter= new FamilyMonitorAdapter(getActivity(), listItem,type);
        lv.setAdapter(listItemAdapter);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //TODO refresh
            @Override
            public void onRefresh() {

                refresh.setRefreshing(false);
            }
        });


        return inflater.inflate(R.layout.fragment_older_family_monitor, container, false);
    }


}
