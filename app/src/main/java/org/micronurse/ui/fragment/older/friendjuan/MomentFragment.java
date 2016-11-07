package org.micronurse.ui.fragment.older.friendjuan;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;

import org.micronurse.R;
import org.micronurse.adapter.FriendMomentAdapter;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.FriendMomentListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.FriendMoment;
import org.micronurse.ui.activity.older.PostFriendMomentActivity;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class MomentFragment extends Fragment {
    private static final int MAX_MOMENT_NUM = 20;

    private View viewRoot;
    private FloatingActionButton btnPostMoment;

    private LinkedList<FriendMoment> momentList = new LinkedList<>();
    private FriendMomentAdapter momentAdapter;
    private LRecyclerViewAdapter mLRecyclerViewAdapter;
    private LRecyclerView momentListView;
    private Calendar endTime;

    public MomentFragment(){
        endTime = Calendar.getInstance();
    }

    public static MomentFragment getInstance(Context context){
        return new MomentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_friend_juan_moment, container, false);
        momentListView = (LRecyclerView) viewRoot.findViewById(R.id.moment_list);
        momentListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        btnPostMoment = (FloatingActionButton) viewRoot.findViewById(R.id.btn_post_moment);
        btnPostMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostFriendMomentActivity.class);
                startActivityForResult(intent, PostFriendMomentActivity.REQUEST_CODE_POST_MOMENT);
            }
        });

        momentAdapter = new FriendMomentAdapter(getActivity(), momentList);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(momentAdapter);
        momentListView.setAdapter(mLRecyclerViewAdapter);
        momentListView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                getMomentList(true);
            }
        });

        momentListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMomentList(false);
            }
        });

        momentListView.setLScrollListener(new LRecyclerView.LScrollListener() {
            @Override
            public void onScrollUp() {
                btnPostMoment.hide();
            }

            @Override
            public void onScrollDown() {
                btnPostMoment.show();
            }

            @Override
            public void onScrolled(int i, int i1) {}

            @Override
            public void onScrollStateChanged(int i) {}
        });

        getMomentList(false);
        return viewRoot;
    }

    private void getMomentList(final boolean downloadMore){
        if(downloadMore) {
            RecyclerViewStateUtils.setFooterViewState(momentListView, LoadingFooter.State.Loading);
            momentListView.forceToRefresh();
        } else {
            endTime.setTimeInMillis(System.currentTimeMillis());
        }

        new MicronurseAPI<FriendMomentListResult>(getContext(),
                MicronurseAPI.getApiUrl(MicronurseAPI.OlderFriendJuanAPI.FRIEND_MOMENT, String.valueOf(endTime.getTimeInMillis()), String.valueOf(MAX_MOMENT_NUM)),
                Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FriendMomentListResult>() {
            @Override
            public void onResponse(FriendMomentListResult response) {
                viewRoot.findViewById(R.id.txt_no_moment).setVisibility(View.GONE);
                if(!downloadMore) {
                    momentList.clear();
                    momentList.addAll(response.getMomentList());
                    momentListView.refreshComplete();
                    mLRecyclerViewAdapter.notifyDataSetChanged();
                }else{
                    momentList.addAll(response.getMomentList());
                    mLRecyclerViewAdapter.notifyItemInserted(momentList.size() - 1);
                    if(response.getMomentList().size() < MAX_MOMENT_NUM)
                        RecyclerViewStateUtils.setFooterViewState(momentListView, LoadingFooter.State.TheEnd);
                    else
                        RecyclerViewStateUtils.setFooterViewState(momentListView, LoadingFooter.State.Normal);
                }
                endTime.setTimeInMillis(momentList.get(momentList.size() - 1).getTimestamp() - 1);
            }
        }, new APIErrorListener() {
            @Override
            public boolean onErrorResponse(VolleyError err, Result result) {
                if(!downloadMore)
                    momentListView.refreshComplete();
                if(result != null && result.getResultCode() == PublicResultCode.FRIEND_JUAN_NO_MOMENT){
                    if(downloadMore)
                        RecyclerViewStateUtils.setFooterViewState(momentListView, LoadingFooter.State.TheEnd);
                    return true;
                }else{
                    if(downloadMore)
                        RecyclerViewStateUtils.setFooterViewState(getActivity(), momentListView, momentList.size(), LoadingFooter.State.NetWorkError, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getMomentList(true);
                            }
                        });
                }
                return false;
            }
        }, FriendMomentListResult.class, false, null).startRequest();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PostFriendMomentActivity.REQUEST_CODE_POST_MOMENT && resultCode == PostFriendMomentActivity.RESULT_CODE_POST_MOMENT_SUCCESSFULLY){
            getMomentList(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

