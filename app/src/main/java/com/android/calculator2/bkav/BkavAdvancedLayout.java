package com.android.calculator2.bkav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import androidx.gridlayout.widget.GridLayout;

// Bkav TienNVh : Layout của Tab Advanced
public class BkavAdvancedLayout extends GridLayout {
    public BkavAdvancedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /****************************** Bkav **************************/
    private Bitmap mBitmap;
    private float mOffset;
    private int mOffsetPixel;
    private int mWidthDistanceRight;

    public void setInforScrollViewpager(Bitmap bitmap, float v, int i1) {
        this.mBitmap = bitmap;
        this.mOffset = v;
        this.mOffsetPixel = i1;
        invalidate();
        requestLayout();
    }

    /**
     * Bkav Phongngb
     * Day la ham set background advanced khi keo den dau setBackground tuong ung den do
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
    }
