package org.micronurse.adapter;

/**
 * Created by Lsq on 2016/10/25.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.model.User;
import android.widget.TextView;

import org.micronurse.R;
import java.util.List;

public class GuardianListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<User> dataList;
    private Context context;
    private OnItemClickListener listener;

    public GuardianListAdapter(Context context, List<User> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public void setDataList(List<User> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GuardianListsItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_guardians_lists, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final User data = dataList.get(position);
        GuardianListsItemViewHolder holder = (GuardianListsItemViewHolder) viewHolder;
        holder.nameView.setText(data.getNickname());
        holder.phoneNumberView.setText(data.getPhoneNumber());
        holder.itemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onItemClick(data);
            }
        });
    }

    public interface OnItemClickListener{
        void onItemClick(User guardian);
    }

    private class GuardianListsItemViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private View itemCardView;
        private TextView nameView;
        private TextView phoneNumberView;

        public GuardianListsItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemCardView = itemView.findViewById(R.id.guardian_lists_card);
            nameView = (TextView) itemView.findViewById(R.id.guardian_name);
            phoneNumberView = (TextView) itemView.findViewById(R.id.guardian_phone_number);
        }
    }

}
