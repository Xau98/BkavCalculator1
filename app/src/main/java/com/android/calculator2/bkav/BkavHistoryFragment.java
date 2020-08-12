/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2.bkav;

import android.app.Fragment;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.calculator2.DragController;
import com.android.calculator2.Evaluator;
import com.android.calculator2.HistoryItem;
import com.bkav.calculator2.R;

import java.util.ArrayList;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;

public class BkavHistoryFragment extends Fragment {
    public static final String TAG = "HistoryFragment";
    private RecyclerView mRecyclerView;
    private BkavHistoryAdapter mAdapter;
    private Evaluator mEvaluator;
    private ArrayList<HistoryItem> mDataSet = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BkavHistoryAdapter(getActivity(), mDataSet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(
                R.layout.fragment_history, container, false /* attachToRoot */);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler_view);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_DRAGGING) {
                    stopActionModeOrContextMenu();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final BkavCalculator activity = (BkavCalculator) getActivity();
        mEvaluator = Evaluator.getInstance(activity);
        mAdapter.setEvaluator(mEvaluator);
        final long maxIndex = mEvaluator.getMaxIndex();

        final ArrayList<HistoryItem> newDataSet = new ArrayList<>();

        for (long i = 0; i < maxIndex; ++i) {
            newDataSet.add(null);
        }
        final boolean isEmpty = newDataSet.isEmpty();
        // Bkav TienNVh
        // mRecyclerView.setBackgroundColor(ContextCompat.getColor(activity,
        // isEmpty ? R.color.empty_history_color : R.color.display_background_color));
        if (isEmpty) {
            newDataSet.add(new HistoryItem());
        }
        mDataSet = newDataSet;
        mAdapter.setDataSet(mDataSet);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEvaluator != null) {
            // Note that the view is destroyed when the fragment backstack is popped, so
            // these are essentially called when the DragLayout is closed.
            mEvaluator.cancelNonMain();
        }
    }
// Bkav TienNVh : bỏ các hiệu ứng kéo lên kéo xuống
//    private void initializeController(boolean isResult, boolean isOneLine, boolean isDisplayEmpty) {
//        mDragController.setDisplayFormula(
//                (BkavCalculatorFormula) getActivity().findViewById(R.id.formula));
//        mDragController.setDisplayResult(
//                (BkavCalculatorResult) getActivity().findViewById(R.id.result));
//        mDragController.setToolbar(getActivity().findViewById(R.id.toolbar));
//        mDragController.setEvaluator(mEvaluator);
//        mDragController.initializeController(isResult, isOneLine, isDisplayEmpty);
//    }

    public boolean stopActionModeOrContextMenu() {
        if (mRecyclerView == null) {
            return false;
        }
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            final View view = mRecyclerView.getChildAt(i);
            final BkavHistoryAdapter.ViewHolder viewHolder =
                    (BkavHistoryAdapter.ViewHolder) mRecyclerView.getChildViewHolder(view);
            if (viewHolder != null && viewHolder.getResult() != null
                    && viewHolder.getResult().stopActionModeOrContextMenu()) {
                return true;
            }
        }
        return false;
    }


}
