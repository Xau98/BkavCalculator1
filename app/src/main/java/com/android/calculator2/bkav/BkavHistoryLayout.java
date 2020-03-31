package com.android.calculator2.bkav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
// Bkav TienNVh :
public class BkavHistoryLayout extends RelativeLayout {
    public BkavHistoryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap mBitmap;
    private int mPostion;
    private float mOffset;
    private int mOffsetPixel;

    public void setInforScrollViewpager(Bitmap bitmap, float v ) {
        this.mBitmap = bitmap;
        this.mOffset = v;

        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint mPaint = new Paint();
        if (mBitmap != null) {

            Rect dest = new Rect(0, 0, getWidth(), getHeight());
            mPaint.setFilterBitmap(true);
            canvas.translate((int) (mOffset * getWidth()), 0);
            canvas.drawBitmap(mBitmap, null, dest, mPaint);

        }
    }

}
