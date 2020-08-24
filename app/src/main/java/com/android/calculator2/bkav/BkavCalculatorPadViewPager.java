package com.android.calculator2.bkav;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import com.android.calculator2.CalculatorPadViewPager;
import com.bkav.calculator2.R;

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

        // Bkav TienNVh : Trả về độ rộng của 1 tab
        @Override
        public float getPageWidth(int position) {
            int orientation = getResources().getConfiguration().orientation;
            // Bkav TienNVh : Check
            if (position == BkavCalculator.POSITION_HISTORY_VIEWPAGER) {
                // Bkav TienNVh :  Check nếu ở xoay ngang
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // Bkav TienNVh : set width cho tab history khi o man hinh chia doi.
                    if (getChildCount() == BkavCalculator.GET_CHILD_VIEWPAGER)
                        return 0.692f;

                    return 0.385f;
                } else {
                    // Bkav TienNVh : Ở chế độ dọc màn hình
                    return 0.717f / 0.9f;
                }
            }
            // Bkav TienNVh :  Set độ rông cho tab Advence
            if (position == BkavCalculator.POSITION_ADVENCE_VIEWPAGER) {
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

    // Bkav TienNVh :  Mục đích tạo biến để biết được trạng thái của tab History open/close
    // open là false , close là true
    boolean isShowTabHistory =true;
    // Bkav TienNVh : Custom lại để cho tab lịch sử về phía trái và tab Advnace về phía phải của màn hình
    // Bkav TienNVh : Mục đích biến nay là giữ tab bàn phím số và phép tính ở nguyên vị trí chính
    // khi vuốt 2 bên để mơ tab lich sự và tab advance
    private final PageTransformer mPageTransformer = new PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            // Bkav TienNVh :  Check xem đang ở chế độ nào, chế độ để dọc màn hình thi getChildCount = 3 vì có 3 tab
            // còn ở chế độ xoay ngang thì chỉ có 2 tab
            if (getChildCount() >= 2) {
                if (view.equals(getChildAt(1))) {
                    float sizeTrans = getWidth() * -position;
                    // Bkav TienNVh : Check trường hợp đang ở tab main mở tab lịch sử
                    if(sizeTrans < 0 && isShowTabHistory && mCallInvisibleTabHistory != null) {
                        // Bkav TienNVh : Call back để hiện thị tab history
                        mCallInvisibleTabHistory.onCallVisiblleHistory();
                        // Bkav TienNVh :  Chuyển biến trạng thái
                        isShowTabHistory = false;
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
        // Bkav TienNVh : Mục đích đặt hàm set margin để tách code android và code bkav
        // Bkav TienNVh : Set Margin về 0 để làm mất đi viền xanh cạnh trái
        setPageMargin(0);
    }

    //=========================================bkav =========================================
    public BkavCalculatorPadViewPager.CallInvisibleTabHistory mCallInvisibleTabHistory;

    public void setCallInvisibleTabHistory(BkavCalculatorPadViewPager.CallInvisibleTabHistory callinvisibleTabHistory) {
        this.mCallInvisibleTabHistory = callinvisibleTabHistory;
    }

    // Bkav TienNVh: Call back listener close /open tab history
    public interface CallInvisibleTabHistory {
        void onCallVisiblleHistory();

        void onCloseHistory();
    }

    // Bkav TienNVh :
    @Override
    protected void eventCloseHistory() {
        // Bkav TienNVh : Check trường hợp đang ở tab history nhưng chỉ nhận khi vuốt sang tab main
        if(getCurrentItem() == 1 && !isShowTabHistory){
            // Bkav TienNVh : Tiến hành đóng tab history
            if(mCallInvisibleTabHistory != null)
                 mCallInvisibleTabHistory.onCloseHistory();
            // Bkav TienNVh : Sau khi đóng tab History thì quay lại tab main
            isShowTabHistory = true;
        }
    }
}
