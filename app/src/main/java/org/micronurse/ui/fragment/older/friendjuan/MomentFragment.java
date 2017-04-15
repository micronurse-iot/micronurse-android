package org.micronurse.ui.fragment.older.friendjuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.micronurse.R;
import org.micronurse.adapter.FriendMomentAdapter;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.FriendMomentListResult;
import org.micronurse.net.model.result.Result;
import org.micronurse.model.FriendMoment;
import org.micronurse.ui.activity.older.PostFriendMomentActivity;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;

import java.util.Calendar;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MomentFragment extends Fragment {
    private static final int MAX_MOMENT_NUM = 20;

    private View rootView;
    @BindView(R.id.txt_no_moment)
    TextView txtNoMoment;
    @BindView(R.id.btn_post_moment)
    FloatingActionButton btnPostMoment;
    @BindView(R.id.moment_list)
    SuperRecyclerView momentListView;

    private LinkedList<FriendMoment> momentList = new LinkedList<>();
    private FriendMomentAdapter momentAdapter;
    private Calendar endTime;

    public MomentFragment(){

    }

    public static MomentFragment getInstance(Context context){
        return new MomentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_friend_juan_moment, container, false);
        ButterKnife.bind(this, rootView);

        momentListView.getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
        momentListView.getRecyclerView().setNestedScrollingEnabled(false);
        momentListView.getSwipeToRefresh().setColorSchemeResources(R.color.colorAccent);

        btnPostMoment = (FloatingActionButton) rootView.findViewById(R.id.btn_post_moment);
        btnPostMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostFriendMomentActivity.class);
                startActivityForResult(intent, PostFriendMomentActivity.REQUEST_CODE_POST_MOMENT);
            }
        });

        momentAdapter = new FriendMomentAdapter(getActivity(), momentList);
        momentListView.setAdapter(momentAdapter);
        momentListView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMomentList(false);
            }
        });
        momentListView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int i, int i1, int i2) {
                getMomentList(true);
            }
        }, -1);

        momentListView.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && btnPostMoment.isShown())
                    btnPostMoment.hide();
                else if(dy < 0 && !btnPostMoment.isShown())
                    btnPostMoment.show();
            }
        });

        getMomentList(false);
        return rootView;
    }

    private void getMomentList(final boolean downloadMore){
        if(!downloadMore){
            endTime = Calendar.getInstance();
            momentListView.setRefreshing(true);
        }

        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), HttpApi.getApiUrl(HttpApi.OlderFriendJuanAPI.FRIEND_MOMENT, DateTimeUtil.getHttpTimestampStr(endTime.getTime()),
                String.valueOf(MAX_MOMENT_NUM)), Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<FriendMomentListResult>(FriendMomentListResult.class) {
                    @Override
                    public void onResponse() {
                        momentListView.setRefreshing(false);
                        momentListView.hideMoreProgress();
                    }

                    @Override
                    public void onDataResponse(FriendMomentListResult data) {
                        showContent(!downloadMore, true);
                        if(!downloadMore) {
                            momentList.clear();
                            momentAdapter.notifyDataSetChanged();
                        }
                        for(FriendMoment fm : data.getMomentList()){
                            momentList.add(fm);
                            momentAdapter.notifyItemInserted(momentList.size() - 1);
                            endTime.setTimeInMillis(fm.getTimestamp().getTime() - 1);
                        }
                        momentListView.setNumberBeforeMoreIsCalled((data.getMomentList().size() < MAX_MOMENT_NUM) ? -1 : 1);
                    }

                    @Override
                    public void onErrorResponse() {
                        showContent(!downloadMore, false);
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        if(errorInfo.getResultCode() == PublicResultCode.FRIEND_JUAN_NO_MOMENT){
                            momentListView.setNumberBeforeMoreIsCalled(-1);
                            return true;
                        }
                        return false;
                    }
                }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PostFriendMomentActivity.REQUEST_CODE_POST_MOMENT && resultCode == PostFriendMomentActivity.RESULT_CODE_POST_MOMENT_SUCCESSFULLY){
            getMomentList(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showContent(boolean refresh, boolean hasContent){
        if(refresh && !hasContent){
            momentListView.getRecyclerView().setVisibility(View.INVISIBLE);
            txtNoMoment.setVisibility(View.VISIBLE);
        }else if(refresh){
            momentListView.getRecyclerView().setVisibility(View.VISIBLE);
            txtNoMoment.setVisibility(View.GONE);
        }
    }
}

