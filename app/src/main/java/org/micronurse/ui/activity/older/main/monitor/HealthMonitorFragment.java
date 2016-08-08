package org.micronurse.ui.activity.older.main.monitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
//import android.widget.Adapter;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//import java.util.Map;

import org.micronurse.R;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeLayout;
    private TextView mText1;
    private TextView mText2;
    private TextView mText3;

    public HealthMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setContentView(R.layout.fragment_older_health_monitor);
        mText1 = (TextView) getActivity().findViewById(R.id.text_temperature);
        mText2 = (TextView) getActivity().findViewById(R.id.text_pulse);
        mText3 = (TextView) getActivity().findViewById(R.id.text_blood_pressure);
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_list_item_1, android.R.id.text1, item_list);
        //mList.setAdapter(arrayAdapter);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            //TODO
            //体温、脉搏、血压三种数值的刷新操作
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        double d1 = 36 + 2 * Math.random();
                        double d2 = 62 + 5 * Math.random();
                        double d3 = 60 + 20 * Math.random();
                        double d4 = 120 + 20 * Math.random();

                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date=new Date();
                        String time_str=sdf.format(date);

                        //显示伪数据，用当前时间作为时间戳
                        mText1.setText("体温： " + String.valueOf(d1) + "°C" + "  时间：" + time_str);
                        mText2.setText("脉搏： " + String.valueOf(d2) + "次/分" + "  时间：" + time_str);
                        mText3.setText("血压： " + String.valueOf(d3) + "/" + String.valueOf(d4) + "mmHg" + "  时间：" + time_str);
                    }
                }, 3000);
            }
        });

        swipeLayout.setProgressViewEndTarget(true, 10);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_older_family_monitor, container, false);
    }


    //TODO
    public void onRefresh() {
    }

}

