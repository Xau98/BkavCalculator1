package com.android.calculator2.bkav;

import android.app.WallpaperManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.calculator2.Calculator;
import com.android.calculator2.CalculatorPadViewPager;
import com.android.calculator2.bkav.EqualsImageButton.State;
import com.bkav.calculator2.R;
import com.xlythe.math.Constants;

/**
 * Created by anhbm on 07/06/2017.
 */

public class BkavCalculator extends Calculator {

    private LinearLayout mRootView;
    private CalculatorPadViewPager mCalculatorPadViewPager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Bkav AnhBM: khong cho ban phim hien len.
        hideSoftKeyboard();

        super.onCreate(savedInstanceState);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        
        mFormulaEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                hideSoftKeyboard();
                return true;
            }
        });


        Button dot = (Button) findViewById(R.id.dec_point);
        dot.setText(String.valueOf(Constants.DECIMAL_POINT));

        mFormulaEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mFormulaEditText.setFocusableInTouchMode(true);
        mFormulaEditText.setFocusable(true);

        mFormulaEditText.setEnabled(true);
        mFormulaEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFormulaEditText.getSelectionEnd() != mFormulaEditText.getText().length()) {
                    mFormulaEditText.setCursorVisible(true);
                }
                mDeleteButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.GONE);
            }
        });

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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

    @Override
    protected void onDelete() {
        mFormulaEditText.backspace();
    }

    @Override
    protected void onEquals() {
        String text = mFormulaEditText.getCleanText();
        if (mCurrentState == CalculatorState.INPUT) {
            
            // Bkav QuangLH: truong hop dang xoay ngang thi chi la Button thoi.
            // Xu ly nhu dau bang.
            State state = State.EQUALS;
            if (mEqualButton instanceof EqualsImageButton) {
                state = ((EqualsImageButton) mEqualButton).getState();
            }
            switch (state) {
                case EQUALS:
                    setState(CalculatorState.EVALUATE);
                    mEvaluator.evaluate(text, this);
                    break;
                case NEXT:
                    mFormulaEditText.next();
                    break;
            }
        }
    }

    @Override
    protected void insertMathExpression(View view) {
        insert(((Button) view).getText().toString());
    }

    @Override
    protected void insertAdvancedMathExpression(View view) {
        insert(((Button) view).getText() + "(");
    }

    protected void insert(String text) {
        // Add left parenthesis after functions.
        if (mCurrentState.equals(CalculatorState.INPUT) ||
                mFormulaEditText.isCursorModified()) {
            mFormulaEditText.insert(text);
        } else {
            mFormulaEditText.setText(text);
        }
    }

    @Override
    protected void clearResult() {
        mResultEditText.getEditableText().clear();
    }

    @Override
    protected void disableCursorView() {
        mFormulaEditText.setCursorVisible(false);
    }

    @Override
    protected void closePadAdvanced(View view) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (view.getId() == R.id.fun_percent || view.getId() == R.id.op_fact || view.getId() == R.id.const_pi) {
                openPage();
            }
        }
    }
    
    @Override
    protected String getCleanText() {
        return mFormulaEditText.getCleanText();
    }
}
