package com.android.calculator2.bkav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.calculator2.CalculatorDisplay;

public class BkavCalculatorDisplay extends CalculatorDisplay {

    public BkavCalculatorDisplay(Context context) {
        super(context);
    }

    public BkavCalculatorDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BkavCalculatorDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setHideShowToolbar() {
        // Bkav TienNVh : check TH ko phải click vào button history
        if (!mEnableToolbar) {
            // Bkav TienNVh : Check toolbar đã hiện thì chưa , nếu đang ẩn thì hiện và ngược lại
            if (mToolbar.getVisibility() != View.VISIBLE) {
                showToolbar(true);
            } else {
                hideToolbar();
            }
        }
        // Bkav TienNVh : Set lại trạng thái
        mEnableToolbar = false;
    }

    // Bkav TienNVh : Biến này lưu trang thái click button history
    private boolean mEnableToolbar = false;
    public void setEnableToolbar(boolean b) {
        mEnableToolbar = b;
    }
}
