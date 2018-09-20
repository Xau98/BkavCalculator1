package com.android.calculator2.bkav;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bkav.calculator2.R;

import java.util.List;

public class BkavHistoryAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResource;
    private List<String> mListHistory;

    public BkavHistoryAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mListHistory = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            view = vi.inflate(mResource, null);
        }

        String historyItem = getItem(position);
        String[] array = historyItem.split("=");

        if (historyItem != null) {
            TextView txtHistory = (TextView) view.findViewById(R.id.textview_history);
            TextView txtResult = (TextView) view.findViewById(R.id.textview_result);
            if (array.length >= 2) {
                Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/helveticaneue-light.ttf");
                int orientation = getContext().getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    txtResult.setTextSize(30);
                    txtHistory.setTextSize(16);
                    txtHistory.setPadding(0, 0, 30, 0);
                }
                txtHistory.setTypeface(typeface);
                txtResult.setTypeface(typeface);

                txtHistory.setText(array[0]);
                txtResult.setText(array[1]);
            }
        }
        return view;
    }
}
