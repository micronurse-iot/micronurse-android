package org.micronurse.ui.activity;

/**
 * Created by 1111 on 2016/8/6.
 */
import org.micronurse.R;
import java.util.ArrayList;
import java.util.HashMap;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class GuardThiefMonitorFragment extends  AppCompatActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_older_guardtheif_monitor);
        //绑定ListView控件
        ListView list = (ListView) findViewById(R.id.monitor_sites_listView);
        //绑定刷新控件
       SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.id_swipe_guardThief);

        //生成动态数组，加入数据，比如监测地点有厨房，阳台，客厅，卧室
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> map = new HashMap<String, Object>();

        //TODO
        //map.put("ItemImage",); //卧室，厨房等区域的安卓图像资源
        map.put("ItemText", "阳台");

        //生成适配器的Item和动态数组对应元素
        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,//数据源
                R.layout.guardthief_monitor_listview_item,//ListItem的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"ItemImage", "ItemText"},
                //ImageItem的XML文件里面的一个ImageView,一个TextView ID
                new int[]{R.id.ItemImage, R.id.ItemText}
        );

        //添加并显示
        list.setAdapter(listItemAdapter);

        //TODO
        //添加点击,显示具体监控的日期信息，如：2016.8.6号
       /* list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(){
             setTitle("警报信息")
             setMessage("2016.8.6")
            setPositiveButton("确定", );
            }
        });*/

        //TODO
        //刷新接口
       // swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener(){});

    }

}
