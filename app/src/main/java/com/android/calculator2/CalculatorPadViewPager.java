/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bkav.calculator2.R;

public class CalculatorPadViewPager extends ViewPager {


    private final PagerAdapter mStaticPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return getChildCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return getChildAt(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            removeViewAt(position);
        }

        // method kiem tra xem cac doi tuong duoc tra ve boi insatantiateItem duoc ket noi voi cac view duoc cung cap

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) { // Bkav phongngb set lai hien thi cua ba man hinh tren viewpager
            if (position == 0) {
                return 0.84f;
            } else if (position == 2) {
                return (0.84f);
            } else {
                return (float) (mScreenWidth + mWidthDistanceRight) / mScreenWidth;

            }
            // BKAV AnhBM
//            return  position == 1 ? 0.8f :(float) (mScreenWidth + mWidthDistanceRight) / mScreenWidth;
        }
    };

    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        private void recursivelySetEnabled(View view, boolean enabled) {
            if (view instanceof ViewGroup) {
                final ViewGroup viewGroup = (ViewGroup) view;
                for (int childIndex = 0; childIndex < viewGroup.getChildCount(); ++childIndex) {
                    recursivelySetEnabled(viewGroup.getChildAt(childIndex), enabled);
                }
            } else {
                view.setEnabled(enabled);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (getAdapter() == mStaticPagerAdapter) {
                for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
                    // Only enable subviews of the current page.
                    recursivelySetEnabled(getChildAt(childIndex), childIndex == position);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);

//            Bkav phongngb khi scroll se draw background advenced
            if (mIScrollViewPager != null)
                mIScrollViewPager.onScroll(position, positionOffset, positionOffsetPixels);
        }
    };

    private final PageTransformer mPageTransformer = new PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            // Phongngb : Check neu lon hon 3 view . neu view hien thi len man hinh la view 1
            // thi di chuyen cac view kia
            if (getChildCount() > 2) {
                if (view.equals(getChildAt(1))) {
//                    view.setAlpha(Math.max(1.0f + position, 0.0f));
                    view.setTranslationX(getWidth() * -position);
                }
            }
        }
    };


    public CalculatorPadViewPager(Context context) {
        this(context, null);
    }

    public CalculatorPadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAdapter(mStaticPagerAdapter);
//        setBackgroundColor(getResources().getColor(android.R.color.black));
        setOnPageChangeListener(mOnPageChangeListener);
//        setPageMargin(getResources().getDimensionPixelSize(R.dimen.pad_page_margin));

        // Bkav Phongngb : de cho viewPager co ther reverse drawing
        setPageTransformer(true, mPageTransformer);
        // Bkav AnhBM
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;

        final Resources res = getResources();
        mWidthDistanceRight = res.getDimensionPixelOffset(R.dimen.width_distance_right);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Invalidate the adapter's data set since children may have been added during inflation.
        if (getAdapter() == mStaticPagerAdapter) {
            mStaticPagerAdapter.notifyDataSetChanged();
        }
    }


    /******************** Bkav **********************/
    private int mScreenWidth;

    private int mWidthDistanceRight;
    private IScrollViewPager mIScrollViewPager;

    public interface IScrollViewPager {
        void onScroll(int i, float v, int i1);
    }

    public void setOnScrollViewPager(IScrollViewPager mIScrollViewPager) {
        this.mIScrollViewPager = mIScrollViewPager;
    }


}
