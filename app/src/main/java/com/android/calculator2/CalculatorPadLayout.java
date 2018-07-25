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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bkav.calculator2.R;

/**
 * A layout that places children in an evenly distributed grid based on the specified
 * {@link android.R.attr#columnCount} and {@link android.R.attr#rowCount} attributes.
 */
public class CalculatorPadLayout extends ViewGroup {

    private int mRowCount;
    private int mColumnCount;

    public CalculatorPadLayout(Context context) {
        this(context, null);
    }

    public CalculatorPadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalculatorPadLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                new int[]{android.R.attr.rowCount, android.R.attr.columnCount}, defStyle, 0);
        mRowCount = a.getInt(0, 1);
        mColumnCount = a.getInt(1, 1);

        a.recycle();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        final boolean isRTL = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        final int columnWidth =
                Math.round((float) (right - left - paddingLeft - paddingRight)) / mColumnCount;
        final int rowHeight =
                Math.round((float) (bottom - top - paddingTop - paddingBottom)) / mRowCount;

        int rowIndex = 0, columnIndex = 0;
        for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
            final View childView = getChildAt(childIndex);
            if (childView.getVisibility() == View.GONE) {
                continue;
            }

            final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();

            final int childTop = paddingTop + lp.topMargin + rowIndex * rowHeight;
            final int childBottom = childTop - lp.topMargin - lp.bottomMargin + rowHeight;
            final int childLeft = paddingLeft + lp.leftMargin +
                    (isRTL ? (mColumnCount - 1) - columnIndex : columnIndex) * columnWidth;
            final int childRight = childLeft - lp.leftMargin - lp.rightMargin + columnWidth;

            final int childWidth = childRight - childLeft;
            final int childHeight = childBottom - childTop;
            if (childWidth != childView.getMeasuredWidth() ||
                    childHeight != childView.getMeasuredHeight()) {
                childView.measure(
                        MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
            }
            childView.layout(childLeft, childTop, childRight, childBottom);

            rowIndex = (rowIndex + (columnIndex + 1) / mColumnCount) % mRowCount;
            columnIndex = (columnIndex + 1) % mColumnCount;

        }
    }


    /**
     * Bkav Phongngb
     * Day la ham set background advenced khi keo den dau setBackground tuong ung den do
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint mPaint = new Paint();
        if (mBitmap != null) {
            Rect dest = new Rect(0, 0, getWidth(), getHeight());
            mPaint.setFilterBitmap(true);

            int delta = getWidth() - (int) (mOffsetPixel - mWidthDistanceRight);
            canvas.translate(-delta + mOffset * mWidthDistanceRight, 0);
            canvas.drawBitmap(mBitmap, null, dest, mPaint);

        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    //    Bkav Phongngb
    private Bitmap mBitmap;
    private int mPostion;
    private float mOffset;
    private int mOffsetPixel;
    private int mWidthDistanceRight;
    private int mScreenWidth;

    public void setInforScrollViewpager(Bitmap bitmap, int i, float v, int i1) {
        this.mBitmap = bitmap;
        this.mPostion = i;
        this.mOffset = v;
        this.mOffsetPixel = i1;

        final Resources res = getResources();
        mWidthDistanceRight = res.getDimensionPixelOffset(R.dimen.width_distance_right);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;

        invalidate();
        requestLayout();

    }


}
