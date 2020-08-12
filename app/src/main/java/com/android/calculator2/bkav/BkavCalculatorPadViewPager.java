package com.android.calculator2.bkav;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import com.android.calculator2.CalculatorPadViewPager;
import com.bkav.calculator2.R;

//Bkav AnhNDd TODO Các hằng số 1,2,3 trong này đặt trong biến để biết ý nghĩa
public class BkavCalculatorPadViewPager extends CalculatorPadViewPager {

    public BkavCalculatorPadViewPager(Context context) {
        super(context);
    }

    // Bkav TienNVh : customs lai Adapter cho viewpage
    private final PagerAdapter mStaticPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return getChildCount();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            final View child = getChildAt(position);
            // Set a OnClickListener to scroll to item's position when it isn't the current item.
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentItem(position, true /* smoothScroll */);
                }
            });
            // Set an OnTouchListener to always return true for onTouch events so that a touch
            // sequence cannot pass through the item to the item below.
            child.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.onTouchEvent(event);
                    return true;
                }
            });

            // Set an OnHoverListener to always return true for onHover events so that focus cannot
            // pass through the item to the item below.
            child.setOnHoverListener(new OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    v.onHoverEvent(event);
                    return true;
                }
            });
            // Make the item focusable so it can be selected via a11y.
            child.setFocusable(true);
            // Set the content description of the item which will be used by a11y to identify it.
            child.setContentDescription(getPageTitle(position));
            return child;
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) {
            int orientation = getResources().getConfiguration().orientation;
            if (position == 0) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // Bkav TienNVh : set width cho tab history khi o man hinh chia doi
                    if (getChildCount() == 3)
                        return 0.692f;
                    return 0.385f;
                } else {
                    return 0.717f / 0.9f;
                }
            }
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
    //Bkav AnhNDd TODO Biến để sai???? Giải thích lại ý nghĩa, không code thế này
    boolean check =true;
    // Bkav TienNVh : Custom lại để cho tab lịch sử về phía trái và tab Advnace về phía phải của màn hình
    // Bkav TienNVh : Mục đích biến nay là giữ tab bàn phím số và phép tính ở nguyên vị trí chính
    // khi vuốt 2 bên để mơ tab lich sự và tab advance
    private final PageTransformer mPageTransformer = new PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            if (getChildCount() >= 2) {
                if (view.equals(getChildAt(1))) {
                    float sizeTrans = getWidth() * -position;
                    if(sizeTrans<0&&check && mCallInvisibleTabHistory != null) {
                        mCallInvisibleTabHistory.onCallVisiblleHistory();
                        check = false;
                    }
                    view.setTranslationX(sizeTrans);
                }
            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.M)
    public BkavCalculatorPadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Bkav TienNVh : Set Adapter cho Viewpager
        setAdapter(mStaticPagerAdapter);
        // Bkav TienNVh : giữa nguyên tab number làm tab main
        // nghĩa là tab này ở nguyên 1 vị trí dù có đóng mở tab khác
        setPageTransformer(false, mPageTransformer);
        //Bkav AnhNDd TODO code gốc margin ở đâu mà set ở đây????
        // Bkav TienNVh : Set Margin về 0 để làm mất đi viền xanh cạnh trái
        setPageMargin(0);
    }

    //=========================================bkav =========================================
    public BkavCalculatorPadViewPager.CallInvisibleTabHistory mCallInvisibleTabHistory;

    public void setCallInvisibleTabHistory(BkavCalculatorPadViewPager.CallInvisibleTabHistory callinvisibleTabHistory) {
        this.mCallInvisibleTabHistory = callinvisibleTabHistory;
    }

    // Bkav TienNVh: Call back listener close /open tab history
    //Bkav AnhNDd TODO nếu interface dùng để callback thì luôn phải check ở class khởi tạo có khác null hay không
    public interface CallInvisibleTabHistory {
        void onCallVisiblleHistory();

        void onCloseHistory();
    }

    // Bkav TienNVh :
    @Override
    protected void getBkavCurrentItem() {
        if(getCurrentItem()==1 && !check){
            mCallInvisibleTabHistory.onCloseHistory();
            check = true;
        }
    }
}
