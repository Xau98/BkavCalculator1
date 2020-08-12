package com.android.calculator2.bkav;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.calculator2.AlignedTextView;
import com.android.calculator2.CalculatorResult;
import com.android.calculator2.Evaluator;
import com.android.calculator2.HistoryItem;
import com.bkav.calculator2.R;

import java.util.ArrayList;

// Bkav TienNVh : Adapter của tab history
class BkavHistoryAdapter extends RecyclerView.Adapter<BkavHistoryAdapter.ViewHolder> {
    private Activity mActivity;
    private ArrayList<HistoryItem> mDataset;
    private Evaluator mEvaluator;
    public static final int HISTORY_VIEW_TYPE = 1;
    private static final int EMPTY_VIEW_TYPE = 0;

    public BkavHistoryAdapter(Activity activity, ArrayList<HistoryItem> dataset) {
        mActivity = activity;
        mDataset = dataset;
    }

    public void setEvaluator(Evaluator evaluator) {
        mEvaluator = evaluator;
    }

    @NonNull
    @Override
    public BkavHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view;
        if (viewType == HISTORY_VIEW_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bkav_history_item, parent, false);
        } else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_history_view, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final BkavHistoryAdapter.ViewHolder holder, int position) {
        HistoryItem item = getItem(position);
        if (item.isEmptyView()) {
            return;
        }
        holder.mFormula.setText(item.getFormula());
        holder.mResult.setEvaluator(mEvaluator, item.getEvaluatorIndex());
        Typeface myTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/helveticaNeueThin.ttf");
        holder.mFormula.setTypeface(myTypeface);
        holder.mResult.setTypeface(myTypeface);
        // Bkav TienNVh : Click cả item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BkavHistoryAdapter.OnClickItemHistory) mActivity).onClickItemHistory(holder.mResult.getTruncatedWholePart());
            }
        });
        // Bkav TienNVh :Do view Result đã nhận listener=> ko nhận listener khi click cả item
        holder.mResult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    ((BkavHistoryAdapter.OnClickItemHistory) mActivity).onClickItemHistory(holder.mResult.getTruncatedWholePart());
                return false;
            }
        });
        // Bkav TienNVh :Do view fomula nằm trong Scroll view => ko nhận được event từ onclick itemView
        holder.mFormula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BkavHistoryAdapter.OnClickItemHistory) mActivity).onClickItemHistory(holder.mResult.getTruncatedWholePart());
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder.getItemViewType() == EMPTY_VIEW_TYPE) {
            return;
        }
        mEvaluator.cancel(holder.getItemId(), true);
        holder.mFormula.setText(null);
        holder.mResult.setText(null);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getEvaluatorIndex();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isEmptyView() ? EMPTY_VIEW_TYPE : HISTORY_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setDataSet(ArrayList<HistoryItem> dataSet) {
        mDataset = dataSet;
    }

    private HistoryItem getItem(int position) {
        HistoryItem item = mDataset.get(position);
        if (item == null) {
            int evalutorIndex = (int) (mEvaluator.getMaxIndex() - position);
            item = new HistoryItem(evalutorIndex, mEvaluator.getTimeStamp(evalutorIndex), mEvaluator.getExprAsSpannable(evalutorIndex));
            mDataset.set(position, item);
        }
        return item;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private AlignedTextView mFormula;
        private BkavCalculatorResult mResult;
        private View mDivider;

        public ViewHolder(View view, int viewType) {
            super(view);
            if (viewType == EMPTY_VIEW_TYPE) {
                return;
            }
            mFormula = view.findViewById(R.id.bkav_history_formula);
            mResult = view.findViewById(R.id.bkav_history_result);
            mDivider = view.findViewById(R.id.bkav_history_divider);
        }

        public CalculatorResult getResult() {
            return mResult;
        }
    }

    // Bkav TienNVh : Call back when click item in history
    public interface OnClickItemHistory {
        void onClickItemHistory(String result);
    }
}
