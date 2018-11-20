package com.android.calculator2.bkav;

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

import com.android.calculator2.CalculatorNumericPadLayout;
import com.android.calculator2.CalculatorPadViewPager;
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
        //Bkav ThanhNgD:  Trong nay` bi cmt -> Bo cmt
        @Override
        public void onPageSelected(int position) {
            Log.i("XXX","XXX");
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
    };

    //Bkav ThanhNgD: Them method nay`
    public OnPageChangeListener getmOnPageChangeListener(){
        return mOnPageChangeListener;
    }

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
