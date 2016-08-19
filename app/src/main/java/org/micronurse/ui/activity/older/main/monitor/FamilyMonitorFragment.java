package org.micronurse.ui.activity.older.main.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.adapter.FamilyMonitorAdapter;
import org.micronurse.model.Humidometer;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;

import java.util.ArrayList;
import java.util.Date;

public class FamilyMonitorFragment extends Fragment {
    private View viewRoot;
    private TextView safeLevel;
    private RecyclerView temperatureList;
    private RecyclerView humidityList;
    private RecyclerView smokeList;
    private SwipeRefreshLayout refresh;

    public FamilyMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_older_family_monitor, container, false);
        safeLevel = (TextView)viewRoot.findViewById(R.id.safe_level);
        refresh = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        temperatureList = (RecyclerView) viewRoot.findViewById(R.id.temperature_list);
        temperatureList.setLayoutManager(new LinearLayoutManager(getContext()));
        temperatureList.setNestedScrollingEnabled(false);
        humidityList = (RecyclerView) viewRoot.findViewById(R.id.humidity_list);
        humidityList.setLayoutManager(new LinearLayoutManager(getContext()));
        humidityList.setNestedScrollingEnabled(false);
        smokeList = (RecyclerView) viewRoot.findViewById(R.id.smoke_list);
        smokeList.setLayoutManager(new LinearLayoutManager(getContext()));
        smokeList.setNestedScrollingEnabled(false);

        //Test Data
        ArrayList<Object> listItem = new ArrayList<>();
        listItem.add(new Thermometer(new Date().getTime(), "厨房", (float) 23.8));
        listItem.add(new Humidometer(new Date().getTime(), "卧室", (float) 76));
        listItem.add(new SmokeTransducer(new Date().getTime(), "客厅", 2000));
        FamilyMonitorAdapter listItemAdapter= new FamilyMonitorAdapter(getActivity(), listItem);
        temperatureList.setAdapter(listItemAdapter);
        humidityList.setAdapter(listItemAdapter);
        smokeList.setAdapter(listItemAdapter);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //TODO refresh
            @Override
            public void onRefresh() {
                refresh.setRefreshing(false);
            }
        });
        return viewRoot;
    }


}
