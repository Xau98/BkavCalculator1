package com.android.calculator2.bkav;

import android.content.Context;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.android.calculator2.CalculatorResult;
import com.bkav.calculator2.R;

public class BkavCalculatorResult extends CalculatorResult {
    public BkavCalculatorResult(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Bkav TienNVh : Set Color của ký tự E trong phần hiện thị kết quả
    @Override
    protected void setColorE(Context context) {
        mExponentColorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.bkav_display_result_exponent_text_color));
    }
}
