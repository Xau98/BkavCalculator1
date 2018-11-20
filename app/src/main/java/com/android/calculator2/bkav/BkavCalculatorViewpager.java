package com.android.calculator2.bkav;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bkav.calculator2.R;

// Phongngb : calculator viewpager khi xoay ngang
public class BkavCalculatorViewpager extends ViewPager {

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

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) {
            if (position == 0) {//Bkav ThanhNgD: 0.4 -> 0.405
                return (float) ((0.405 * mScreenWidth) / (mScreenWidth + mWidthDistanceRight));
            } else {
                return 1f;
            }
        }
    };

    private final OnPageChangeListener mOnPageChangeListener = new SimpleOnPageChangeListener() {
        //Bkav ThanhNgD:  Trong nay` bi cmt -> Bo cmt
        private void recursivelySetEnabled(View view, boolean enabled) {
            if (view instanceof ViewGroup) {
                final ViewGroup viewGroup = (ViewGroup) view;
                for (int childIndex = 0; childIndex < viewGroup.getChildCount(); ++childIndex) {
                    recursivelySetEnabled(viewGroup.getChildAt(childIndex), enabled);
                }
            } else {
                // Bkav ThanhNgD: setEnabled(...) xu li kha nang Touchables( co' the cham.) cua view.
                // false -> vo hieu hoa' Touchables, true -> bat Touchables
                view.setEnabled(enabled);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (getAdapter() == mStaticPagerAdapter) {
                for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
                    //Bkav ThanhNgD: childIndex == position -> true
                    // Neu' la` childIndex == position thi` page dang chon moi' click dc
                    // Con` la` true thi` tat ca nhung gi` hien tren windown deu` co the click dc
                    recursivelySetEnabled(getChildAt(childIndex), true);
                }
            }
        }
    };

    private final PageTransformer mPageTransformer = new PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            if (view.equals(getChildAt(1))) {
                view.setTranslationX(getWidth() * -position);
            }
        }
    };

    public BkavCalculatorViewpager(Context context) {
        this(context, null);
    }

    public BkavCalculatorViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAdapter(mStaticPagerAdapter);
        setOnPageChangeListener(mOnPageChangeListener);
        setPageTransformer(true, mPageTransformer);

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

        if (getAdapter() == mStaticPagerAdapter) {
            mStaticPagerAdapter.notifyDataSetChanged();
        }
    }

    /******************** Bkav **********************/
    private int mScreenWidth;

    private int mWidthDistanceRight;
}
