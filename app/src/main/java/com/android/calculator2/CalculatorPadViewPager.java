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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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

        //Bkav Phongngb: method kiem tra xem cac doi tuong duoc tra ve boi insatantiateItem duoc ket noi voi cac view duoc cung cap
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) { // Bkav phongngb set lai hien thi cua ba man hinh tren viewpager
            if (position == 0) {
                return 0.80f;// Bkav ThanhNgD: 0.84 -> 0.80
            } else if (position == 2) {
                return (0.75f);//Bkav ThanhNgD: 0.84 -> 0.75
            } else {
                return (float) (mScreenWidth + mWidthDistanceRight) / mScreenWidth;

            }
            //BKAV AnhBM
            //return  position == 1 ? 0.8f :(float) (mScreenWidth + mWidthDistanceRight) / mScreenWidth;
        }
    };

    public void recursivelySetEnabled(View view, boolean enabled) {
        if (view instanceof ViewGroup) {
            // Bkav ThanhNgD: Can` chuyen view thanh` viewGrop vi`: view chuyen` vao` gom` nhieu` child view, ma`
            // method setEnable() o duoi' chi thuc hien dc voi' cac base view nhu TextView, Button...
            final ViewGroup viewGroup = (ViewGroup) view;
            // Thuc hien de quy lai method nay` voi' tat' ca cac' child view cua viewGroup
            // Method nay` se chay vao` else{} chu' k vao` day nua~ vi` viewGroup.getChildAt(childIndex)
            // luc' nay` la` base view -> (view instanceof ViewGroup) la` false
            for (int childIndex = 0; childIndex < viewGroup.getChildCount(); ++childIndex) {
                recursivelySetEnabled(viewGroup.getChildAt(childIndex), enabled);
            }
        } else {
            // Bkav ThanhNgD: setEnabled(...) xu li kha nang Touchables( co' the cham.) cua view
            // false -> vo hieu hoa' Touchables, true -> bat Touchables
            view.setEnabled(enabled);
        }
    }

    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (getAdapter() == mStaticPagerAdapter) {
                TextView emptyElement = (TextView) getChildAt(0).findViewById(R.id.emptyElement);
                for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
                    // Xu ly bug khi lich su trong'(emptyElement dang VISIBLE ) van click button 123... cua page 1
                    if(emptyElement != null && getCurrentItem() == 0 && emptyElement.getVisibility() == VISIBLE){
                        CalculatorNumericPadLayout calculatorNumericPadLayout
                                = (CalculatorNumericPadLayout) getChildAt(1).findViewById(R.id.pad_numeric);
                        recursivelySetEnabled( calculatorNumericPadLayout, false);
                    }
                    else {
                        //Bkav ThanhNgD: childIndex == position -> true
                        // Neu' la` childIndex == position thi` page dang chon moi' click dc, page khac'
                        // du` co' hien cung k the click vi`  setEnabled() = false
                        // Con` la` true thi` neu' view dc hien tren windown thi` co the click
                        recursivelySetEnabled( getChildAt(childIndex), true);
                    }
                }
            }
        }

        //Bkav phongngb khi scroll se draw background advenced
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
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
                    view.setTranslationX(getWidth() * -position);
                }
            }

//            Bkav AnhBM: Bo logic goc
//            if (position < 0.0f) {
//                // Pin the left page to the left side.
//                view.setTranslationX(getWidth() * -position);
//
//                //AnhBM: Bo do khong can hieu ung alpha khi vuot sang nua
//                //view.setAlpha(Math.max(1.0f + position, 0.0f));
//            } else {
//                // Use the default slide transition when moving to the next page.
//                view.setTranslationX(0.0f);
//                view.setAlpha(1.0f);
//            }
        }
    };


    public CalculatorPadViewPager(Context context) {
        this(context, null);
    }

    public CalculatorPadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAdapter(mStaticPagerAdapter);
        //Bkav AnhBM: bo de dung cho cau truc pageview moi
//        setBackgroundColor(getResources().getColor(android.R.color.black));
//        setPageMargin(getResources().getDimensionPixelSize(R.dimen.pad_page_margin));
        setOnPageChangeListener(mOnPageChangeListener);
        // Bkav Phongngb : de cho viewPager co the reverse drawing
        setPageTransformer(/*false*/true, mPageTransformer);

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
