package com.android.calculator2.bkav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class BkavHistoryLayout extends RelativeLayout {
    public BkavHistoryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap mBitmap;
    private float mOffset;
    private  Paint mPaint;
    private Rect mDest;
    public void setInforScrollViewpager(Bitmap bitmap , float v ) {
        this.mBitmap = bitmap;
        this.mOffset = v;
        mPaint = new Paint();
        mDest = new Rect(0, 0, getWidth(), getHeight());
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Bkav TienNVh : Vì Cắt bitmap của 2 tab này giống nhau chỉ khác mỗi vị trí và độ dài
        if (mBitmap != null) {
            mPaint.setFilterBitmap(true);
            canvas.translate((int) (mOffset * getWidth()), 0);
            canvas.drawBitmap(mBitmap, null, mDest, mPaint);

        }
    }

}
