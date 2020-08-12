package com.android.calculator2.bkav;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
    public void setSwitchToolbar() {
        if (!mEnableToolbar) {
            if (mToolbar.getVisibility() != View.VISIBLE) {
                showToolbar(true);
            } else {
                hideToolbar();
            }
        }
        mEnableToolbar = false;
    }

    // Bkav TienNVh : Biến này lưu trang thái click button history
    private boolean mEnableToolbar = false;
    public void setEnableToolbar(boolean b) {
        mEnableToolbar = b;
    }
}
