package com.android.calculator2.bkav;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bkav.calculator2.R;

import java.util.ArrayList;

public class BkavHistoryAdapter extends RecyclerView.Adapter <BkavHistoryAdapter.RecyclerViewHoler> {
    private ArrayList<String> listHistory;
    private onClickItemSaveHistory mOnClickItemSaveHistory;
    private Context mContext;
    public BkavHistoryAdapter(Context context, ArrayList<String> listHistory) {
        this.listHistory = listHistory;
        mContext=context;
    }

    public void setmOnClickItemSaveHistory(onClickItemSaveHistory mOnClickItemSaveHistory) {
        this.mOnClickItemSaveHistory = mOnClickItemSaveHistory;
    }

    @NonNull
    @Override
    public RecyclerViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.line_history, parent, false);
        return new RecyclerViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHoler holder, int position) {
        final String historyCurrent[]= listHistory.get(position).split("=");
        if(historyCurrent.length!=0) {
            holder.mFormula.setText(historyCurrent[0]);
            holder.mResult.setText(historyCurrent[1]);
            holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickItemSaveHistory.onClick(historyCurrent[1]);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listHistory.size();
    }

    public class RecyclerViewHoler extends RecyclerView.ViewHolder {
        TextView mResult, mFormula;
        LinearLayout mLinearLayout;
        public RecyclerViewHoler(@NonNull View itemView) {
            super(itemView);
            mFormula=itemView.findViewById(R.id.textview_history);
            mResult=itemView.findViewById(R.id.textview_result);
            mLinearLayout=itemView.findViewById(R.id.linear_layout_history);
            Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/helveticaNeueThin.ttf");
            mFormula.setTypeface(myTypeface);
            mResult.setTypeface(myTypeface);
        }
    }
    interface onClickItemSaveHistory{
        void onClick(String result);
    }
}
