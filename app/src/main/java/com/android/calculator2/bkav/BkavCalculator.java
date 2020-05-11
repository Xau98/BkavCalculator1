/*
 * Copyright (C) 2016 The Android Open Source Project
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

// TODO: Copy & more general paste in formula?  Note that this requires
//       great care: Currently the text version of a displayed formula
//       is not directly useful for re-evaluating the formula later, since
//       it contains ellipses representing subexpressions evaluated with
//       a different degree mode.  Rather than supporting copy from the
//       formula window, we may eventually want to support generation of a
//       more useful text version in a separate window.  It's not clear
//       this is worth the added (code and user) complexity.

package com.android.calculator2.bkav;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.telephony.CarrierConfigManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Property;
import android.view.ActionMode;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroupOverlay;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bkav.calculator2.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class BkavCalculator extends Activity
        implements CalculatorFormula.OnTextSizeChangeListener, OnLongClickListener,
        AlertDialogFragment.OnClickListener, Evaluator.EvaluationListener /* for main result */,
        DragLayout.CloseCallback, DragLayout.DragCallback, PermissionUtil.CallbackCheckPermission {


    private static final String TAG = "Calculator";
    /**
     * Constant for an invalid resource id.
     */
    public static final int INVALID_RES_ID = -1;

    private enum CalculatorState {
        INPUT,          // Result and formula both visible, no evaluation requested,
        // Though result may be visible on bottom line.
        EVALUATE,       // Both visible, evaluation requested, evaluation/animation incomplete.
        // Not used for instant result evaluation.
        INIT,           // Very temporary state used as alternative to EVALUATE
        // during reinitialization.  Do not animate on completion.
        INIT_FOR_RESULT,  // Identical to INIT, but evaluation is known to terminate
        // with result, and current expression has been copied to history.
        ANIMATE,        // Result computed, animation to enlarge result window in progress.
        RESULT,         // Result displayed, formula invisible.
        // If we are in RESULT state, the formula was evaluated without
        // error to initial precision.
        // The current formula is now also the last history entry.
        ERROR           // Error displayed: Formula visible, result shows error message.
        // Display similar to INPUT state.
    }
    // Normal transition sequence is
    // INPUT -> EVALUATE -> ANIMATE -> RESULT (or ERROR) -> INPUT
    // A RESULT -> ERROR transition is possible in rare corner cases, in which
    // a higher precision evaluation exposes an error.  This is possible, since we
    // initially evaluate assuming we were given a well-defined problem.  If we
    // were actually asked to compute sqrt(<extremely tiny negative number>) we produce 0
    // unless we are asked for enough precision that we can distinguish the argument from zero.
    // ERROR and RESULT are translated to INIT or INIT_FOR_RESULT state if the application
    // is restarted in that state.  This leads us to recompute and redisplay the result
    // ASAP. We avoid saving the ANIMATE state or activating history in that state.
    // In INIT_FOR_RESULT, and RESULT state, a copy of the current
    // expression has been saved in the history db; in the other non-ANIMATE states,
    // it has not.
    // TODO: Possibly save a bit more information, e.g. its initial display string
    // or most significant digit position, to speed up restart.


    private final Property<TextView, Integer> TEXT_COLOR =
            new Property<TextView, Integer>(Integer.class, "textColor") {
                @Override
                public Integer get(TextView textView) {
                    return textView.getCurrentTextColor();
                }

                @Override
                public void set(TextView textView, Integer textColor) {
                    textView.setTextColor(textColor);
                }
            };

    private static final String NAME = "Calculator";
    private static final String KEY_DISPLAY_STATE = NAME + "_display_state";
    private static final String KEY_UNPROCESSED_CHARS = NAME + "_unprocessed_chars";
    /**
     * Associated value is a byte array holding the evaluator state.
     */
    private static final String KEY_EVAL_STATE = NAME + "_eval_state";
    private static final String KEY_INVERSE_MODE = NAME + "_inverse_mode";
    /**
     * Associated value is an boolean holding the visibility state of the toolbar.
     */
    private static final String KEY_SHOW_TOOLBAR = NAME + "_show_toolbar";
    // Bkav TienNVh :
    private static int ALPHA_BLUR = 255;
    private static float OFFSET = 1.0f;
    private static int COUNT_CHILD_VIEWPAGE = 3;
    private static int POSITION_TAB_HISTORY = 0;
    private static int POSITION_TAB_MAIN = 1;
    private static int POSITION_TAB_ADVANCE = 2;
    private static  String NAME_FILE_SHAREDPREFERENCES = "SaveHistory";
    private  static String SHAREDPREFERENCES_FORMULATEXT = "FormulaText";
    private static  String SHAREDPREFERENCES_LANGUAGE = "Language";
    private static String LANGUAGE_VN = "vi_VN";
    // Bkav TienNVh :  Truong hop con tro dung truoc cac ky tu :'o, i ,n, g,x,p,s,a' thi no dich chuyen con tro ve sau dau (.
    private static char LIST_CHAR_STOP_CLICK [] = {'o','i','n','g','x','p','s','a','('};


    private final ViewTreeObserver.OnPreDrawListener mPreDrawListener =
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mFormulaContainer.scrollTo(mFormulaText.getRight(), 0);
                    final ViewTreeObserver observer = mFormulaContainer.getViewTreeObserver();
                    if (observer.isAlive()) {
                        observer.removeOnPreDrawListener(this);
                    }
                    return false;
                }
            };

    private final Evaluator.Callback mEvaluatorCallback = new Evaluator.Callback() {
        @Override
        public void onMemoryStateChanged() {
            mFormulaText.onMemoryStateChanged();
        }

        @Override
        public void showMessageDialog(@StringRes int title, @StringRes int message,
                                      @StringRes int positiveButtonLabel, String tag) {
            AlertDialogFragment.showMessageDialog(BkavCalculator.this, title, message,
                    positiveButtonLabel, tag);

        }
    };

    private final OnDisplayMemoryOperationsListener mOnDisplayMemoryOperationsListener =
            new OnDisplayMemoryOperationsListener() {
                @Override
                public boolean shouldDisplayMemory() {
                    return mEvaluator.getMemoryIndex() != 0;
                }
            };

    private final CalculatorFormula.OnFormulaContextMenuClickListener mOnFormulaContextMenuClickListener =
            new CalculatorFormula.OnFormulaContextMenuClickListener() {
                @Override
                public boolean onPaste(ClipData clip) {
                    final ClipData.Item item = clip.getItemCount() == 0 ? null : clip.getItemAt(0);
                    if (item == null) {
                        // nothing to paste, bail early...
                        return false;
                    }
                    // Bkav TienNVh : đoạn này có nghĩa là
                    // Bkav TienNVh : Trong TH :Khi mình copy kết quả và dán để tính tiếp thì ko phải lưu đoạn text mà chỉ cần lấy Index kết quả
                    // Check if the item is a previously copied result, otherwise paste as raw text.
 //                   final Uri uri = item.getUri();
//                    if (uri != null && mEvaluator.isLastSaved(uri)) {
//                        clearIfNotInputState();
//                        mEvaluator.appendExpr(mEvaluator.getSavedIndex());
//                        redisplayAfterFormulaChange();
//                    } else {
                        // Bkav TienNVh : lay du lieu copy
                        String textNew = item.coerceToText(BkavCalculator.this).toString() + "";
                        String formula = mFormulaText.getText().toString();
                        String formula1 = formula.substring(0, mFormulaText.getSelectionStart());
                        String formula2 = formula.substring(mFormulaText.getSelectionEnd());
                        String result = formula1 + textNew + formula2;
                        mPostionCursorToRight = result.length() - textNew.length() - formula1.length();
                        // Bkav TienNVh : Xoa cac phep tinh hiện tại
                        //long start = System.currentTimeMillis();
                        mEvaluator.clearMain();
                        // Bkav TienNVh : add cac phep tính mới
//                        addExplicitStringToExpr(result);
                        addChars(result, false);
                        redisplayAfterFormulaChange();
                        // Bkav TienNVh : thay doi vi tri con tro
                        changePostionCursor();
                        //Log.d("TienNVh", "onPaste: "+(System.currentTimeMillis() - start));
                    //    }
                    return true;
                }

                @Override
                public void onMemoryRecall() {
                    clearIfNotInputState();
                    long memoryIndex = mEvaluator.getMemoryIndex();
                    if (memoryIndex != 0) {
                        mEvaluator.appendExpr(mEvaluator.getMemoryIndex());
                        redisplayAfterFormulaChange();
                    }
                }
            };


    private final TextWatcher mFormulaTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            final ViewTreeObserver observer = mFormulaContainer.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnPreDrawListener(mPreDrawListener);
                observer.addOnPreDrawListener(mPreDrawListener);
            }
        }
    };

    private CalculatorState mCurrentState;
    private Evaluator mEvaluator;
    private BkavMemoryFunction mBkavMemoryFunction;
    private CalculatorDisplay mDisplayView;
    private TextView mModeView, mModeViewM;
    private Toolbar mToolbar;
    private CalculatorFormula mFormulaText;
    private CalculatorResult mResultText;
    private HorizontalScrollView mFormulaContainer;
    private DragLayout mDragLayout;
    private RelativeLayout mRootView;
    private CalculatorPadViewPager mPadViewPager;
    private View mDeleteButton;
    private View mClearButton;
    private View mEqualButton;
    private View mMainCalculator;
    private TextView mInverseToggle;
    private TextView mModeToggle;
    private RecyclerView mRecyclerViewSaveHistory;
    // Bkav TienNVh : Tao mang de chua ds phep tinh trong history
    private ArrayList<String> mListHistory = new ArrayList<>();
    private BkavHistoryAdapter mHistoryAdapter;
    private View[] mInvertibleButtons;
    private View[] mInverseButtons;
    private ActionMode mActionMode;
    private View mCurrentButton;
    private Animator mCurrentAnimator;
    // Bkav TienNVh :
    private SharedPreferences mSharedPreferences;
    private String mSharePreFile = "SaveCalCulator";
    private ImageView mImgMore ;

    // Characters that were recently entered at the end of the display that have not yet
    // been added to the underlying expression.
    private String mUnprocessedChars = null;

    // Color to highlight unprocessed characters from physical keyboard.
    // TODO: should probably match this to the error color?
    private ForegroundColorSpan mUnprocessedColorSpan = new ForegroundColorSpan(Color.RED);

    // Whether the display is one line.
    private boolean mIsOneLine;

    /**
     * Map the old saved state to a new state reflecting requested result reevaluation.
     */
    private CalculatorState mapFromSaved(CalculatorState savedState) {
        switch (savedState) {
            case RESULT:
            case INIT_FOR_RESULT:
                // Evaluation is expected to terminate normally.
                return CalculatorState.INIT_FOR_RESULT;
            case ERROR:
            case INIT:
                return CalculatorState.INIT;
            case EVALUATE:
            case INPUT:
                return savedState;
            default:  // Includes ANIMATE state.
                throw new AssertionError("Impossible saved state");
        }
    }

    /**
     * Restore Evaluator state and mCurrentState from savedInstanceState.
     * Return true if the toolbar should be visible.
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        final CalculatorState savedState = CalculatorState.values()[
                savedInstanceState.getInt(KEY_DISPLAY_STATE,
                        CalculatorState.INPUT.ordinal())];
        setState(savedState);
        CharSequence unprocessed = savedInstanceState.getCharSequence(KEY_UNPROCESSED_CHARS);
        if (unprocessed != null) {
            mUnprocessedChars = unprocessed.toString();
        }
        byte[] state = savedInstanceState.getByteArray(KEY_EVAL_STATE);
        if (state != null) {
            try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(state))) {
                mEvaluator.restoreInstanceState(in);
            } catch (Throwable ignored) {
                // When in doubt, revert to clean state
                mCurrentState = CalculatorState.INPUT;
                mEvaluator.clearMain();
            }
        }
        if (savedInstanceState.getBoolean(KEY_SHOW_TOOLBAR, true)) {
            showAndMaybeHideToolbar();
        } else {
            mDisplayView.hideToolbar();
        }
        onInverseToggled(savedInstanceState.getBoolean(KEY_INVERSE_MODE));
        // TODO: We're currently not saving and restoring scroll position.
        //       We probably should.  Details may require care to deal with:
        //         - new display size
        //         - slow recomputation if we've scrolled far.
    }

    private void restoreDisplay() {
        onModeChanged(mEvaluator.getDegreeMode(Evaluator.MAIN_INDEX));
        if (mCurrentState != CalculatorState.RESULT
                && mCurrentState != CalculatorState.INIT_FOR_RESULT) {
            redisplayFormula();
        }
        if (mCurrentState == CalculatorState.INPUT) {
            // This resultText will explicitly call evaluateAndNotify when ready.
            mResultText.setShouldEvaluateResult(CalculatorResult.SHOULD_EVALUATE, this);

        } else {
            // Just reevaluate.
            setState(mapFromSaved(mCurrentState));
            // Request evaluation when we know display width.
            mResultText.setShouldEvaluateResult(CalculatorResult.SHOULD_REQUIRE, this);
        }
    }

    private BkavHistoryLayout mRelativeLayoutHistory;
    private BkavAdvancedLayout mCalculatorPadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calculator_main);
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        // Hide all default options in the ActionBar.
        getActionBar().setDisplayOptions(0);

        // Ensure the toolbar stays visible while the options menu is displayed.
        getActionBar().addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
            @Override
            public void onMenuVisibilityChanged(boolean isVisible) {
                mDisplayView.setForceToolbarVisible(isVisible);
            }
        });
        mRelativeLayoutHistory = (BkavHistoryLayout) findViewById(R.id.relativeLayout_history);
        mCalculatorPadLayout = (BkavAdvancedLayout) findViewById(R.id.pad_advanced);
        mBkavMemoryFunction = new BkavMemoryFunction();
        mMainCalculator = findViewById(R.id.main_calculator);
        mDisplayView = (CalculatorDisplay) findViewById(R.id.display);
        mToolbar = findViewById(R.id.toolbar);
        mModeView = (TextView) findViewById(R.id.mode);
        mModeViewM = (TextView) findViewById(R.id.mode2);
        mFormulaText = (CalculatorFormula) findViewById(R.id.formula);
        mResultText = (CalculatorResult) findViewById(R.id.result);
        mFormulaContainer = (HorizontalScrollView) findViewById(R.id.formula_container);
        // Bkav TienNVh :
        mRecyclerViewSaveHistory = (RecyclerView) findViewById(R.id.history_recycler_view);

        mEvaluator = Evaluator.getInstance(this);
        mEvaluator.setCallback(mEvaluatorCallback);
        mResultText.setEvaluator(mEvaluator, Evaluator.MAIN_INDEX);
        KeyMaps.setActivity(this);
        mCheckPermission = new CheckPermission(this);
        mPadViewPager = (CalculatorPadViewPager) findViewById(R.id.pad_pager);
        mPadViewPager.setCurrentItem(1);
        mDeleteButton = findViewById(R.id.del);
        mClearButton = findViewById(R.id.clr);
        View numberPad = findViewById(R.id.pad_numeric);
        mEqualButton = numberPad.findViewById(R.id.eq);
        if (mEqualButton == null || mEqualButton.getVisibility() != View.VISIBLE) {
            mEqualButton = findViewById(R.id.pad_operator).findViewById(R.id.eq);
        }
        final TextView decimalPointButton = (TextView) numberPad.findViewById(R.id.dec_point);
        decimalPointButton.setText(getDecimalSeparator());

        mInverseToggle = (TextView) findViewById(R.id.toggle_inv);
        mModeToggle = (TextView) findViewById(R.id.toggle_mode);

        mIsOneLine = mResultText.getVisibility() == View.INVISIBLE;
        mInvertibleButtons = new View[]{
                findViewById(R.id.fun_sin),
                findViewById(R.id.fun_cos),
                findViewById(R.id.fun_tan),
                findViewById(R.id.fun_ln),
                findViewById(R.id.fun_log),
                findViewById(R.id.op_sqrt)

        };
        mInverseButtons = new View[]{
                findViewById(R.id.fun_arcsin),
                findViewById(R.id.fun_arccos),
                findViewById(R.id.fun_arctan),
                findViewById(R.id.fun_exp),
                findViewById(R.id.fun_10pow),
                findViewById(R.id.op_sqr)
        };

        mDragLayout = (DragLayout) findViewById(R.id.drag_layout);
        mDragLayout.removeDragCallback(this);
        mDragLayout.addDragCallback(this);
        mDragLayout.setCloseCallback(this);

        mFormulaText.setOnContextMenuClickListener(mOnFormulaContextMenuClickListener);
        mFormulaText.setOnDisplayMemoryOperationsListener(mOnDisplayMemoryOperationsListener);
        mFormulaText.setEnabled(true);
        mFormulaText.setOnTextSizeChangeListener(this);
        mFormulaText.addTextChangedListener(mFormulaTextWatcher);
        mDeleteButton.setOnLongClickListener(this);

        // Bkav TienNVh :
        mSharedPreferences = getSharedPreferences(mSharePreFile, MODE_PRIVATE);
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            mCurrentState = CalculatorState.INPUT;
            mEvaluator.clearMain();
            showAndMaybeHideToolbar();
            onInverseToggled(false);
        }
        //Bkav TienNVh :Setbackground
        setBlurBackground();
        //Bkav TienNVh :Load tab history
        onRefeshSaveHistory();
        // Bkav TienNVh : setbackground history sau khi xoay màn hình
        mRelativeLayoutHistory.post(new Runnable() {
            @Override
            public void run() {
                if (bitmapBlurHis != null)
                    mRelativeLayoutHistory.setBackground(new BitmapDrawable(bitmapBlurHis));
            }
        });

        final int orientation = getResources().getConfiguration().orientation;
        long a = System.currentTimeMillis();
        ViewTreeObserver vto = mDragLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mDragLayout.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                } else {
                    mDragLayout.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
                // Bkav TienNVh :
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int mScreenHeight =mDragLayout.getHeight();
                int heightChild = mRelativeLayoutHistory.getHeight();
                int widthHistory =  mRelativeLayoutHistory.getWidth();
                // Bkav TienNVh :
                Bitmap bitmap = convertViewToBitmap(mDragLayout);
                // Bkav TienNVh :  set background cho tab History
                BlurManager blur = new BlurManager();
                int y = mScreenHeight - heightChild;
                Bitmap cutBitmapHistory =Bitmap.createBitmap(bitmap, 0,   y, widthHistory, heightChild);
                Bitmap mContainerFilter = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter.eraseColor(getResources().getColor(R.color.colorHistory));
                Bitmap bmHistory = overlayBitmap(mContainerFilter, cutBitmapHistory, ALPHA_BLUR);
                blur.bitmapScale(0.05f).build(getApplicationContext(), bmHistory);
                bitmapBlurHis = blur.blur(20f);

                // Bkav TienNVh :  Set background cho tab Advanced
                BlurManager blurAd = new BlurManager();
                final Bitmap cutBitmapAd = Bitmap.createBitmap(bitmap, (int) (bitmap.getWidth() * 0.2), y, (int) (bitmap.getWidth() * 0.8), heightChild );

                Bitmap mContainerFilter1 = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter1.eraseColor(getResources().getColor(R.color.colorAdvanced));
                Bitmap bmAd = overlayBitmap(mContainerFilter1, cutBitmapAd, ALPHA_BLUR);
                blurAd.bitmapScale(0.05f).build(getApplicationContext(), bmAd);
                final   Bitmap bitmapBlurAd = blurAd.blur(20f);

                // Bkav TienNVh : sự kiện sang trang
                //    Bkav TienNVh : set background cho History
                mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, (float) 0.0);
                mImgMore = findViewById(R.id.bt_more);

                // Bkav TienNVh : Nếu ViewPager có 3 tab thì set background và hiện bt More
                if (mPadViewPager.getChildCount() == COUNT_CHILD_VIEWPAGE){
                    mCalculatorPadLayout.setInforScrollViewpager(bitmapBlurAd, OFFSET, findViewById(R.id.numeric_operator).getWidth());
                }
                else
                    mImgMore.setVisibility(View.GONE);

                mPadViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == POSITION_TAB_HISTORY) {
                            // Bkav TienNVh :  position =0 là tab history
                            mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, positionOffset);
                        } else {
                            if (position == POSITION_TAB_MAIN) {
                                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    if (bitmapBlurAd != null) {
                                        mCalculatorPadLayout.setInforScrollViewpager(bitmapBlurAd, positionOffset, positionOffsetPixels);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onPageSelected(int i) {
                        //Bkav ThanhNgD: Goi lai onPageSelected() de nhan su kien khi changed page
                        mPadViewPager.getmOnPageChangeListener().onPageSelected(i);
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });
            }
        });

        //Bkav TienNVh : Ko cho click xuyen len lich su
        mRelativeLayoutHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

        // Bkav TienNVh : Khi xoay hide button more , set height cho button Xoa
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.delHistory).getLayoutParams().height = 150;
        }

        // Bkav TienNVh : Set font number
        setFontNumber();

        // Bkav TienNVh : Nhận sự kiện chạm vào
        mFormulaText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                String text = mFormulaText.getText().toString();
                int handle = mFormulaText.getSelectionStart();
                // Bkav TienNVh : Check trong trường hợp phép tính không bị lỗi thì cho phép chuyển con trỏ về cuối '('
                if ((mUnprocessedChars != null && mUnprocessedChars.equals("")) || mUnprocessedChars == null) {
                    if (text.length() >= handle + 1) {
                        // Bkav TienNVh :  Truong hop con tro dung truoc cac ky tu :'o, i ,n, g,x,p,s,a' thi no dich chuyen con tro ve sau dau (.
                        // Bkav TienNVh : Trong truong hop
                        char charAtClick = text.charAt(handle);
                        if (checkCharStopClick(charAtClick)) {
                            if (charAtClick == 's') {
                                if (handle > 0 && text.charAt(handle - 1) == 'o') {
                                    for (int i = handle; i < text.length(); i++) {
                                        if (text.charAt(i) == '(') {
                                            mFormulaText.setSelection(i + 1);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                for (int i = handle; i < text.length(); i++) {
                                    if (text.charAt(i) == '(') {
                                        mFormulaText.setSelection(i + 1);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }
                return true;
            }
        });
        // Bkav TienNVh : Lam trong suot arstatus bar
        overlapStatusbar();
    }
    // Bkav TienNVh : Check
    boolean checkCharStopClick(char s){
        for (int i=0 ; i<LIST_CHAR_STOP_CLICK.length ; i++){
            if(s == LIST_CHAR_STOP_CLICK[i])
                return true;
        }
            return false;
    }
    // Bkav TienNVh :them font chu cho number
    void setFontNumber() {
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/helveticaNeueThin.ttf");
        Button digit_0, digit_1, digit_2, digit_3, digit_4, digit_5, digit_6, digit_7, digit_8, digit_9, dec_point;
        digit_0 = findViewById(R.id.digit_0);
        digit_0.setTypeface(myTypeface);
        digit_1 = findViewById(R.id.digit_1);
        digit_1.setTypeface(myTypeface);
        digit_2 = findViewById(R.id.digit_2);
        digit_2.setTypeface(myTypeface);
        digit_3 = findViewById(R.id.digit_3);
        digit_3.setTypeface(myTypeface);
        digit_4 = findViewById(R.id.digit_4);
        digit_4.setTypeface(myTypeface);
        digit_5 = findViewById(R.id.digit_5);
        digit_5.setTypeface(myTypeface);
        digit_6 = findViewById(R.id.digit_6);
        digit_6.setTypeface(myTypeface);
        digit_7 = findViewById(R.id.digit_7);
        digit_7.setTypeface(myTypeface);
        digit_8 = findViewById(R.id.digit_8);
        digit_8.setTypeface(myTypeface);
        digit_9 = findViewById(R.id.digit_9);
        digit_9.setTypeface(myTypeface);
        dec_point = findViewById(R.id.dec_point);
        dec_point.setTypeface(myTypeface);
    }

    Bitmap bitmapBlurHis = null;

    // Bkav TienNVh : Load tab History và giao diện của tab History và Advanced
    public void onRefeshSaveHistory() {
        // Bkav TienNVh : Các phép toàn được lưu vào lịch sử bằng SharedPreferences
        // Bkav TienNVh : Phép toàn lưu theo : ví dụ  "3+5=8;"
        // Ngăn cách giữa các phép toán là ";"
        // Ngăn cách giữa phép tính và kết quả là "="
        String savehistory = mSharedPreferences.getString(NAME_FILE_SHAREDPREFERENCES, "");
        if (!savehistory.equals("")) {
            String sliptSaveHistory[] = savehistory.split(";");
            mListHistory = new ArrayList<String>(Arrays.asList(sliptSaveHistory));
            mHistoryAdapter = new BkavHistoryAdapter(getApplication(), mListHistory);
            // Bkav TienNVh :  Xử lý click vào item History
            mHistoryAdapter.setmOnClickItemSaveHistory(new BkavHistoryAdapter.onClickItemSaveHistory() {
                @Override
                public void onClick(String result) {
                    for (int i = 0; i < result.length(); i++) {
                        char splitFormulatext = result.charAt(i);
                        if (KeyMaps.keyForDigVal((int) splitFormulatext) == View.NO_ID) {
                            if (KeyMaps.keyForChar(splitFormulatext) != View.NO_ID) {
                                addExplicitKeyToExpr(KeyMaps.keyForChar(splitFormulatext));
                            }
                        } else {
                            addExplicitKeyToExpr(KeyMaps.keyForDigVal((int) splitFormulatext));
                        }
                    }
                    restoreDisplay();
                    // Bkav TienNVh :dịch chuyển con trỏ về cuối cùng
                    mFormulaText.setSelection(mFormulaText.getText().length());
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setReverseLayout(false);
            layoutManager.setStackFromEnd(true);
            mRecyclerViewSaveHistory.setLayoutManager(layoutManager);
            mRecyclerViewSaveHistory.setAdapter(mHistoryAdapter);
            mRecyclerViewSaveHistory.scrollToPosition(mListHistory.size() - 1);
            mRecyclerViewSaveHistory.setVisibility(View.VISIBLE);
            findViewById(R.id.emptyElement).setVisibility(View.GONE);
            findViewById(R.id.delHistory).setVisibility(View.VISIBLE);
        } else {
            // Bkav TienNVh :  Trường hợp không có lịch sử
            findViewById(R.id.emptyElement).setVisibility(View.VISIBLE);
            findViewById(R.id.delHistory).setVisibility(View.GONE);
            mRecyclerViewSaveHistory.setVisibility(View.GONE);
        }

        // Bkav TienNVh :  Chan click xuyen history
        findViewById(R.id.emptyElement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    //    Bkav Phongngb tao ra 1 bitmap b1 tuong tu nhu mot bitmap thu 2
    public Bitmap overlayBitmap(Bitmap b1, /*onto*/Bitmap b2, int alpha) {
        Paint p = new Paint();
        p.setAlpha(alpha);
        Bitmap bmOverlay = Bitmap.createBitmap(b2.getWidth(), b2.getHeight(), b2.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        Matrix matrix = new Matrix();
        canvas.drawBitmap(b2, matrix, null);
        canvas.drawBitmap(b1, matrix, p);
        return bmOverlay;
    }

    //    Bkav Phongngb convert view to bitmap
    private Bitmap convertViewToBitmap(View view) {
//        if (view != null)
//            view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bm = view.getDrawingCache();
//        return bm;
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return b;
    }

    //Bkav TienNVh : Cut bitmap phan nam duoi history
    private Bitmap cutImageToBackgroundHistory(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight =mDragLayout.getHeight();
        int heightChild = mRelativeLayoutHistory.getHeight();
        int widthChild =  mRelativeLayoutHistory.getWidth();
        Bitmap cutBitmap = null;
            int y = mScreenHeight - heightChild;
                cutBitmap = Bitmap.createBitmap(bitmap, 0,   y, widthChild, heightChild);
        return cutBitmap;
    }

    // Bkav TienNVh : cutbitmap phan nam duoi advence
    private Bitmap cutImageToBackgroundAdvence(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight =mDragLayout.getHeight();
        int heightChild = mRelativeLayoutHistory.getHeight();

        Bitmap cutBitmap = null;
        cutBitmap = Bitmap.createBitmap(bitmap, (int) (bitmap.getWidth() * 0.2), mScreenHeight - heightChild, (int) (bitmap.getWidth() * 0.8), heightChild );
        return cutBitmap;
    }


    // Bkav TienNVh :  Hàm này dùng đề cắt chuỗi thành ký tự. sau đó chuyển sang id. sau đó add vào mảng Expr
    // Bkav TienNVh :  Các ký tự đặc biệt như sin( , cos( .... xét từng trường hợp để lấy lại id
    // và dịch chuyển con trỏ theo sau các ký tự đặc biệt
    // VD : chuổi truyền vào là 123sin( thì hàm tiến hành cắt từ trái sang ta được các ký tự là 1, 2,3, sin(
    // cắt 1,2,3 thì đơn giản còn cắt sin( cắt đến 4 ký tự

    // Bkav TienNVh :  Check ngôn ngữ để thay đổi dấu phẩy cho phù hợp với ngôn ngữ
    // Bkav TienNVh : Có thể thay hàm addChar
//
//    public void addExplicitStringToExpr(String formulatext) {
//        mEvaluator.clearMain();
//        // Bkav TienNVh : Check chuỗi khác rỗng
//        if (!formulatext.equals("")) {
//            // Bkav TienNVh :  Cắt các ký tự trong chuỗi
//            for (int i = 0; i < formulatext.length(); ) {
//                // Bkav TienNVh : Lay tung ky tu trong chuoi
//                char splitFormulatext = formulatext.charAt(i);
//                // Bkav TienNVh : xử lý dấu ngăn cách và dấu phẩy
//                if (splitFormulatext == ',' || splitFormulatext == '.') {
//                    // Bkav TienNVh : Trường hợp Ngôn ngữ tiếng việt dấu ',' tương ứng với dấu phẩy và dấu ngăn cách là '.'
//                    // Bkav TienNVh : Trường  hợp ngôn ngữ khác tiếng viết '.' tương ứng với dấu phẩy và dấu ngăn cách là dấu ','
//                    if ((splitFormulatext == ',' && Locale.getDefault().toString().equals("vi_VN")) ||
//                            (splitFormulatext == '.' && !Locale.getDefault().toString().equals("vi_VN"))) {
//                        // Bkav TienNVh : Trường hợp dấu phẩy
//                        addExplicitKeyToExpr(R.id.dec_point);
//                        i++;
//                        mUnprocessedChars = null;
//                        continue;
//                    } else {
//                        // Bkav TienNVh : Trường hợp dấu ngăn cách
//                        i++;
//                        mUnprocessedChars = null;
//                        continue;
//                    }
//                }
//                // Bkav TienNVh :  Kiểm tra ký tự có phải số không ?
//                if (KeyMaps.keyForDigVal((int) splitFormulatext) == View.NO_ID) {
//                    // Bkav TienNVh : Kiểm tra ký tự có phải phép tính không?
//                    if (KeyMaps.keyForChar(splitFormulatext) != View.NO_ID) {
//                        // Bkav TienNVh : check i < formulatext.length() - 2  để tránh trường hợp lỗi do ko có vị trí i+2
//                        // trường hợp trùng  e với exp() , để phân biệt thì phải dựa vào ký tự p . 'e'chuyển sang byte được 112 tượng tư 'p'=112
//                        if (i < formulatext.length() - 2 && (byte) formulatext.charAt(i + 2) == 112 && splitFormulatext == 'e' && (byte) formulatext.charAt(i + 1) == 120) {// Bkav TienNVh : 'p'=112
//                            // Bkav TienNVh :sau khi lọc ký tự là exp( thì add vào mảng Expr
//                            addExplicitKeyToExpr(R.id.fun_exp);
//                            // Bkav TienNVh : Nếu đang trong trạng thái lỗi thì chuyển sang hết lỗi
//                            // Bkav TienNVh :  TH lỗi khi mUnprocessedChars != null
//                            mUnprocessedChars = null;
//                            // Bkav TienNVh : tăng lên 4 vì độ dài chuỗi exp( là 4
//                            i = i + 4;
//                            // Bkav TienNVh : Tiếp tục vòng lặp với i tăng lên 4
//                            continue;
//                        } else {
//                            // Bkav TienNVh :  Trường hợp ký tự là hằng số 'e ' chuyển sang id sau đó add vào mảng Expr
//                            addExplicitKeyToExpr(KeyMaps.keyForChar(splitFormulatext));
//                            mUnprocessedChars = null;
//                            i++;
//                            continue;
//                        }
//                    } else {
//                        // Bkav TienNVh :  dấu căn chuyển sang byte là 26
//                        if ((byte) splitFormulatext == 26) {
//                            addExplicitKeyToExpr(R.id.op_sqrt);
//                            mUnprocessedChars = null;
//                            i++;
//                            continue;
//                        } else {
//                            // Bkav TienNVh :  Các ký tự : sin, cos , tag , ln ,.. do cắt từng ký tự 1 => cắt đưược là 's', 'c',  't' ,'l'
//                            // Bkav TienNVh : Xét từng trường hợp : ký tự 's' thì có cả trường hợp acrsin . tương tự  cos , tan , ln
//                            switch (splitFormulatext) {
//                                case 's':
//                                    // Bkav TienNVh : Check trường hợp cắt được ký tự 's'
//                                    if (formulatext.length() > i + 3 && (byte) formulatext.charAt(i + 3) != 40) { // Bkav TienNVh :  '('= 40
//                                        // Bkav TienNVh : Check  TH: sin-1(
//                                        if ((byte) formulatext.charAt(i + 3) == 123) {// Bkav TienNVh :  '-'=123
//                                            addExplicitKeyToExpr(R.id.fun_arcsin);
//                                            mUnprocessedChars = null;
//                                            // Bkav TienNVh :  độ dài của sin-1( =6
//                                            i = i + 6;
//                                        } else {
//                                            // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm => sai cú pháp  (VD : si5555n => Báo lỗi cú pháp )
//                                            insertCharacters(formulatext);
//                                            return;
//                                        }
//                                    } else {
//                                        // Bkav TienNVh : Trường hợp sin(
//                                        if ((byte) formulatext.charAt(i + 1) == 105) { // Bkav TienNVh : 'i'=105
//                                            addExplicitKeyToExpr(R.id.fun_sin);
//                                            i = i + 4;
//                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                            mUnprocessedChars = null;
//                                        } else {
//                                            // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
//                                            insertCharacters(formulatext);
//                                            return;
//                                        }
//                                    }
//                                    continue;
//                                case 'c':
//                                    if (formulatext.length() > i + 3 && (byte) formulatext.charAt(i + 3) != 40) {
//                                        if ((byte) formulatext.charAt(i + 3) == 94) {// Bkav TienNVh :  '-'=94
//                                            addExplicitKeyToExpr(R.id.fun_arccos);
//                                            i = i + 6;
//                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                            mUnprocessedChars = null;
//                                        } else {
//                                            // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
//                                            insertCharacters(formulatext);
//                                            return;
//                                        }
//                                    } else {
//                                        addExplicitKeyToExpr(R.id.fun_cos);
//                                        i = i + 4;
//                                        // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                        mUnprocessedChars = null;
//                                    }
//                                    continue;
//                                case 't':
//                                    if (formulatext.length() > i + 3 && (byte) formulatext.charAt(i + 3) != 40) {
//                                        if ((byte) formulatext.charAt(i + 3) == 94) {// Bkav TienNVh :  '-'=94
//                                            addExplicitKeyToExpr(R.id.fun_arctan);
//                                            i = i + 6;
//                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                            mUnprocessedChars = null;
//                                        } else {
//                                            // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
//                                            insertCharacters(formulatext);
//                                            return;
//                                        }
//                                    } else {
//                                        addExplicitKeyToExpr(R.id.fun_tan);
//                                        i = i + 4;
//                                        // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                        mUnprocessedChars = null;
//                                    }
//                                    continue;
//                                case 'l':
//                                    if ((byte) formulatext.charAt(i + 2) == 103) { // Bkav TienNVh : 'g'=103
//                                        addExplicitKeyToExpr(R.id.fun_log);
//                                        i = i + 4;
//                                        // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                        mUnprocessedChars = null;
//                                    } else {
//                                        if ((byte) formulatext.charAt(i + 2) == 40) {// Bkav TienNVh : '('=40
//                                            addExplicitKeyToExpr(R.id.fun_ln);
//                                            i = i + 3;
//                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
//                                            mUnprocessedChars = null;
//                                        } else {
//                                            // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
//                                            insertCharacters(formulatext);
//                                            return;
//                                        }
//                                    }
//                                    continue;
//
//                                default:
//                                    // Bkav TienNVh :  Trường hợp : x^2
//                                    if ((byte) formulatext.charAt(i) == -78) {
//                                        addExplicitKeyToExpr(R.id.op_sqr);
//                                        i++;
//                                        mUnprocessedChars = null;
//                                        continue;
//                                    } else {
//                                        mEvaluator.clearMain();
//                                        mUnprocessedChars = formulatext;
//                                        return;
//                                    }
//                            }
//                        }
//                    }
//                } else {
//                    addExplicitKeyToExpr(KeyMaps.keyForDigVal((int) splitFormulatext));
//                    i++;
//                    mUnprocessedChars = null;
//                }
//            }
//        }
//    }

    // Bkav TienNVh : truong hop co ky tu chen  o giua cum
    public void insertCharacters(String formulatext) {
        mEvaluator.clearMain();
        mUnprocessedChars = formulatext;
        if (haveUnprocessed()) {
            mFormulaText.setText(mUnprocessedChars);
        } else {
            mFormulaText.setText(formulatext);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    private CheckPermission mCheckPermission;
    private WallpaperBlurCompat mWallpaperBlurCompat;

    // Bkav TienNVh : lay hinh nen
    private Bitmap getBluredBackgroundFromRom() {
        if (mWallpaperBlurCompat == null) {
            mWallpaperBlurCompat = new WallpaperBlurCompat(this, mCheckPermission);
        }
        return mWallpaperBlurCompat.getWallpaperBlur();
    }

    // Bkav TienNVh : set background cho hinh nen
    private void setBlurBackground() {
        Bitmap backgroundBitmapFromRom = getBluredBackgroundFromRom();
        mDragLayout.setBackground(new BitmapDrawable(backgroundBitmapFromRom));
        if (mPadViewPager != null)
            mPadViewPager.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void denyPermission(String[] pers) {

    }

    // Bkav TienNVh : Cap quyen
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void acceptPermission(String[] pers) {
        if (mCheckPermission != null && mCheckPermission.canAccessWriteStorage()) {
            setBlurBackground();
        }
    }

    @Override
    public void alwaysDeny(String[] pers) {

    }

    //Bkav QuangNDb ham de len statusbar
    private void overlapStatusbar() {
        //Bkav QuangNDb overlap statusbar
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    // Bkav TienNVh : Xu ly dau phay theo ngon ngu
    @Override
    protected void onResume() {
        super.onResume();
        mEvaluator.clearMain();
        String language = mSharedPreferences.getString(SHAREDPREFERENCES_LANGUAGE, LANGUAGE_VN);
        String formulaText = mSharedPreferences.getString(SHAREDPREFERENCES_FORMULATEXT, "");
        String languageCurrent = Locale.getDefault().toString();
        if (!language.equals("")) {
            // Bkav TienNVh : Chuyen dau phay cho phu hop voi ngon ngu
            if (!language.equals(languageCurrent)) {
                if (languageCurrent.equals(LANGUAGE_VN)) {
                    formulaText = formulaText.replace(",", "");
                    formulaText = formulaText.replace(".", ",");
                } else {
                    formulaText = formulaText.replace(".", "");
                    formulaText = formulaText.replace(",", ".");
                }
            }
        }
//        addExplicitStringToExpr(formulaText);
        addChars(formulaText, false);
        restoreDisplay();
        if (mFormulaText.length() > 0)
            mFormulaText.setSelection(mFormulaText.length());

        // If HistoryFragment is showing, hide the main Calculator elements from accessibility.
        // This is because Talkback does not use visibility as a cue for RelativeLayout elements,
        // and RelativeLayout is the base class of DragLayout.
        // If we did not do this, it would be possible to traverse to main Calculator elements from
        // HistoryFragment.
        mMainCalculator.setImportantForAccessibility(
                mDragLayout.isOpen() ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        mEvaluator.cancelAll(true);
        // If there's an animation in progress, cancel it first to ensure our state is up-to-date.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        super.onSaveInstanceState(outState);
        outState.putInt(KEY_DISPLAY_STATE, mCurrentState.ordinal());
        outState.putCharSequence(KEY_UNPROCESSED_CHARS, mUnprocessedChars);
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(byteArrayStream)) {
            mEvaluator.saveInstanceState(out);
        } catch (IOException e) {
            // Impossible; No IO involved.
            throw new AssertionError("Impossible IO exception", e);
        }
        outState.putByteArray(KEY_EVAL_STATE, byteArrayStream.toByteArray());
        outState.putBoolean(KEY_INVERSE_MODE, mInverseToggle.isSelected());
        outState.putBoolean(KEY_SHOW_TOOLBAR, mDisplayView.isToolbarVisible());
        // We must wait for asynchronous writes to complete, since outState may contain
        // references to expressions being written.

        mEvaluator.waitForWrites();
    }

    // Set the state, updating delete label and display colors.
    // This restores display positions on moving to INPUT.
    // But movement/animation for moving to RESULT has already been done.
    private void setState(CalculatorState state) {
        if (mCurrentState != state) {
            if (state == CalculatorState.INPUT) {
                // We'll explicitly request evaluation from now on.
                mResultText.setShouldEvaluateResult(CalculatorResult.SHOULD_NOT_EVALUATE, null);
                restoreDisplayPositions();
            }
            mCurrentState = state;

            if (mCurrentState == CalculatorState.RESULT) {
                // No longer do this for ERROR; allow mistakes to be corrected.
                mDeleteButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
            } else {
                mDeleteButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.GONE);
            }
            if (mIsOneLine) {
                if (mCurrentState == CalculatorState.RESULT
                        || mCurrentState == CalculatorState.EVALUATE
                        || mCurrentState == CalculatorState.ANIMATE) {
                    mFormulaText.setVisibility(View.VISIBLE);
                    mResultText.setVisibility(View.VISIBLE);
                } else if (mCurrentState == CalculatorState.ERROR) {
                    mFormulaText.setVisibility(View.INVISIBLE);
                    mResultText.setVisibility(View.VISIBLE);
                } else {
                    mFormulaText.setVisibility(View.VISIBLE);
                    mResultText.setVisibility(View.INVISIBLE);
                }
            }

            if (mCurrentState == CalculatorState.ERROR) {
                final int errorColor =
                        ContextCompat.getColor(this, R.color.calculator_error_color);
                mFormulaText.setTextColor(errorColor);
                mResultText.setTextColor(errorColor);
                // Bkav TienNVh : Không cho setStatusBar vì theo kịch bản để StatusBar trong suốt
                //getWindow().setStatusBarColor(errorColor);
            } else if (mCurrentState != CalculatorState.RESULT) {
                mFormulaText.setTextColor(
                        ContextCompat.getColor(this, R.color.display_formula_text_color));
                mResultText.setTextColor(
                        ContextCompat.getColor(this, R.color.display_result_text_color));
                // Bkav TienNVh : Không cho setStatusBar vì theo kịch bản để StatusBar trong suốt
                //getWindow().setStatusBarColor(
                //        ContextCompat.getColor(this, R.color.calculator_statusbar_color));
            }
            // Bkav TienNVh : Set thanh status bar trong suot
            invalidateOptionsMenu();
        }
    }

    public boolean isResultLayout() {
        // Note that ERROR has INPUT, not RESULT layout.
        return mCurrentState == CalculatorState.INIT_FOR_RESULT
                || mCurrentState == CalculatorState.RESULT;
    }

    public boolean isOneLine() {
        return mIsOneLine;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TienNvh : Luu phep tinh cuoi cung
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SHAREDPREFERENCES_FORMULATEXT, mFormulaText.getText() + "");
        editor.putString(SHAREDPREFERENCES_LANGUAGE, Locale.getDefault().toString() + "");
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        mEvaluator.delete();
        mDragLayout.removeDragCallback(this);
        super.onDestroy();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        // Bkav TienNVh : Khi chuyển giữa chia đôi gai diện về 1 giao diện thì đóng các tab trở về tab chính
        mPadViewPager.setCurrentItem(1);
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
    }

    /**
     * Destroy the evaluator and close the underlying database.
     */
    public void destroyEvaluator() {
        mEvaluator.destroyEvaluator();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (mode.getTag() == CalculatorFormula.TAG_ACTION_MODE) {
            mFormulaContainer.scrollTo(mFormulaText.getRight(), 0);
        }
    }

    /**
     * Stop any active ActionMode or ContextMenu for copy/paste actions.
     * Return true if there was one.
     */
    private boolean stopActionModeOrContextMenu() {
        return mResultText.stopActionModeOrContextMenu()
                || mFormulaText.stopActionModeOrContextMenu();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        // If there's an animation in progress, end it immediately, so the user interaction can
        // be handled.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.end();
        }
    }

    // Bkav TienNVh :
    @Override
    public void onBackPressed() {
        if (!stopActionModeOrContextMenu()) {
            // Bkav TienNVh : Xu ly vuot back
            // Bkav TienNVh : Khi đang mở tab khác . nếu vuốt back thì trở về trang chính
            if (mPadViewPager.getCurrentItem() != 1) {
                mPadViewPager.setCurrentItem(1);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Allow the system to handle special key codes (e.g. "BACK" or "DPAD").
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return super.onKeyUp(keyCode, event);
        }

        // Stop the action mode or context menu if it's showing.
        stopActionModeOrContextMenu();

        // Always cancel unrequested in-progress evaluation of the main expression, so that
        // we don't have to worry about subsequent asynchronous completion.
        // Requested in-progress evaluations are handled below.
        cancelUnrequested();

        switch (keyCode) {
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mCurrentButton = mEqualButton;
                onEquals();
                return true;
            case KeyEvent.KEYCODE_DEL:
                mCurrentButton = mDeleteButton;
                onDelete();
                return true;
            case KeyEvent.KEYCODE_CLEAR:
                mCurrentButton = mClearButton;
                onClear();
                return true;
            default:
                cancelIfEvaluating(false);
                final int raw = event.getKeyCharacterMap().get(keyCode, event.getMetaState());
                if ((raw & KeyCharacterMap.COMBINING_ACCENT) != 0) {
                    return true; // discard
                }
                // Try to discard non-printing characters and the like.
                // The user will have to explicitly delete other junk that gets past us.
                if (Character.isIdentifierIgnorable(raw) || Character.isWhitespace(raw)) {
                    return true;
                }
                char c = (char) raw;
                if (c == '=') {
                    mCurrentButton = mEqualButton;
                    onEquals();
                } else {
                    addChars(String.valueOf(c), true);
                    redisplayAfterFormulaChange();
                }
                return true;
        }
    }

    /**
     * Invoked whenever the inverse button is toggled to update the UI.
     *
     * @param showInverse {@code true} if inverse functions should be shown
     */
    //Bkav TienNVh:
    private void onInverseToggled(boolean showInverse) {
        mInverseToggle.setSelected(showInverse);
        int orientation = getResources().getConfiguration().orientation;
        if (showInverse) {
            mInverseToggle.setContentDescription(getString(R.string.desc_inv_on));
            for (View invertibleButton : mInvertibleButtons) {
                invertibleButton.setVisibility(View.GONE);
            }

            for (View inverseButton : mInverseButtons) {
                inverseButton.setVisibility(View.VISIBLE);
            }
            //Bkav TienNVh: hide m+, m- (Xoay ngang)
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                findViewById(R.id.op_m_plus).setVisibility(View.GONE);
                findViewById(R.id.op_m_sub).setVisibility(View.GONE);
                findViewById(R.id.op_m_r).setVisibility(View.VISIBLE);
                findViewById(R.id.op_m_c).setVisibility(View.VISIBLE);
            }
        } else {
            mInverseToggle.setContentDescription(getString(R.string.desc_inv_off));
            for (View invertibleButton : mInvertibleButtons) {
                invertibleButton.setVisibility(View.VISIBLE);
            }
            for (View inverseButton : mInverseButtons) {
                inverseButton.setVisibility(View.GONE);
            }
            //Bkav TienNVh: show m+, m- (Xoay ngang)
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                findViewById(R.id.op_m_plus).setVisibility(View.VISIBLE);
                findViewById(R.id.op_m_sub).setVisibility(View.VISIBLE);
                findViewById(R.id.op_m_r).setVisibility(View.GONE);
                findViewById(R.id.op_m_c).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Invoked whenever the deg/rad mode may have changed to update the UI. Note that the mode has
     * not necessarily actually changed where this is invoked.
     *
     * @param degreeMode {@code true} if in degree mode
     */
    private void onModeChanged(boolean degreeMode) {
        if (degreeMode) {
            mModeView.setText(R.string.mode_deg);
            mModeView.setContentDescription(getString(R.string.desc_mode_deg));

            mModeToggle.setText(R.string.mode_rad);
            mModeToggle.setContentDescription(getString(R.string.desc_switch_rad));
        } else {
            mModeView.setText(R.string.mode_rad);
            mModeView.setContentDescription(getString(R.string.desc_mode_rad));

            mModeToggle.setText(R.string.mode_deg);
            mModeToggle.setContentDescription(getString(R.string.desc_switch_deg));
        }

    }

    private void removeHistoryFragment() {
        final FragmentManager manager = getFragmentManager();
        if (manager != null && !manager.isDestroyed()) {
            manager.popBackStack(HistoryFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // When HistoryFragment is hidden, the main Calculator is important for accessibility again.
        mMainCalculator.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }

    /**
     * Switch to INPUT from RESULT state in response to input of the specified button_id.
     * View.NO_ID is treated as an incomplete function id.
     */
    private void switchToInput(int button_id) {
        // Bkav TienNVh : Kiem tra id nay co phai id cua phep tinh khong?
        // Bkav TienNVh : Kiem tra id nay co phai id cua cac hau to (!,%..) khong?
        if (KeyMaps.isBinary(button_id) || KeyMaps.isSuffix(button_id)) {
            mEvaluator.collapse(mEvaluator.getMaxIndex() /* Most recent history entry */);
        } else {
            announceClearedForAccessibility();
            mEvaluator.clearMain();
        }
        setState(CalculatorState.INPUT);
    }

    /**
     * Add the given button id to input expression, assuming it was explicitly
     * typed/touched.
     * We perform slightly more aggressive correction than in pasted expressions.
     */
    private void addExplicitKeyToExpr(int id) {
        if (mCurrentState == CalculatorState.INPUT && id == R.id.op_sub) {
            // Bkav TienNVh : Trường hợp nhập số âm, format lai phep tinh
            mEvaluator.getExpr(Evaluator.MAIN_INDEX).removeTrailingAdditiveOperators();
        }
        // Bkav TienNVh : add id vao Mang Expr
        addKeyToExpr(id);
    }

    // Add the given button id to input expression.
    // If appropriate, clear the expression before doing so.
    private void addKeyToExpr(int id) {
        // Bkav TienNVh : Khi nhap phep tinh chuyen sang trang thai INPUT
        if (mCurrentState == CalculatorState.ERROR) {
            setState(CalculatorState.INPUT);
        } else if (mCurrentState == CalculatorState.RESULT) {
            // Bkav TienNVh : Check trong trang thai lay ket qua de tiep tuc tinh
            switchToInput(id);
        }
        if (!mEvaluator.append(id)) {
            // TODO: Some user visible feedback?
        }
    }

    public void evaluateInstantIfNecessary() {
        if (mCurrentState == CalculatorState.INPUT
                && mEvaluator.getExpr(Evaluator.MAIN_INDEX).hasInterestingOps()) {
            mEvaluator.evaluateAndNotify(Evaluator.MAIN_INDEX, this, mResultText);
        }
    }

    private void redisplayAfterFormulaChange() {
        // TODO: Could do this more incrementally.
        redisplayFormula();
        setState(CalculatorState.INPUT);
        mResultText.clear();
        if (haveUnprocessed()) {
            // Force reevaluation when text is deleted, even if expression is unchanged.
            mEvaluator.touch();
        } else {
            evaluateInstantIfNecessary();
        }
    }

    /**
     * Show the toolbar.
     * Automatically hide it again if it's not relevant to current formula.
     */
    private void showAndMaybeHideToolbar() {
        final boolean shouldBeVisible =
                mCurrentState == CalculatorState.INPUT && mEvaluator.hasTrigFuncs();
        mDisplayView.showToolbar(!shouldBeVisible);
    }

    /**
     * Display or hide the toolbar depending on calculator state.
     */
    private void showOrHideToolbar() {
        final boolean shouldBeVisible =
                mCurrentState == CalculatorState.INPUT && mEvaluator.hasTrigFuncs();
        if (shouldBeVisible) {
            mDisplayView.showToolbar(false);
        } else {
            mDisplayView.hideToolbar();
        }
    }

    // Bkav TienNVh :  Hàm chứa tất cả sự kiện click của app
    public void onButtonClick(View view) {
        // Any animation is ended before we get here.
        mCurrentButton = view;
        int postionCursor = mFormulaText.getSelectionEnd(); // vi tri con tro
        stopActionModeOrContextMenu();
        // See onKey above for the rationale behind some of the behavior below:
        cancelUnrequested();
        int orientation = getResources().getConfiguration().orientation;
        final int id = view.getId();
        switch (id) {
            // Bkav TienNVh : Tab tinh nang mo rong
            case R.id.bt_more:
                if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 1) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() + 1);
                } else if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 2) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                }
                break;
            // Bkav TienNVh : Delete History
            case R.id.delHistory:
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(NAME_FILE_SHAREDPREFERENCES , "");
                editor.apply();
                onRefeshSaveHistory();
                break;
            // Bkav TienNVh : Tab History
            case R.id.bt_history:
                // Bkav TienNVh :
                mDisplayView.setEnableToolbar(true);
                if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 1) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                } else if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 0) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() + 1);
                }
                break;
            case R.id.eq:
                mEvaluator.setmStatusM(false);
                onEquals();
                break;
            case R.id.del:
                onDelete();
                break;
            case R.id.clr:
                onClear();
                return;  // Toolbar visibility adjusted at end of animation.
            case R.id.toggle_inv:
                final boolean selected = !mInverseToggle.isSelected();
                mInverseToggle.setSelected(selected);
                onInverseToggled(selected);
                if (mCurrentState == CalculatorState.RESULT) {
                    mResultText.redisplay();   // In case we cancelled reevaluation.
                }
                break;
            case R.id.toggle_mode:
                cancelIfEvaluating(false);
                final boolean mode = !mEvaluator.getDegreeMode(Evaluator.MAIN_INDEX);
                if (mCurrentState == CalculatorState.RESULT
                        && mEvaluator.getExpr(Evaluator.MAIN_INDEX).hasTrigFuncs()) {
                    // Capture current result evaluated in old mode.
                    mEvaluator.collapse(mEvaluator.getMaxIndex());
                    redisplayFormula();
                }
                // In input mode, we reinterpret already entered trig functions.
                mEvaluator.setDegreeMode(mode);
                onModeChanged(mode);
                // Show the toolbar to highlight the mode change.
                showAndMaybeHideToolbar();
                setState(CalculatorState.INPUT);
                mResultText.clear();
                if (!haveUnprocessed()) {
                    evaluateInstantIfNecessary();
                }
                return;
            //Bkav  TienNVh: Click cac nut mc , mr, m+, m-

            case R.id.op_m_c:
//                mBkavMemoryFunction.onClearMemory();
                mEvaluator.clearEverything();
                mModeViewM.setText("");
                Toast.makeText(getApplication(),"Đã xoá bộ nhớ tạm",Toast.LENGTH_LONG).show();
                return;

            // Bkav TienNVh : Tính năng Mr : Lấy dữ liệu trong bộ nhớ tạm
            case R.id.op_m_r:
                clearIfNotInputState();
                long memoryIndex = mEvaluator.getMemoryIndex();
                if (memoryIndex != 0) {
                    mEvaluator.appendExpr(mEvaluator.getMemoryIndex());
                    redisplayAfterFormulaChange();
                }
//                mEvaluator.clearMain();
//                addChars(mBkavMemoryFunction.onRecallMemory(), false);
//                redisplayAfterFormulaChange();
//                onEquals();
//               if (mBkavMemoryFunction.isExitMemory())
                // Bkav TienNVh : Dịch chuyển con trỏ cuối cùng
                mFormulaText.setSelection(mFormulaText.getText().length());
                if (mEvaluator.exitExprs())
                    mModeViewM.setText("M");
                return;
            // Bkav TienNVh : Tính năng m+ : thêm  vào bộ nhớ tam
            // Bkav TienNVh : Trang thai
            //CalculatorState.EVALUATE : Phep tinh khong hop le
            // CalculatorState.ANIMATE  : Pheps tinh hop le
            // CalculatorState.INPUT : trang thai nguoi nhap vao
            //CalculatorState.RESULT : co ket qua
            case R.id.op_m_plus:
                // Bkav TienNVh : Thuc hien phep tinh
                onEquals();
                // Bkav TienNVh :  Khi nào có phép tinh đó hợp lệ thì mới dùng được tính năng M
                if (mCurrentState == CalculatorState.RESULT|| mCurrentState== CalculatorState.ANIMATE) {
                    mEvaluator.addToMemory(mResultText.getmIndex());
                    mFormulaText.setSelection(mFormulaText.getText().length());
                }
                if (mEvaluator.exitExprs())
                    mModeViewM.setText("M");
                mEvaluator.setmStatusM(true);
//                // Bkav TienNVh :  Tránh trường hợp ko có dữ liệu
//                if(mResultText.getText().length()>0) {
//                    // Bkav TienNVh : Trường hợp phép tính hợp lệ
//                    if (mCurrentState == CalculatorState.ANIMATE) {
//                        mBkavMemoryFunction.onMPlusAddMemory(mResultText.getText().toString());
//                    } else {
//                        // Bkav TienNVh : TH: ko phải phép tính
//                        if (mCurrentState == CalculatorState.INPUT)
//                            mBkavMemoryFunction.onMPlusAddMemory(mFormulaText.getText().toString());
//                    }
//                }
//                // Bkav TienNVh : kiểm tra bộ nhớ đã tồn tại , mục đích hiện thị 'M' trên màn hình
//                if (mBkavMemoryFunction.isExitMemory())
//                    mModeViewM.setText("M");
//                    // Bkav TienNVh : bật trạng thái M , mục đích
//                    mBkavMemoryFunction.setmStatusM(true);
                return;
            // Bkav TienNVh :  m- tương tự như m+
            case R.id.op_m_sub:
                onEquals();
                if (mCurrentState == CalculatorState.RESULT|| mCurrentState== CalculatorState.ANIMATE) {
                    mEvaluator.subtractFromMemory(mResultText.getmIndex());
                    mFormulaText.setSelection(mFormulaText.getText().length());
                }
//                if(mResultText.getText().length()>0) {
//                    if (mCurrentState == CalculatorState.ANIMATE) {
//                        mBkavMemoryFunction.onMSubAddMemory(mResultText.getText().toString());
//                    } else {
//                        if (mCurrentState == CalculatorState.INPUT)
//                            mBkavMemoryFunction.onMSubAddMemory(mFormulaText.getText().toString());
//                    }
//                }
                if (mEvaluator.exitExprs())
                    mModeViewM.setText("M");
                mEvaluator.setmStatusM(true);
                return;

            //Bkav  TienNVh: Click cac nut % , ! , pi dong item mo rong
            case R.id.op_fact:
            case R.id.op_pct:
            case R.id.const_pi:
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mPadViewPager != null) {
                        mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                    }
                }
                // Bkav TienNVh : Các trường hợp còn lại là các ký tự
                // Để Mục đích là thêm vào id vao Mang Expr  đúng trình tự  thì phải
                // chuyển id sang string sau đó chèn vào vị trí mình mong muốn
                // sau đó cắt String thành các ký tự ,sau đó chuyển ký tự đó sang id ròi add vào Mảng Expr
                // mỗi khi click ký tự thì được xoá sạch rồi sau đó thưc hiện tính lại
            default:
                cancelIfEvaluating(false);
                // addChars(KeyMaps.toString(this, id), true);
                //addExplicitKeyToExpr(id);
                // Bkav TienNVh : Lấy nội dung đang hiện thị
                String formulatext = mFormulaText.getText().toString();
                // Bkav TienNVh :  Chuyển từ id sang giá trị
                String newtext = KeyMaps.toString(this, id);
                // Bkav TienNVh : Truowng hop lay ket qua de tiep tuc tinh tiep
                if (mCurrentState == CalculatorState.RESULT) {
                    // Bkav TienNVh : Check xem ký tự nhập tiếp theo có phải phép tính ko?
                    // Nếu phải thì giữ kết quả để tính tiếp , ngược lại thì xoá kết quả
                    if (checkFormulaNext(id)) {
                        // Bkav TienNVh : Trong trường hợp lấy kết quả để tính tiếp thì chèn ký tự vừa nhập vào sau kết quả
                        formulatext = mTruncatedWholeNumber + newtext;
                        postionCursor = formulatext.length() + newtext.length() - 1;
                    } else {
                        formulatext = newtext;
                        // Bkav TienNVh :  Sau khi xoá thì reset lại vị trí con trỏ
                        postionCursor = 0;
                        mPostionCursorToRight=0;
                    }
                }
                int lengthold = formulatext.length();// do dai cua chuoi
                if (lengthold >= postionCursor || mCurrentState == CalculatorState.RESULT) {
                    // Bkav TienNVh : Chen chuoi moi nhap vao vi tri con tro
                    String formulaText = formulatext;
                    // Bkav TienNVh : Trong trường hợp lấy kết quả để tính tiếp thì mặc đinh chèn ký tự vào phía sau kết quả => bỏ qua đoạn chèn
                    if (mCurrentState != CalculatorState.RESULT) {
                        mPostionCursorToRight = lengthold - postionCursor;// Bkav TienNVh : Vi tri con tro tinh tu ben phai sang
                        String slipt1 = formulatext.substring(0, mFormulaText.getSelectionStart());
                        String slipt2 = formulatext.substring(mFormulaText.getSelectionEnd(), lengthold);
                        formulaText = slipt1 + newtext + slipt2;
                    }
                    // Bkav TienNVh : Xoa tat ca cac phep tinh cu
                    mEvaluator.clearMain();
                    // Bkav TienNVh : Them Cac phep tinh sau khi sua
                    //            addExplicitStringToExpr(formulaText);
                    addChars(formulaText, false);
                    // Bkav TienNVh : tính năng mở rộng thêm dấu ( sau các sin( , cos(...
                    if (KeyMaps.isFunc(id)) {
                        addExplicitKeyToExpr(R.id.rparen);
                        mPostionCursorToRight++;
                    }
                    // Bkav TienNVh : Show ket qua neu chuoi ky tu nhap vao la phep tinh
                    redisplayAfterFormulaChange();
                    // Bkav TienNVh : Thay doi vi tri con tro sau
                    changePostionCursor();
                }
                break;
        }
    }
// Bkav TienNVh :
    private  boolean checkFormulaNext(int id){
        if(KeyMaps.isBinary(id)||KeyMaps.isSuffix(id))
            return true;
        return false;
    }

    // Bkav TienNVh : Biến chỉ vị trí con tro đếm từ bên phải sang
    private int mPostionCursorToRight = 0;

    // Bkav TienNVh : Cập nhật lại vị trí con trỏ
    public void changePostionCursor() {
        int lengthold = mFormulaText.length();// do dai cua chuoi
        // Bkav TienNVh : Xét trường hợp
        if (lengthold >= mPostionCursorToRight && mPostionCursorToRight != 0) {
            // Bkav TienNVh :  Set lại vị trí cảu con trỏ
            mFormulaText.setSelection(lengthold - mPostionCursorToRight);
        } else {
            // Bkav TienNVh :  Set vi trí con tro ở cuối cùng
            if (mPostionCursorToRight == 0)
                mFormulaText.setSelection(lengthold);
        }

    }

    void redisplayFormula() {
        SpannableStringBuilder formula
                = mEvaluator.getExpr(Evaluator.MAIN_INDEX).toSpannableStringBuilder(this);
        if (mUnprocessedChars != null) {
            // Add and highlight characters we couldn't process.
            formula.append(mUnprocessedChars, mUnprocessedColorSpan,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mFormulaText.changeTextTo(formula);
        mFormulaText.setContentDescription(TextUtils.isEmpty(formula)
                ? getString(R.string.desc_formula) : null);
    }

    @Override
    public boolean onLongClick(View view) {
        mCurrentButton = view;
        if (view.getId() == R.id.del) {
            onClear();
            return true;
        }
        return false;
    }

    // Bkav TienNVh : Tạo biến để lưu kết quả đầy đủ trong trường hợp "E"
    String mTruncatedWholeNumber = null;

    // Initial evaluation completed successfully.  Initiate display.
    public void onEvaluate(long index, int initDisplayPrec, int msd, int leastDigPos,
                           String truncatedWholeNumber) {
        if (index != Evaluator.MAIN_INDEX) {
            throw new AssertionError("Unexpected evaluation result index\n");
        }

        // Invalidate any options that may depend on the current result.
        invalidateOptionsMenu();

        mResultText.onEvaluate(index, initDisplayPrec, msd, leastDigPos, truncatedWholeNumber);

        if (mCurrentState != CalculatorState.INPUT) {
            // In EVALUATE, INIT, RESULT, or INIT_FOR_RESULT state.
            // Bkav TienNVh : check muc dich la lay ket qua de tiep tuc tinh phep tinh tiep theo
            if (mResultText.getText().toString().contains("E")) {
                // Bkav TienNVh : Check ket qua cho ra so lon thi lay phan nguyen cua ket qua
                mTruncatedWholeNumber = truncatedWholeNumber;
            } else {
                // Bkav TienNVh :  Nguoc lai thi lay ket qua
                mTruncatedWholeNumber = mResultText.getText().toString();
            }
            onResult(mCurrentState == CalculatorState.EVALUATE /* animate */,
                    mCurrentState == CalculatorState.INIT_FOR_RESULT
                            || mCurrentState == CalculatorState.RESULT /* previously preserved */);

        }
    }

    // Reset state to reflect evaluator cancellation.  Invoked by evaluator.
    public void onCancelled(long index) {
        // Index is Evaluator.MAIN_INDEX. We should be in EVALUATE state.
        setState(CalculatorState.INPUT);
        mResultText.onCancelled(index);
    }

    // Reevaluation completed; ask result to redisplay current value.
    public void onReevaluate(long index) {
        // Index is Evaluator.MAIN_INDEX.
        mResultText.onReevaluate(index);
    }

    @Override
    public void onTextSizeChanged(final TextView textView, float oldSize) {
        if (mCurrentState != CalculatorState.INPUT) {
            // Only animate text changes that occur from user input.
            return;
        }

        // Calculate the values needed to perform the scale and translation animations,
        // maintaining the same apparent baseline for the displayed text.
        final float textScale = oldSize / textView.getTextSize();
        final float translationX = (1.0f - textScale) *
                (textView.getWidth() / 2.0f - textView.getPaddingEnd());
        final float translationY = (1.0f - textScale) *
                (textView.getHeight() / 2.0f - textView.getPaddingBottom());

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(textView, View.SCALE_X, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.SCALE_Y, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, translationX, 0.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, translationY, 0.0f));
        animatorSet.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Cancel any in-progress explicitly requested evaluations.
     *
     * @param quiet suppress pop-up message.  Explicit evaluation can change the expression
     *              value, and certainly changes the display, so it seems reasonable to warn.
     * @return true if there was such an evaluation
     */
    private boolean cancelIfEvaluating(boolean quiet) {
        if (mCurrentState == CalculatorState.EVALUATE) {
            mEvaluator.cancel(Evaluator.MAIN_INDEX, quiet);
            return true;
        } else {
            return false;
        }
    }

    private void cancelUnrequested() {
        if (mCurrentState == CalculatorState.INPUT) {
            mEvaluator.cancel(Evaluator.MAIN_INDEX, true);
        }
    }

    private boolean haveUnprocessed() {
        return mUnprocessedChars != null && !mUnprocessedChars.isEmpty();
    }

    private void onEquals() {
        // addChars(mFormulaText.getText() + "", false);
        //  addExplicitStringToExpr(mFormulaText.getText() + "");
        // Ignore if in non-INPUT state, or if there are no operators.
        if (mCurrentState == CalculatorState.INPUT) {
            if (haveUnprocessed()) {
                setState(CalculatorState.EVALUATE);
                onError(Evaluator.MAIN_INDEX, R.string.error_syntax);
            } else if (mEvaluator.getExpr(Evaluator.MAIN_INDEX).hasInterestingOps()) {
                setState(CalculatorState.EVALUATE);
                mEvaluator.requireResult(Evaluator.MAIN_INDEX, this, mResultText);
                //Bkav TienNVh: luu vao lich su
                if (mResultText.getText().length() != 0) {
                    String textNew = mFormulaText.getText() + "=" + mResultText.getText() + ";";
                    // Bkav TienNVh : Trường hợp kết quả có "E" thì lấy kết quả đầy đủ để lưu
                    if (mResultText.getText().toString().contains("E") && mTruncatedWholeNumber != null) {
                        textNew = mFormulaText.getText() + "=" + mTruncatedWholeNumber + ";";
                    }
                    // Bkav TienNVh : Tranh luu ket qua trung nhau
                    String savehistoryold = mSharedPreferences.getString(NAME_FILE_SHAREDPREFERENCES, "");
                    if (savehistoryold.contains(textNew)) {
                        savehistoryold = savehistoryold.replace(textNew, "");
                    }

                    // Bkav TienNVh : Luu ket qua vao lich su
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(NAME_FILE_SHAREDPREFERENCES, savehistoryold + textNew);
                    editor.apply();
                    onRefeshSaveHistory();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        // Bkav TienNVh : Luu trang thai ngon ngu.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SHAREDPREFERENCES_LANGUAGE, Locale.getDefault().toString());
        editor.apply();
        super.onPause();
    }

    // Bkav TienNVh : Hàm này code đang có một số lỗi . Khi nào sửa xong Sẽ format code luôn
    private void onDelete() {
        // Delete works like backspace; remove the last character or operator from the expression.
        // Note that we handle keyboard delete exactly like the delete button.  For
        // example the delete button can be used to delete a character from an incomplete
        // function name typed on a physical keyboard.
        // This should be impossible in RESULT state.
        // If there is an in-progress explicit evaluation, just cancel it and return.
        if (cancelIfEvaluating(false)) return;
        setState(CalculatorState.INPUT);
        if (haveUnprocessed()) {
            mUnprocessedChars = mFormulaText.getText() + "";
            mPostionCursorToRight = mUnprocessedChars.length() - mFormulaText.getSelectionEnd();
            if (mFormulaText.getSelectionEnd() >= 1 && mUnprocessedChars.length() > 0)
                mUnprocessedChars = mUnprocessedChars.substring(0, mFormulaText.getSelectionEnd() - 1) + mUnprocessedChars.substring(mFormulaText.getSelectionEnd());

            addChars(mUnprocessedChars, false);
            // addExplicitStringToExpr(mUnprocessedChars);
            changePostionCursor();
        } else {
            final Editable formulaText = mFormulaText.getEditableText();
            // Bkav TienNVh : lấy vị trí con trỏ tính từ bên trái
            int postionCursor = mFormulaText.getSelectionEnd();
            if (postionCursor == mFormulaText.getSelectionStart()) {
                // Bkav TienNVh : tính vị trí con trỏ tính từ bên phải sang
                // Bkav TienNVh : Mục đích chỉ thay đổi các ký tự trước con trỏ , còn sau con trỏ thì dự nguyên
                mPostionCursorToRight = formulaText.length() - postionCursor;
                final int formulaLength = formulaText.length() - mPostionCursorToRight;
                // Bkav TienNVh :  Lấy ngôn ngữ hiên đang dùng
                String locale = Locale.getDefault().toString();
                char comma;
                // Bkav TienNVh : Xet truong hop:  dau phay tuy thuoc vao ngon ngu
                if (locale.equals(LANGUAGE_VN)) {
                    // Bkav TienNVh :  Trường hợp ngôn ngữ tiếng việt thì dùng "." làm dấu phân cách giua 3 số nguyên
                    // Bkav TienNVh : Mục đích là nếu xoá dấu phân cach thì xoá cái số trước nó
                    comma = '.';
                } else {
                    // Bkav TienNVh : Trường hop ngược lại thì dùng dấu ',' để phân cách
                    comma = ',';
                }
                // Bkav TienNVh :
                if (formulaLength > 0) {
                    if (formulaText.charAt(formulaLength - 1) == comma) {
                        // Bkav TienNVh : Truong hop xoa dau ngan cach
                        formulaText.delete(formulaLength - 2, formulaLength);
                    } else {
                        // Bkav TienNVh : Trường hợp trước con trỏ là "("
                        if (formulaLength > 0 && formulaText.charAt(formulaLength - 1) == '(') {
                            // Bkav TienNVh :
                            if (formulaLength > 2 && (byte) formulaText.charAt(formulaLength - 3) == 123) {
                                //Bkav TienNVh TH: arccos() , arcsin() ,arctan()
                                formulaText.delete(formulaLength - 6, formulaLength);
                            } else {
                                if (formulaLength > 2 && formulaText.charAt(formulaLength - 3) == 'l') {
                                    //Bkav TienNVh TH: ln()
                                    formulaText.delete(formulaLength - 3, formulaLength);
                                } else {
                                    // Bkav TienNVh :  sin() , cos() , tan(), exp(), log()
                                    if (formulaLength > 3) {
                                        switch (formulaText.charAt(formulaLength - 4)) {
                                            case 'l':
                                                // Bkav TienNVh : TH:  xoa ln((
                                                if (formulaText.charAt(formulaLength - 2) == '(') {
                                                    formulaText.delete(formulaLength - 1, formulaLength);
                                                    break;
                                                }
                                            case 'c':
                                            case 's':
                                            case 't':
                                            case 'e':
                                                formulaText.delete(formulaLength - 4, formulaLength);
                                                break;
                                            default:// Bkav TienNVh :  Truong hop : '('
                                                formulaText.delete(formulaLength - 1, formulaLength);
                                                break;
                                        }
                                    } else {
                                        // Bkav TienNVh :  Truong hop : '('
                                        formulaText.delete(formulaLength - 1, formulaLength);
                                    }
                                }
                            }
                        } else {
                            // Bkav TienNVh : Xoá các ký tự có độ dài = 1
                            formulaText.delete(formulaLength - 1, formulaLength);
                        }

                    }
                }
                if (formulaText.length() == 1 && formulaText.charAt(0) == ')')
                    formulaText.clear();
            } else {
                formulaText.delete(mFormulaText.getSelectionStart(), mFormulaText.getSelectionEnd());
            }
            mEvaluator.clearMain();
            // Bkav TienNVh : Vị trí con trỏ sau khi xoá
            int postionCursor2 = mFormulaText.getSelectionStart();
            addChars(formulaText.toString(), true);

            // Bkav TienNVh : Hiện thị kết quả phép tính sau khi thay đổi (Xoá )
            redisplayAfterFormulaChange();
            // addExplicitStringToExpr(formulaText.toString());
            mPostionCursorToRight = formulaText.toString().length() - postionCursor2;
            // Bkav TienNVh : Cập nhật vị trí con trỏ sau khi xoá
            changePostionCursor();
        }
        if (mEvaluator.getExpr(Evaluator.MAIN_INDEX).isEmpty() && !haveUnprocessed()) {
            // Resulting formula won't be announced, since it's empty.
            announceClearedForAccessibility();
        }
        // Bkav TienNVh : Hiện thị kết quả phép tính sau khi thay đổi (Xoá )
        redisplayAfterFormulaChange();
        // Bkav TienNVh : Cập nhật vị trí con trỏ sau khi xoá
        changePostionCursor();
    }


    private void reveal(View sourceView, int colorRes, AnimatorListener listener) {
        final ViewGroupOverlay groupOverlay =
                (ViewGroupOverlay) getWindow().getDecorView().getOverlay();

        final Rect displayRect = new Rect();
        mDisplayView.getGlobalVisibleRect(displayRect);

        // Make reveal cover the display and status bar.ake
        final View revealView = new View(this);
        revealView.setBottom(displayRect.bottom);
        revealView.setLeft(displayRect.left);
        revealView.setRight(displayRect.right);
        revealView.setBackgroundColor(ContextCompat.getColor(this, colorRes));
        groupOverlay.add(revealView);

        final int[] clearLocation = new int[2];
        sourceView.getLocationInWindow(clearLocation);
        clearLocation[0] += sourceView.getWidth() / 2;
        clearLocation[1] += sourceView.getHeight() / 2;

        final int revealCenterX = clearLocation[0] - revealView.getLeft();
        final int revealCenterY = clearLocation[1] - revealView.getTop();

        final double x1_2 = Math.pow(revealView.getLeft() - revealCenterX, 2);
        final double x2_2 = Math.pow(revealView.getRight() - revealCenterX, 2);
        final double y_2 = Math.pow(revealView.getTop() - revealCenterY, 2);
        final float revealRadius = (float) Math.max(Math.sqrt(x1_2 + y_2), Math.sqrt(x2_2 + y_2));

        final Animator revealAnimator =
                ViewAnimationUtils.createCircularReveal(revealView,
                        revealCenterX, revealCenterY, 0.0f, revealRadius);
        revealAnimator.setDuration(
                getResources().getInteger(android.R.integer.config_longAnimTime));
        revealAnimator.addListener(listener);

        final Animator alphaAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f);
        alphaAnimator.setDuration(
                getResources().getInteger(android.R.integer.config_mediumAnimTime));

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(revealAnimator).before(alphaAnimator);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                groupOverlay.remove(revealView);
                mCurrentAnimator = null;
            }
        });

        mCurrentAnimator = animatorSet;
        animatorSet.start();
    }

    private void announceClearedForAccessibility() {
        mResultText.announceForAccessibility(getResources().getString(R.string.cleared));
    }

    public void onClearAnimationEnd() {
        mUnprocessedChars = null;
        mResultText.clear();
        mEvaluator.clearMain();
        setState(CalculatorState.INPUT);
        redisplayFormula();
    }

    private void onClear() {
        if (mEvaluator.getExpr(Evaluator.MAIN_INDEX).isEmpty() && !haveUnprocessed()) {
            return;
        }
        cancelIfEvaluating(true);
        announceClearedForAccessibility();
        reveal(mCurrentButton, R.color.calculator_primary_color, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onClearAnimationEnd();
                showOrHideToolbar();
            }
        });
    }

    // Evaluation encountered en error.  Display the error.
    @Override
    public void onError(final long index, final int errorResourceId) {
        if (index != Evaluator.MAIN_INDEX) {
            throw new AssertionError("Unexpected error source");
        }
        if (mCurrentState == CalculatorState.EVALUATE) {
            setState(CalculatorState.ANIMATE);
            mResultText.announceForAccessibility(getResources().getString(errorResourceId));
            reveal(mCurrentButton, R.color.calculator_error_color,
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setState(CalculatorState.ERROR);
                            mResultText.onError(index, errorResourceId);
                        }
                    });
        } else if (mCurrentState == CalculatorState.INIT|| mCurrentState == CalculatorState.ERROR//trong trường status: ERROR thì hiện thị dòng thông báo lỗi.
                || mCurrentState == CalculatorState.INIT_FOR_RESULT /* very unlikely */) {
            setState(CalculatorState.ERROR);
            mResultText.onError(index, errorResourceId);
        } else {
            mResultText.clear();
        }
    }

    // Animate movement of result into the top formula slot.
    // Result window now remains translated in the top slot while the result is displayed.
    // (We convert it back to formula use only when the user provides new input.)
    // Historical note: In the Lollipop version, this invisibly and instantaneously moved
    // formula and result displays back at the end of the animation.  We no longer do that,
    // so that we can continue to properly support scrolling of the result.
    // We assume the result already contains the text to be expanded.
    private void onResult(boolean animate, boolean resultWasPreserved) {
        // Calculate the textSize that would be used to display the result in the formula.
        // For scrollable results just use the minimum textSize to maximize the number of digits
        // that are visible on screen.
        float textSize = mFormulaText.getMinimumTextSize();
        if (!mResultText.isScrollable()) {
            textSize = mFormulaText.getVariableTextSize(mResultText.getText().toString());
        }

        // Scale the result to match the calculated textSize, minimizing the jump-cut transition
        // when a result is reused in a subsequent expression.
        final float resultScale = textSize / mResultText.getTextSize();

        // Set the result's pivot to match its gravity.
        mResultText.setPivotX(mResultText.getWidth() - mResultText.getPaddingRight());
        mResultText.setPivotY(mResultText.getHeight() - mResultText.getPaddingBottom());

        // Calculate the necessary translations so the result takes the place of the formula and
        // the formula moves off the top of the screen.
        final float resultTranslationY = (mFormulaContainer.getBottom() - mResultText.getBottom())
                - (mFormulaText.getPaddingBottom() - mResultText.getPaddingBottom());
        float formulaTranslationY = -mFormulaContainer.getBottom();
        if (mIsOneLine) {
            // Position the result text.
            mResultText.setY(mResultText.getBottom());
            formulaTranslationY = -(findViewById(R.id.toolbar).getBottom()
                    + mFormulaContainer.getBottom());
        }

        // Change the result's textColor to match the formula.
        final int formulaTextColor = mFormulaText.getCurrentTextColor();

        if (resultWasPreserved) {
            // Result was previously addded to history.
            mEvaluator.represerve();
        } else {
            // Add current result to history.
            mEvaluator.preserve(Evaluator.MAIN_INDEX, true);
        }

        if (animate) {
            mResultText.announceForAccessibility(getResources().getString(R.string.desc_eq));
            mResultText.announceForAccessibility(mResultText.getText());
            setState(CalculatorState.ANIMATE);
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofPropertyValuesHolder(mResultText,
                            PropertyValuesHolder.ofFloat(View.SCALE_X, resultScale),
                            PropertyValuesHolder.ofFloat(View.SCALE_Y, resultScale),
                            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, resultTranslationY)),
                    ObjectAnimator.ofArgb(mResultText, TEXT_COLOR, formulaTextColor),
                    ObjectAnimator.ofFloat(mFormulaContainer, View.TRANSLATION_Y,
                            formulaTranslationY));
            animatorSet.setDuration(getResources().getInteger(
                    android.R.integer.config_longAnimTime));
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setState(CalculatorState.RESULT);
                    mCurrentAnimator = null;
                }
            });

            mCurrentAnimator = animatorSet;
            animatorSet.start();
        } else /* No animation desired; get there fast when restarting */ {
            mResultText.setScaleX(resultScale);
            mResultText.setScaleY(resultScale);
            mResultText.setTranslationY(resultTranslationY);
            mResultText.setTextColor(formulaTextColor);
            mFormulaContainer.setTranslationY(formulaTranslationY);
            setState(CalculatorState.RESULT);
        }
    }

    // Restore positions of the formula and result displays back to their original,
    // pre-animation state.
    private void restoreDisplayPositions() {
        // Clear result.
        mResultText.setText("");
        // Reset all of the values modified during the animation.
        mResultText.setScaleX(1.0f);
        mResultText.setScaleY(1.0f);
        mResultText.setTranslationX(0.0f);
        mResultText.setTranslationY(0.0f);
        mFormulaContainer.setTranslationY(0.0f);

        mFormulaText.requestFocus();
    }

    @Override
    public void onClick(AlertDialogFragment fragment, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (HistoryFragment.CLEAR_DIALOG_TAG.equals(fragment.getTag())) {
                // TODO: Try to preserve the current, saved, and memory expressions. How should we
                // handle expressions to which they refer?
                mEvaluator.clearEverything();
                // TODO: It's not clear what we should really do here. This is an initial hack.
                // May want to make onClearAnimationEnd() private if/when we fix this.
                onClearAnimationEnd();
                mEvaluatorCallback.onMemoryStateChanged();
                onBackPressed();
            } else if (Evaluator.TIMEOUT_DIALOG_TAG.equals(fragment.getTag())) {
                // Timeout extension request.
                mEvaluator.setLongTimeout();
            } else {
                Log.e(TAG, "Unknown AlertDialogFragment click:" + fragment.getTag());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //   getMenuInflater().inflate(R.menu.activity_calculator, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Show the leading option when displaying a result.
        //  menu.findItem(R.id.menu_leading).setVisible(mCurrentState == CalculatorState.RESULT);
        // Show the fraction option when displaying a rational result.
        //boolean visible = mCurrentState == CalculatorState.RESULT;
        // final UnifiedReal mainResult = mEvaluator.getResult(Evaluator.MAIN_INDEX);
        // mainResult should never be null, but it happens. Check as a workaround to protect
        // against crashes until we find the root cause (b/34763650).
        //  visible &= mainResult != null && mainResult.exactlyDisplayable();
        //   menu.findItem(R.id.menu_fraction).setVisible(visible);
        return true;
    }


    /* Begin override CloseCallback method. */

    @Override
    public void onClose() {
        removeHistoryFragment();
    }

    /* End override CloseCallback method. */

    /* Begin override DragCallback methods */

    public void onStartDraggingOpen() {
        mDisplayView.hideToolbar();
        //  showHistoryFragment();
    }

    @Override
    public void onInstanceStateRestored(boolean isOpen) {
    }

    @Override
    public void whileDragging(float yFraction) {
    }

    @Override
    public boolean shouldCaptureView(View view, int x, int y) {
        return view.getId() == R.id.history_frame
                && (mDragLayout.isMoving() || mDragLayout.isViewUnder(view, x, y));
    }

    @Override
    public int getDisplayHeight() {
        return mDisplayView.getMeasuredHeight();
    }

    /* End override DragCallback methods */

    /**
     * Change evaluation state to one that's friendly to the history fragment.
     * Return false if that was not easily possible.
     */
    private boolean prepareForHistory() {
        if (mCurrentState == CalculatorState.ANIMATE) {
            // End the current animation and signal that preparation has failed.
            // onUserInteraction is unreliable and onAnimationEnd() is asynchronous, so we
            // aren't guaranteed to be out of the ANIMATE state by the time prepareForHistory is
            // called.
            if (mCurrentAnimator != null) {
                mCurrentAnimator.end();
            }
            return false;
        } else if (mCurrentState == CalculatorState.EVALUATE) {
            // Cancel current evaluation
            cancelIfEvaluating(true /* quiet */);
            setState(CalculatorState.INPUT);
            return true;
        } else if (mCurrentState == CalculatorState.INIT) {
            // Easiest to just refuse.  Otherwise we can see a state change
            // while in history mode, which causes all sorts of problems.
            // TODO: Consider other alternatives. If we're just doing the decimal conversion
            // at the end of an evaluation, we could treat this as RESULT state.
            return false;
        }
        // We should be in INPUT, INIT_FOR_RESULT, RESULT, or ERROR state.
        return true;
    }

    private HistoryFragment getHistoryFragment() {
        final FragmentManager manager = getFragmentManager();
        if (manager == null || manager.isDestroyed()) {
            return null;
        }
        final Fragment fragment = manager.findFragmentByTag(HistoryFragment.TAG);
        return fragment == null || fragment.isRemoving() ? null : (HistoryFragment) fragment;
    }

    private void showHistoryFragment() {
        if (getHistoryFragment() != null) {
            // If the fragment already exists, do nothing.

            return;
        }

        final FragmentManager manager = getFragmentManager();
        if (manager == null || manager.isDestroyed() || !prepareForHistory()) {
            // If the history fragment can not be shown, close the draglayout.

            mDragLayout.setClosed();
            return;
        }

        stopActionModeOrContextMenu();
        manager.beginTransaction()
                .replace(R.id.history_frame, new HistoryFragment(), HistoryFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .addToBackStack(HistoryFragment.TAG)
                .commit();

        // When HistoryFragment is visible, hide all descendants of the main Calculator view.
        mMainCalculator.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        // TODO: pass current scroll position of result
    }

    private void displayMessage(String title, String message) {
        AlertDialogFragment.showMessageDialog(this, title, message, null, null /* tag */);
    }

    private void displayFraction() {
        UnifiedReal result = mEvaluator.getResult(Evaluator.MAIN_INDEX);
        displayMessage(getString(R.string.menu_fraction),
                KeyMaps.translateResult(result.toNiceString()));
    }

    // Display full result to currently evaluated precision
    private void displayFull() {
        Resources res = getResources();
        String msg = mResultText.getFullText(true /* withSeparators */) + " ";
        if (mResultText.fullTextIsExact()) {
            msg += res.getString(R.string.exact);
        } else {
            msg += res.getString(R.string.approximate);
        }
        displayMessage(getString(R.string.menu_leading), msg);
    }

    /**
     * Add input characters to the end of the expression.
     * Map them to the appropriate button pushes when possible.  Leftover characters
     * are added to mUnprocessedChars, which is presumed to immediately precede the newly
     * added characters.
     *
     * @param moreChars characters to be added
     * @param explicit  these characters were explicitly typed by the user, not pasted
     */
    private void addChars(String moreChars, boolean explicit) {
        mEvaluator.clearMain();
// Bkav TienNVh : Bỏ vì moreChars đã  cộng mUnprocessedChars
//        if (mUnprocessedChars != null) {
//            moreChars = mUnprocessedChars + moreChars;
//        }
        int current = 0;
        int len = moreChars.length();
        boolean lastWasDigit = false;
        if (mCurrentState == CalculatorState.RESULT && len != 0) {
            // Clear display immediately for incomplete function name.
//            switchToInput(KeyMaps.keyForChar(moreChars.charAt(current)));
            announceClearedForAccessibility();
            mEvaluator.clearMain();
            setState(CalculatorState.INPUT);
        }
        char groupingSeparator = KeyMaps.translateResult(",").charAt(0);
        while (current < len) {
            char c = moreChars.charAt(current);
            if (Character.isSpaceChar(c) || c == groupingSeparator) {
                ++current;
                continue;
            }
            int f = KeyMaps.funForString(moreChars, current);
            int k = -1;
            // Bkav TienNVh :  Check phép tính chức năng . Nếu ko phải thì mới xét đến ký tự  .
            // Nhằm tránh trùng trường hợp hằng sô e và chức năng exp(
            if (f == -1)
                k = KeyMaps.keyForChar(c);

            if (!explicit) {
                int expEnd;
                if (lastWasDigit && current !=
                        (expEnd = Evaluator.exponentEnd(moreChars, current))) {
                    // Process scientific notation with 'E' when pasting, in spite of ambiguity
                    // with base of natural log.
                    // Otherwise the 10^x key is the user's friend.
                    mEvaluator.addExponent(moreChars, current, expEnd);
                    current = expEnd;
                    lastWasDigit = false;
                    continue;
                } else {
                    boolean isDigit = KeyMaps.digVal(k) != KeyMaps.NOT_DIGIT;
                    // Bkav TienNVh :
                    if (current == 0 && (isDigit || k == R.id.dec_point)
                            && mEvaluator.getExpr(Evaluator.MAIN_INDEX).hasTrailingConstant()) {
                        // Refuse to concatenate pasted content to trailing constant.
                        // This makes pasting of calculator results more consistent, whether or
                        // not the old calculator instance is still around.
                        addKeyToExpr(R.id.op_mul);
                    }
                    lastWasDigit = (isDigit || lastWasDigit && k == R.id.dec_point);
                }
            }
            if (k != View.NO_ID) {
                mCurrentButton = findViewById(k);
                if (explicit) {
                    addExplicitKeyToExpr(k);
                } else {
                    addKeyToExpr(k);
                }
                if (Character.isSurrogate(c)) {
                    current += 2;
                } else {
                    ++current;
                }
                continue;
            }

            if (f != View.NO_ID) {
                mCurrentButton = findViewById(f);
                if (explicit) {
                    addExplicitKeyToExpr(f);
                } else {
                    addKeyToExpr(f);
                }
                if (f == R.id.op_sqrt) {
                    // Square root entered as function; don't lose the parenthesis.
                    addKeyToExpr(R.id.lparen);
                }
                current = moreChars.indexOf('(', current) + 1;
                continue;
            }
            // There are characters left, but we can't convert them to button presses.
            mUnprocessedChars = moreChars.substring(current);
            redisplayAfterFormulaChange();
            showOrHideToolbar();
            return;
        }
        mUnprocessedChars = null;
        redisplayAfterFormulaChange();
        showOrHideToolbar();
    }

    private void clearIfNotInputState() {
        if (mCurrentState == CalculatorState.ERROR
                || mCurrentState == CalculatorState.RESULT) {
            setState(CalculatorState.INPUT);
            mEvaluator.clearMain();
        }
    }

    /**
     * Since we only support LTR format, using the RTL comma does not make sense.
     */
    private String getDecimalSeparator() {
        final char defaultSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        final char rtlComma = '\u066b';
        return defaultSeparator == rtlComma ? "," : String.valueOf(defaultSeparator);
    }

    /**
     * Clean up animation for context menu.
     */
    @Override
    public void onContextMenuClosed(Menu menu) {
        stopActionModeOrContextMenu();
    }

    public interface OnDisplayMemoryOperationsListener {
        boolean shouldDisplayMemory();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Bkav TienNVh :  Check vị trí click có nằm trong vùng hiện thị không
        //nếu nămf trong thì cho ẩn mode (paste/ copy)
        if (ev.getY() < mDisplayView.getHeight()) {
            if (mFormulaText != null) mFormulaText.touchOutSide((int) ev.getX(), (int) ev.getY());
            if(mResultText != null) mResultText.touchOutSide((int) ev.getX(), (int) ev.getY());
        }
        return super.dispatchTouchEvent(ev);
    }
}
