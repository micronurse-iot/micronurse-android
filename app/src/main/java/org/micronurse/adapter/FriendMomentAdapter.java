package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.model.FriendMoment;
import org.micronurse.model.User;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendMomentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<FriendMoment> momentList;
    private Context context;

    public FriendMomentAdapter(Context context, List<FriendMoment> momentList) {
        this.context = context;
        this.momentList = momentList;
    }

    public void setMomentList(List<FriendMoment> momentList) {
        this.momentList = momentList;
    }

    @Override
    public int getItemCount() {
        return momentList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendShareItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_moment, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof FriendShareItemViewHolder) {
            FriendShareItemViewHolder holder = (FriendShareItemViewHolder)viewHolder;
            FriendMoment momentItem = momentList.get(position);
            User u = GlobalInfo.findUserById(momentItem.getUserId());
            if(u == null)
                return;
            holder.momentPortrait.setImageBitmap(u.getPortrait());
            holder.momentUser.setText(u.getNickname());
            holder.momentTextContent.setText(momentItem.getTextContent());
            holder.momentTime.setText(DateTimeUtil.convertTimestamp(context, momentItem.getTimestamp()));
        }
    }

    private class FriendShareItemViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private CircleImageView momentPortrait;
        private TextView momentUser;
        private TextView momentTextContent;
        private TextView momentTime;

        public FriendShareItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            momentPortrait = (CircleImageView) itemView.findViewById(R.id.moment_portrait);
            momentTextContent = (TextView) itemView.findViewById(R.id.moment_text_content);
            momentUser = (TextView) itemView.findViewById(R.id.moment_user);
            momentTime = (TextView) itemView.findViewById(R.id.moment_time);
        }
    }
}
