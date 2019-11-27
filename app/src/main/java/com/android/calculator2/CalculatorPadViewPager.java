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

package com.android.calculator2;

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
            Log.d("TienNVh", "getCount: " + getChildCount());
            return getChildCount();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            return getChildAt(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d("TienNVh", "destroyItem: " + position);
//             removeViewAt(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) {
            int orientation = getResources().getConfiguration().orientation;
            if (position == 0) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    return 0.385f;
                return 0.705f / 0.9f;
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
    //Bkav ThanhNgD: Them method nay`

    public void recursivelySetEnabled(View view, boolean enabled) {

        if (view instanceof ViewGroup) {
            // Bkav ThanhNgD: Can` chuyen view thanh` viewGrop vi`: view chuyen` vao` gom` nhieu` child view, ma`
            // method setEnable() o duoi' chi thuc hien dc voi' cac base view nhu TextView, Button...
            final ViewGroup viewGroup = (ViewGroup) view;
            Log.d("TienNVh", "onPageSelected 0: " + viewGroup.getChildCount());
            // Thuc hien de quy lai method nay` voi' tat' ca cac' child view cua viewGroup
            // Method nay` se chay vao` else{} chu' k vao` day nua~ vi` viewGroup.getChildAt(childIndex)
            // luc' nay` la` base view -> (view instanceof ViewGroup) la` false
            for (int childIndex = 0; childIndex < viewGroup.getChildCount(); ++childIndex) {
                recursivelySetEnabled(viewGroup.getChildAt(childIndex), enabled);
            }

        } else {
            // Bkav ThanhNgD: setEnabled(...) xu li kha nang Touchables( co' the cham.) cua view
            // false -> vo hieu hoa' Touchables, true -> bat Touchables

            Log.d("TienNVh", "recursivelySetEnabled: " + view);

            view.setEnabled(enabled);

        }
    }

    //Bkav ThanhNgD: Them method nay`
    public OnPageChangeListener getmOnPageChangeListener() {
        return mOnPageChangeListener;
    }

    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

            if (getAdapter() == mStaticPagerAdapter) {
                Log.d("TienNVh", "onPageSelected: " + getChildCount());

                TextView emptyElement = (TextView) getChildAt(0).findViewById(R.id.emptyElement);
                for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
                    recursivelySetEnabled(getChildAt(childIndex), true);
                    // Xu ly bug khi lich su trong'(emptyElement dang VISIBLE ) van click button 123... cua page 1
                    if (emptyElement != null && getCurrentItem() == 0 && emptyElement.getVisibility() == VISIBLE) {
//                        GridLayout calculatorNumericPadLayout
//                                = (GridLayout) getChildAt(1).findViewById(R.id.pad_numeric);
//                        recursivelySetEnabled( calculatorNumericPadLayout, false);
                    } else {
                        //Bkav ThanhNgD: childIndex == position -> true
                        // Neu' la` childIndex == position thi` page dang chon moi' click dc, page khac'
                        // du` co' hien cung k the click vi`  setEnabled() = false
                        // Con` la` true thi` neu' view dc hien tren windown thi` co the click
//                        recursivelySetEnabled( getChildAt(childIndex), true);
                    }
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
                    Log.d("TienNVh", "transformPage: " + getWidth() + " : " + position + " : " + view.getTranslationX() + " : " + view.getX());
                    view.setTranslationX(sizeTrans);
                }
            }
            //else {
//                // Use the default slide transition when moving to the next page.
//                view.setTranslationX(0.0f);
//                view.setAlpha(1.0f);
//            }
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
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        try {
//            // Always intercept touch events when a11y focused since otherwise they will be
//            // incorrectly offset by a11y before being dispatched to children.
//            if (isAccessibilityFocused() || super.onInterceptTouchEvent(ev)) {
//                return true;
//            }
//
//            // Only allow the current item to receive touch events.
//            final int action = ev.getActionMasked();
//            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
//                // If a child is a11y focused then we must always intercept the touch event
//                // since it will be incorrectly offset by a11y.
//                final int childCount = getChildCount();
//                for (int childIndex = childCount - 1; childIndex >= 0; --childIndex) {
//                    if (getChildAt(childIndex).isAccessibilityFocused()) {
//                        mClickedItemIndex = childIndex;
//                        return true;
//                    }
//                }
//
//                if (action == MotionEvent.ACTION_DOWN) {
//                    mClickedItemIndex = -1;
//                }
//
//                // Otherwise if touch is on a non-current item then intercept.
//                final int actionIndex = ev.getActionIndex();
//                final float x = ev.getX(actionIndex) + getScrollX();
//                final float y = ev.getY(actionIndex) + getScrollY();
//                for (int i = childCount - 1; i >= 0; --i) {
//                    final int childIndex = getChildDrawingOrder(childCount, i);
//                    final View child = getChildAt(childIndex);
//                    if (child.getVisibility() == VISIBLE
//                            && x >= child.getLeft() && x < child.getRight()
//                            && y >= child.getTop() && y < child.getBottom()) {
//                        if (action == MotionEvent.ACTION_DOWN) {
//                            mClickedItemIndex = childIndex;
//                        }
//                        return childIndex != getCurrentItem();
//                    }
//                }
//            }
//
//            return false;
//        } catch (IllegalArgumentException e) {
//            Log.e("Calculator", "Error intercepting touch event", e);
//            return false;
//        }
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        try {
//            // Allow both the gesture detector and super to handle the touch event so they both see
//            // the full sequence of events. This should be safe since the gesture detector only
//            // handle clicks and super only handles swipes.
//            mGestureDetector.onTouchEvent(ev);
//            return super.onTouchEvent(ev);
//        } catch (IllegalArgumentException e) {
//            Log.e("Calculator", "Error processing touch event", e);
//            return false;
//        }
//    }
}
