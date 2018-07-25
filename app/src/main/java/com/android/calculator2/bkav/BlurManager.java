
package com.android.calculator2.bkav;

import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class BlurManager {
    public static final float DEFAULT_BITMAP_SCALE = 0.4f;
    public static final float DEFAULT_MIN_RADIUS = 0.1f;
    public static final float DEFAULT_MAX_RADIUS = 25f;
    public static final long DEFAULT_DURATION = 200;

    private TimeAnimator mTimeAnimator;
    private BlurTimeListener mAnimationCallback;

    private View mBluredView;

    private Bitmap mInputBitmap;
    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mScriptIntrinsicBlur;

    private float mMinRadius;
    private float mMaxRadius;
    private boolean mUseSetImageBitmap;
    private float mBitmapScale;
    private float mCurveFactor;

    private OnBlurAnimationEndListener mOnBlurAnimationEndListener;

    private boolean mIsBuilt;

    public BlurManager(View bluredView, OnBlurAnimationEndListener onBlurAnimationEndListener) {
        mBluredView = bluredView;
        mOnBlurAnimationEndListener = onBlurAnimationEndListener;

        mUseSetImageBitmap = false;
        mMinRadius = DEFAULT_MIN_RADIUS;
        mMaxRadius = DEFAULT_MAX_RADIUS;
        mBitmapScale = DEFAULT_BITMAP_SCALE;

        mIsBuilt = false;
    }

//    Bkav Phongngb them ham tao khong doi so de blur

    public BlurManager() {
        mUseSetImageBitmap = false;
        mMinRadius = DEFAULT_MIN_RADIUS;
        mMaxRadius = DEFAULT_MAX_RADIUS;
        mBitmapScale = DEFAULT_BITMAP_SCALE;
        mIsBuilt = false;
    }

    public BlurManager build(Context context, Bitmap imageBitmap) {
        if (mBitmapScale == 1f) {
            mInputBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            int width = Math.round(imageBitmap.getWidth() * mBitmapScale);
            int height = Math.round(imageBitmap.getHeight() * mBitmapScale);

            mInputBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        }

        mRenderScript = RenderScript.create(context);
        mScriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));

        Allocation tmpIn = Allocation.createFromBitmap(mRenderScript, mInputBitmap);
        mScriptIntrinsicBlur.setInput(tmpIn);

        mAnimationCallback = new BlurTimeListener(this, mMinRadius, mMaxRadius, 0);
        mAnimationCallback.setDuration(DEFAULT_DURATION);

        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setInterpolator(new DecelerateInterpolator(mCurveFactor));

        mIsBuilt = true;

        return this;
    }

    public BlurManager build(Activity context, View bitmapSourceView) {
        Bitmap imageBitmap = getScreenshot(bitmapSourceView);

        return build(context, imageBitmap);
    }

    /**
     * Bkav QuangLH: truong hop mBluredView la ImageView va muon dung ham setBitmap de
     * thay src.
     */
    public BlurManager useSetImageBitmap(boolean useSetImageBitmap) {
        mUseSetImageBitmap = useSetImageBitmap;

        return this;
    }

    public BlurManager minRadius(float minRadius) {
        mMinRadius = minRadius;

        return this;
    }

    public BlurManager maxRadius(float maxRadius) {
        mMaxRadius = maxRadius;

        return this;
    }

    public BlurManager bitmapScale(float bitmapScale) {
        mBitmapScale = bitmapScale;

        return this;
    }

    public BlurManager curveFactor(float curveFactor) {
        mCurveFactor = curveFactor;

        return this;
    }

    public void setDuration(long duration) {
        mAnimationCallback.setDuration(duration);
    }

    public void start() {
        if (!mIsBuilt) {
            throw new RuntimeException("Must call build() before calling start().");
        }

        mBluredView.setAlpha(0f);
        mAnimationCallback.setReversing(false);
        doAnimation();
    }

    public void reverse() {
        if (!mIsBuilt) {
            throw new RuntimeException("Must call build() before calling start().");
        }

        mAnimationCallback.setReversing(true);
        doAnimation();
    }

    private void doAnimation() {
        if (!mTimeAnimator.isStarted()) {
            mTimeAnimator.setTimeListener(mAnimationCallback);
            mTimeAnimator.setRepeatMode(ObjectAnimator.RESTART);
            mTimeAnimator.setRepeatCount(0);
            mTimeAnimator.start();
        }
    }

    public Bitmap blur(float radius) {
        mScriptIntrinsicBlur.setRadius(radius);

        Bitmap outputBitmap = Bitmap.createBitmap(mInputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(mRenderScript, outputBitmap);
        mScriptIntrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private Bitmap getScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    public void onBlurImageChanged(float alpha, Bitmap newBluredBitmap) {
        mOnBlurAnimationEndListener.onBluredViewAlphaChanged(alpha);
        mBluredView.setAlpha(alpha);
        if (mUseSetImageBitmap && mBluredView instanceof ImageView) {
            ((ImageView) mBluredView).setImageBitmap(newBluredBitmap);
        } else {
            mBluredView.setBackground(new BitmapDrawable(newBluredBitmap));
        }
    }

    public void onBlurAnimationEnd() {
        mTimeAnimator.removeAllListeners();
        mTimeAnimator.end();

        if (mOnBlurAnimationEndListener != null) {
            mOnBlurAnimationEndListener.onBlurAnimationEnd(mAnimationCallback.isReversing());
        }
    }

    public interface OnBlurAnimationEndListener {
        void onBlurAnimationEnd(boolean reversing);

        void onBluredViewAlphaChanged(float alpha);
    }

    private static class BlurTimeListener implements TimeAnimator.TimeListener {
        private static final float MIN_RADIUS = 0.1f;
        private static final float MAX_RADIUS = 25f;

        private float mMinRadius;
        private float mMaxRadius;
        private long mDuration;
        private BlurManager mBlurManager;

        private boolean mReversing;

        public BlurTimeListener(BlurManager blurManager, float minRadius, float maxRadius,
                                long duration) {
            mDuration = duration;
            mBlurManager = blurManager;
            mMaxRadius = maxRadius;
            mMinRadius = minRadius;

            mReversing = false;
        }

        public void setDuration(long duration) {
            mDuration = duration;
        }

        public void setReversing(boolean reversing) {
            mReversing = reversing;
        }

        public boolean isReversing() {
            return mReversing;
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            if (totalTime < mDuration) {
                float fraction = (float) totalTime / mDuration;
                float alpha = mReversing ? 1 - fraction : fraction;

                float radius = getCurrentRadius(totalTime);
                Bitmap blurBitmap = mBlurManager.blur(radius);
                mBlurManager.onBlurImageChanged(alpha, blurBitmap);
            } else {
                mBlurManager.onBlurAnimationEnd();
            }
        }

        private float getCurrentRadius(long totalTime) {
            if (!mReversing) {
                return mMinRadius + (mMaxRadius - mMinRadius) / mDuration * totalTime;
            } else {
                return mMaxRadius - (mMaxRadius - mMinRadius) / mDuration * totalTime;
            }
        }
    }
}
