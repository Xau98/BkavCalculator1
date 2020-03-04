/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.bkav.calculator2;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;



public class CalculatorPadViewPager extends ViewPager {

    private final PagerAdapter mStaticPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return getChildCount();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            return getChildAt(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//             removeViewAt(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // Bkav TienNVh :
        @Override
        public float getPageWidth(int position) {
            int orientation = getResources().getConfiguration().orientation;
            if (position == 0) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                    return 0.385f;
                }
                else {
                    return 0.717f / 0.9f;
                }
            }
            // Bkav TienNVh :
            if (position == 2) {
                return 0.9999f;
            }
            return 1.0f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            final String[] pageDescriptions = getContext().getResources()
                    .getStringArray(R.array.desc_pad_pages);
            return pageDescriptions[position];
        }
    };



    // Bkav TienNVh : hàm này mục đích là Lấy hết tất cả các view con trong ViewGroup để setEnable  (cho phép click vào view con)
    public void recursivelySetEnabled(View view, boolean enabled) {
        // Bkav TienNVh :  Kiểm tra view có phải là ViewGroup không ? .
       // Nếu đúng thì tiếp tục tìm view con trong viewGroup
        // Bkav TienNVh : Nếu sai thì setEnable cho view con ý
        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            for (int childIndex = 0; childIndex < viewGroup.getChildCount(); ++childIndex) {
                // Bkav TienNVh : Dùng để quy để thục hiện setEnable cho view con
                recursivelySetEnabled(viewGroup.getChildAt(childIndex), enabled);
            }
        } else {
            view.setEnabled(enabled);
        }
    }



    public OnPageChangeListener getmOnPageChangeListener() {
        return mOnPageChangeListener;
    }

    // Bkav TienNVh : Sự kiện lắng nghe khi chuyển tab
    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (getAdapter() == mStaticPagerAdapter) {
                // Bkav TienNVh : Lấy ra toàn bộ các viewGroup
                for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
                    // Bkav TienNVh : Thực hiện setEnable cho các view con trong mỗi ViewGroup
                    recursivelySetEnabled(getChildAt(childIndex), true);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };

    private final PageTransformer mPageTransformer = new PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            if (getChildCount() >= 2) {
                // Pin the left page to the left side.
                if (view.equals(getChildAt(1))) {
                    float sizeTrans = getWidth() * -position;
                    view.setTranslationX(sizeTrans);
                }
            }
        }
    };

    private final GestureDetector.SimpleOnGestureListener mGestureWatcher =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    // Return true so calls to onSingleTapUp are not blocked.
                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent ev) {
                    if (mClickedItemIndex != -1) {
                        getChildAt(mClickedItemIndex).performClick();
                        mClickedItemIndex = -1;
                        return true;
                    }
                    return super.onSingleTapUp(ev);
                }
            };

    private final GestureDetector mGestureDetector;

    private int mClickedItemIndex = -1;

    public CalculatorPadViewPager(Context context) {
        this(context, null /* attrs */);
    }

    public CalculatorPadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context, mGestureWatcher);
        mGestureDetector.setIsLongpressEnabled(false);

        setAdapter(mStaticPagerAdapter);
        setOnPageChangeListener(mOnPageChangeListener);
        setPageTransformer(true, mPageTransformer);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Invalidate the adapter's data set since children may have been added during inflation.
        if (getAdapter() == mStaticPagerAdapter)
            getAdapter().notifyDataSetChanged();

        // Let page change listener know about our initial position.
        // mOnPageChangeListener.onPageSelected(getCurrentItem());
    }

}
