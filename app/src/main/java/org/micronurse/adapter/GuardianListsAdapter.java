package org.micronurse.adapter;

/**
 * Created by Lsq on 2016/10/25.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import org.micronurse.model.User;
import android.widget.TextView;
import android.widget.Toast;

import org.micronurse.R;
import java.util.List;


public class GuardianListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> dataList;
    private Context context;
    private Intent intent;

    public GuardianListsAdapter(Context context, List dataList) {
        this.context = context;
        this.dataList = dataList;
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

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if(dataList != null){
            final User data = dataList.get(position);
            GuardianListsItemViewHolder holder = (GuardianListsItemViewHolder) viewHolder;
            holder.nameView.setText(data.getNickname());
            holder.phoneNumberView.setText(data.getPhoneNumber());
            holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + data.getPhoneNumber()));
                    context.startActivity(intent);
                }
            });
        }
        else {
            Toast toast = Toast.makeText(context, "没有查找到监护人列表",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
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
