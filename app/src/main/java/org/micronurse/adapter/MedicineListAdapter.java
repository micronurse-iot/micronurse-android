package org.micronurse.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.micronurse.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lsq on 2016/10/31.
 */
public class MedicineListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Integer> medicineList = new ArrayList<>();
    private OnItemNameClickListener itemNameClickListener;
    private OnItemUsageClickListener itemUsageClickListener;
    private OnSetClockClickListener setClockClickListener;
    private OnCancelClockClickListener cancelClockClickListener;

    public MedicineListAdapter(Context context, List<Integer> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    public void setOnItemNameClickListener(OnItemNameClickListener itemNameClickListener){
        this.itemNameClickListener = itemNameClickListener;
    }

    public void setOnItemUsageClickListener(OnItemUsageClickListener itemUsageClickListener){
        this.itemUsageClickListener = itemUsageClickListener;
    }

    public void setOnSetClockClickListener(OnSetClockClickListener setClockClickListener){
        this.setClockClickListener = setClockClickListener;
    }

    public void setOnCancelClockClickListener(OnCancelClockClickListener cancelClockClickListener){
        this.cancelClockClickListener = cancelClockClickListener;
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MedicineListsItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_medication_data, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final Integer id = medicineList.get(position);
        MedicineListsItemViewHolder holder = (MedicineListsItemViewHolder) viewHolder;
        holder.nameView.setText(" ");
        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemNameClickListener != null)
                    itemNameClickListener.onNameTextViewClick();
            }
        });

        holder.usageView.setText(" ");
        holder.usageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(itemUsageClickListener != null)
                    itemUsageClickListener.onUsageTextViewClick();
            }
        });

        holder.setClockButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                setClockClickListener.onSetClockClick(id);
            }
        });

        holder.cancelClockButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                cancelClockClickListener.onCancelClockClick(id);
            }
        });
    }

    public interface OnItemNameClickListener{
        void onNameTextViewClick();
    }

    public interface OnItemUsageClickListener{
        void onUsageTextViewClick();
    }

    public interface OnSetClockClickListener{
        void onSetClockClick(int id);
    }

    public interface OnCancelClockClickListener{
        void onCancelClockClick(int id);
    }

    private class MedicineListsItemViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private View itemCardView;
        private TextView nameView;
        private TextView usageView;
        private Button setClockButton;
        private Button cancelClockButton;

        public MedicineListsItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemCardView = itemView.findViewById(R.id.medicine_data_item_card);
            nameView = (TextView) itemView.findViewById(R.id.medicine_name);
            nameView.setClickable(true);
            usageView = (TextView) itemView.findViewById(R.id.medicine_usage);
            usageView.setClickable(true);
            setClockButton = (Button) itemView.findViewById(R.id.set_clock);
            cancelClockButton = (Button) itemView.findViewById(R.id.cancel_clock);
        }
    }


}
