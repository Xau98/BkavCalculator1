package com.android.calculator2.bkav;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.calculator2.Calculator;
import com.android.calculator2.CalculatorPadViewPager;
import com.bkav.calculator2.R;

/**
 * Created by anhbm on 07/06/2017.
 */

public class BkavCalculator extends Calculator {

    private LinearLayout mRootView;
    private CalculatorPadViewPager mCalculatorPadViewPager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = (LinearLayout) findViewById(R.id.root_layout);
        mCalculatorPadViewPager = (CalculatorPadViewPager) findViewById(R.id.pad_pager);

        //Bitmap backgroundBitmap = getBlurredBackground();
        Bitmap backgroundBitmapFromRom = getBluredBackgroundFromRom();

        //mRootView.setBackground(new BitmapDrawable(backgroundBitmap));
        Display getOrient = getWindowManager().getDefaultDisplay();
        if (getOrient.getWidth() < getOrient.getHeight()) {
            mCalculatorPadViewPager.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        mRootView.setBackground(new BitmapDrawable(backgroundBitmapFromRom));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        makeStatusBarTransparent(mToolbar);
    }

    private Bitmap getBlurredBackground() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Bitmap bitmap = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();

        BlurManager blurManager = new BlurManager(mRootView, null);
        blurManager.bitmapScale(0.1f).build(this, bitmap);
        bitmap = blurManager.blur(25f);
        return bitmap;
    }

    private Bitmap getBluredBackgroundFromRom() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Bitmap bitmap = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();


        WallpaperBlurCompat wallpaperBlurCompat = new WallpaperBlurCompat(this);
        return wallpaperBlurCompat.getWallpaperBlur();
        //return bitmap;
    }

    /**
     * AnhBM: ham thuc hien lam trong suot status bar
     */
    private void makeStatusBarTransparent(Toolbar toolbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // AnhBM: cho toolbar padding 1 doan dung bang statusbar height,
        // viec setpadding ko dung view co the lam hong animation tab
        // Retrieve the AppCompact Toolbar

        View view = findViewById(R.id.toolbar);
        view.setPadding(0, getStatusBarHeight(), 0, 0);
    }

    /**
     * AnhBM: tim chieu cao cua status bar
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
