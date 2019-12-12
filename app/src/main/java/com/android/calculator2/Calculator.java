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

package com.android.calculator2;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Property;
import android.view.ActionMode;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.calculator2.CalculatorFormula.OnTextSizeChangeListener;

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

import static com.android.calculator2.CalculatorFormula.OnFormulaContextMenuClickListener;

public class Calculator extends Activity
        implements OnTextSizeChangeListener, OnLongClickListener,
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
            AlertDialogFragment.showMessageDialog(Calculator.this, title, message,
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

    private final OnFormulaContextMenuClickListener mOnFormulaContextMenuClickListener =
            new OnFormulaContextMenuClickListener() {
                @Override
                public boolean onPaste(ClipData clip) {
                    final ClipData.Item item = clip.getItemCount() == 0 ? null : clip.getItemAt(0);
                    if (item == null) {
                        // nothing to paste, bail early...
                        return false;
                    }

                    // Check if the item is a previously copied result, otherwise paste as raw text.
                    final Uri uri = item.getUri();
                    if (uri != null && mEvaluator.isLastSaved(uri)) {
                        clearIfNotInputState();
                        mEvaluator.appendExpr(mEvaluator.getSavedIndex());
                        redisplayAfterFormulaChange();
                    } else {
                        addChars(item.coerceToText(Calculator.this).toString(), false);
                    }
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

    private CalculatorDisplay mDisplayView;
    private TextView mModeView;
    private Toolbar mToolbar;
    private CalculatorFormula mFormulaText;
    private CalculatorResult mResultText;
    private HorizontalScrollView mFormulaContainer;
    private DragLayout mDragLayout;

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

    private View mCurrentButton;
    private Animator mCurrentAnimator;
    // Bkav TienNVh :
    private SharedPreferences mSharedPreferences;
    private String mSharePreFile = "SaveCalCulator";
    // Bkav TienNVh : Bien luu tam thoi khi dung m+ , m-
    private String mINPUT = "";

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

        mMainCalculator = findViewById(R.id.main_calculator);
        mDisplayView = (CalculatorDisplay) findViewById(R.id.display);
        mModeView = (TextView) findViewById(R.id.mode);
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
        //Bkav TienNVh : Ko cho click xuyen len lich su
        findViewById(R.id.relativeLayout_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Bkav TienNVh : Khi xoay hide button more , set height cho button Xoa
        int orientation = getResources().getConfiguration().orientation;
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.bt_more).setVisibility(View.GONE);
            findViewById(R.id.delHistory).getLayoutParams().height = 150;
        }

        // Bkav TienNVh : Set font number
        setFontNumber();

        // Bkav TienNVh : Lam trong suot status bar
        mToolbar = (Toolbar) findViewById(R.id.toolbarapp);
        makeStatusBarTransparent(mToolbar);

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

    // Bkav TienNVh : Load tab History
    public void onRefeshSaveHistory() {
        String savehistory = mSharedPreferences.getString("SaveHistory", "");
        if (!savehistory.equals("")) {
            String sliptSaveHistory[] = savehistory.split(";");
            mListHistory = new ArrayList<String>(Arrays.asList(sliptSaveHistory));
            mHistoryAdapter = new BkavHistoryAdapter(getApplication(), mListHistory);
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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPadViewPager != null) {
                    mPadViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int i, float v, int i1) {
//                    mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, i, v, i1);
                        }

                        @Override
                        public void onPageSelected(int i) {
                            //Bkav ThanhNgD: Goi lai onPageSelected() de nhan su kien khi changed page
                            // de xu li bug lich su trong' khi o page 0 van~ click dc button 123... cua page 1
                            mPadViewPager.getmOnPageChangeListener().onPageSelected(i);
                        }

                        @Override
                        public void onPageScrollStateChanged(int i) {
                        }
                    });
                }
            }
        }, 1000);
    }

    // Bkav TienNVh : Chuyen ky tu sang ID
    public void addExplicitStringToExpr(String formulatext) {
        if (!formulatext.equals("")) {
                for (int i = 0; i < formulatext.length(); ) {
                    // Bkav TienNVh : Lay tung ky tu trong chuoi
                    char splitFormulatext = formulatext.charAt(i);
                    // Bkav TienNVh : xử lý dấu ngăn cách và dấu phẩy
                    if (splitFormulatext == ',' || splitFormulatext == '.') {
                        // Bkav TienNVh : Trường hợp Ngôn ngữ tiếng việt dấu ',' tương ứng với dấu phẩy và dấu ngăn cách là '.'
                        // Bkav TienNVh : Trường hợp ngôn ngữ khác tiếng viết '.' tương ứng với dấu phẩy và dấu ngăn cách là dấu ','
                        if ((splitFormulatext == ',' && Locale.getDefault().toString().equals("vi_VN")) ||
                                (splitFormulatext == '.' && !Locale.getDefault().toString().equals("vi_VN"))) {
                            // Bkav TienNVh : Trường hợp dấu phẩy
                            addExplicitKeyToExpr(R.id.dec_point);
                            i++;
                            continue;
                        } else {
                            // Bkav TienNVh : Trường hợp dấu ngăn cách
                            i++;
                            continue;
                        }
                    }
                    // Bkav TienNVh :  Kiểm tra ký tự có phải số không ?
                    if (KeyMaps.keyForDigVal((int) splitFormulatext) == View.NO_ID) {
                        // Bkav TienNVh : Kiểm tra ký tự có phải phép tính không?
                        if (KeyMaps.keyForChar(splitFormulatext) != View.NO_ID) {
                            // Bkav TienNVh :  trường hợp trùng  e với exp() , để phân biệt thì phải dựa vào ký tự p
                            if (i < formulatext.length() - 2 && (byte) formulatext.charAt(i + 2) == 112) {// Bkav TienNVh : 'p'=112
                                // Bkav TienNVh : Trường hợp ký tự là exp(
                                addExplicitKeyToExpr(R.id.fun_exp);
                                // Bkav TienNVh : tăng lên 4 vì độ dài chuỗi exp( là 4
                                i = i + 4;
                                continue;
                            } else {
                                // Bkav TienNVh :
                                if (i < formulatext.length() - 2 && splitFormulatext == 'e' && (byte) formulatext.charAt(i + 2) != 112) {// Bkav TienNVh : 'p'=112
                                    // Bkav TienNVh : truong hop co ky tu chen o giua cum
                                    int postion = mFormulaText.getSelectionStart();
                                    mPostionCursorToRight = formulatext.length() - postion - 1;
                                    // Bkav TienNVh : Neu mUnprocessedChars!=null va !empty => Phep tinh ay loi
                                    mUnprocessedChars = formulatext;
                                    mEvaluator.clearMain();
                                    mFormulaText.setText(mUnprocessedChars);
                                    return;
                                } else {
                                    addExplicitKeyToExpr(KeyMaps.keyForChar(splitFormulatext));
                                    i++;
                                    continue;
                                }
                            }
                        } else {
                            if ((byte) splitFormulatext == 26) {
                                addExplicitKeyToExpr(R.id.op_sqrt);
                                i++;
                                continue;
                            } else {
                                switch (splitFormulatext) {
                                    case 's':
                                        if (formulatext.length() > 2 && (byte) formulatext.charAt(i + 3) != 40) { // Bkav TienNVh :  '('= 40
                                            if ((byte) formulatext.charAt(i + 3) == 123) {// Bkav TienNVh :  '-'=123
                                                addExplicitKeyToExpr(R.id.fun_arcsin);
                                                i = i + 6;
                                            } else {
                                                // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
                                                insertCharacters(formulatext);
                                                return;
                                            }
                                        } else {
                                            // Bkav TienNVh : sln(in(
                                            if ((byte) formulatext.charAt(i + 1) == 105) { // Bkav TienNVh : 'i'=105
                                                addExplicitKeyToExpr(R.id.fun_sin);
                                                i = i + 4;
                                                // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                                mUnprocessedChars = null;
                                            } else {
                                                // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
                                                insertCharacters(formulatext);
                                                return;
                                            }
                                        }
                                        continue;
                                    case 'c':
                                        if ((byte) formulatext.charAt(i + 3) != 40) {
                                            if ((byte) formulatext.charAt(i + 3) == 94) {// Bkav TienNVh :  '-'=94
                                                addExplicitKeyToExpr(R.id.fun_arccos);
                                                i = i + 6;
                                                // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                                mUnprocessedChars = null;
                                            } else {
                                                // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
                                                insertCharacters(formulatext);
                                                return;
                                            }
                                        } else {
                                            addExplicitKeyToExpr(R.id.fun_cos);
                                            i = i + 4;
                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                            mUnprocessedChars = null;
                                        }
                                        continue;
                                    case 't':
                                        if ((byte) formulatext.charAt(i + 3) != 40) {
                                            if ((byte) formulatext.charAt(i + 3) == 94) {// Bkav TienNVh :  '-'=94
                                                addExplicitKeyToExpr(R.id.fun_arctan);
                                                i = i + 6;
                                                // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                                mUnprocessedChars = null;
                                            } else {
                                                // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
                                                insertCharacters(formulatext);
                                                return;
                                            }
                                        } else {
                                            addExplicitKeyToExpr(R.id.fun_tan);
                                            i = i + 4;
                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                            mUnprocessedChars = null;
                                        }
                                        continue;
                                    case 'l':
                                        if ((byte) formulatext.charAt(i + 2) == 103) { // Bkav TienNVh : 'g'=103
                                            addExplicitKeyToExpr(R.id.fun_log);
                                            i = i + 4;
                                            // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                            mUnprocessedChars = null;
                                        } else {
                                            if ((byte) formulatext.charAt(i + 2) == 40) {// Bkav TienNVh : '('=40
                                                addExplicitKeyToExpr(R.id.fun_ln);
                                                i = i + 3;
                                                // Bkav TienNVh : set lai trường hợp hết lỗi cú pháp
                                                mUnprocessedChars = null;
                                            } else {
                                                // Bkav TienNVh :  Trường hợp chèn ký tự vào giữa cụm
                                                insertCharacters(formulatext);
                                                return;
                                            }
                                        }
                                        continue;

                                    default:
                                        if ((byte) formulatext.charAt(i) == -78) {
                                            addExplicitKeyToExpr(R.id.op_sqr);
                                            i++;
                                            continue;
                                        }
                                }
                            }
                        }
                    } else {
                        addExplicitKeyToExpr(KeyMaps.keyForDigVal((int) splitFormulatext));
                        i++;
                    }
                }
            }
        }

    // Bkav TienNVh : truong hop co ky tu chen o giua cum
    public void insertCharacters(String formulatext) {
        // Bkav TienNVh : Biến để lưu trữ độ dài của ký tự mới nhập vào
        int lengthTextNew = 1;
        // Bkav TienNVh : Vi tri con tro truoc khi chen
        int postion = mFormulaText.getSelectionStart();
        // Bkav TienNVh :  Lay do dai cua kys tu
        switch (formulatext.charAt(postion)) {
            case 'c':
            case 't':
            case 's':
            case 'e':
                if ((byte) formulatext.charAt(postion + 3) == 123) {
                    lengthTextNew = 6;
                } else
                    lengthTextNew = 4;
                break;
            case 'l':
                if ((byte) formulatext.charAt(postion + 1) == 110) {
                    lengthTextNew = 3;
                } else
                    lengthTextNew = 4;
                break;
            default:
                lengthTextNew = 1;
                break;
        }
        // Bkav TienNVh : Vi tri con tro sau khi chen ky tu , tinh tu ben phai sang
        mPostionCursorToRight = formulatext.length() - postion - lengthTextNew;
        // Bkav TienNVh : Hien thong bao loi
        Log.d("TienNVh", "insertCharacters: " + formulatext);
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

    /**
     * Bkav TienNVh: ham thuc hien lam trong suot status bar
     */
    private void makeStatusBarTransparent(Toolbar toolbar) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // AnhBM: cho toolbar padding 1 doan dung bang statusbar height,
        // viec setpadding ko dung view co the lam hong animation tab
        // Retrieve the AppCompact Toolbar
        //
        View view = findViewById(R.id.toolbarapp);
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

    // Bkav TienNVh : Xu ly dau phay theo ngon ngu
    @Override
    protected void onResume() {
        super.onResume();
        mEvaluator.clearMain();
        String language = mSharedPreferences.getString("Language", "vi_VN");
        String formulaText = mSharedPreferences.getString("FormulaText", "");
        String languageCurrent = Locale.getDefault().toString();
        if (!language.equals("")) {
            // Bkav TienNVh : Chuyen dau phay cho phu hop voi ngon ngu
            if (!language.equals(languageCurrent)) {
                if (languageCurrent.equals("vi_VN")) {
                    formulaText = formulaText.replace(",", "");
                    formulaText = formulaText.replace(".", ",");
                } else {
                    formulaText = formulaText.replace(".", "");
                    formulaText = formulaText.replace(",", ".");
                }
            }
        }
        addExplicitStringToExpr(formulaText);
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
                getWindow().setStatusBarColor(errorColor);
            } else if (mCurrentState != CalculatorState.RESULT) {
                mFormulaText.setTextColor(
                        ContextCompat.getColor(this, R.color.display_formula_text_color));
                mResultText.setTextColor(
                        ContextCompat.getColor(this, R.color.display_result_text_color));
                getWindow().setStatusBarColor(
                        ContextCompat.getColor(this, R.color.calculator_statusbar_color));
            }

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
        editor.putString("FormulaText", mFormulaText.getText() + "");
        editor.putString("Language", Locale.getDefault().toString() + "");
        editor.apply();

    }

    @Override
    protected void onDestroy() {
        mEvaluator.delete();
        mDragLayout.removeDragCallback(this);
        super.onDestroy();
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
        if (KeyMaps.isBinary(button_id) || KeyMaps.isSuffix(button_id)) {
            mEvaluator.collapse(mEvaluator.getMaxIndex() /* Most recent history entry */);
        } else {
            announceClearedForAccessibility();
            mEvaluator.clearMain();
        }
        setState(CalculatorState.INPUT);
    }

    // Add the given button id to input expression.
    // If appropriate, clear the expression before doing so.
    private void addKeyToExpr(int id) {
        if (mCurrentState == CalculatorState.ERROR) {
            setState(CalculatorState.INPUT);
        } else if (mCurrentState == CalculatorState.RESULT) {
            switchToInput(id);
        }
        if (!mEvaluator.append(id)) {
            // TODO: Some user visible feedback?
        }
    }

    /**
     * Add the given button id to input expression, assuming it was explicitly
     * typed/touched.
     * We perform slightly more aggressive correction than in pasted expressions.
     */
    private void addExplicitKeyToExpr(int id) {
        if (mCurrentState == CalculatorState.INPUT && id == R.id.op_sub) {
            mEvaluator.getExpr(Evaluator.MAIN_INDEX).removeTrailingAdditiveOperators();
        }
        addKeyToExpr(id);
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

    public void onButtonClick(View view) {
        // Any animation is ended before we get here.
        mCurrentButton = view;
        int postionCursor = mFormulaText.getSelectionStart(); // vi tri con tro
        stopActionModeOrContextMenu();
        // See onKey above for the rationale behind some of the behavior below:
        cancelUnrequested();
        int orientation = getResources().getConfiguration().orientation;
        final int id = view.getId();
        switch (id) {
            // Bkav TienNVh : Tab tinh nang mo rong
            case R.id.bt_more:
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 1) {
                        mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() + 1);
                    } else if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 2) {
                        mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                    }
                }
                break;
            // Bkav TienNVh : Delete History
            case R.id.delHistory:
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("SaveHistory", "");
                editor.apply();
                onRefeshSaveHistory();
                break;
            // Bkav TienNVh : Tab History
            case R.id.bt_history:
                if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 1) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                } else if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 0) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() + 1);
                }
                break;
            case R.id.eq:
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
                mINPUT = "";
                return;
            case R.id.op_m_r:
                mEvaluator.clearMain();
                if (!mINPUT.equals("")) {
                    for (int i = 0; i < mINPUT.length(); i++) {
                        char splitFormulatext = mINPUT.charAt(i);
                        if (KeyMaps.keyForDigVal((int) splitFormulatext) == View.NO_ID) {
                            if (KeyMaps.keyForChar(splitFormulatext) != View.NO_ID) {
                                addExplicitKeyToExpr(KeyMaps.keyForChar(splitFormulatext));
                            }
                        } else {
                            addExplicitKeyToExpr(KeyMaps.keyForDigVal((int) splitFormulatext));
                        }
                    }
                }
                onEquals();
                redisplayAfterFormulaChange();
                // Bkav TienNVh : Dịch chuyển con trỏ cuối cùng
                mFormulaText.setSelection(mFormulaText.getText().length());
                return;

            // Bkav TienNVh : Tính năng m+
            case R.id.op_m_plus:
                // Bkav TienNVh : Thuc hien phep tinh
                onEquals();
                // Bkav TienNVh : mINPUT là biến lưu tạm thời
                // Bkav TienNVh : Kiem tra truong hop biến mINPUT đã có nội dung chưa
                if (mINPUT.equals("")) {
                    if (mCurrentState == CalculatorState.ANIMATE || mCurrentState == CalculatorState.RESULT) {
                        // Bkav TienNVh : Trường hợp các ký tự nhập vào là phép tính thì lấy kết quả để lưu
                        mINPUT = mTruncatedWholeNumber + "";
                    } else {
                        // Bkav TienNVh : Trường họp cào là 1 số thì lấy các ký tự nhập vào để lưu
                        mINPUT = mFormulaText.getText() + "";
                    }
                } else {
                    // Bkav TienNVh : Trường hơp : biến mINPUT đã có nội dung thì lấy nội dung của mINPUT cũ nối với nội dung mới
                    if (mCurrentState == CalculatorState.ANIMATE || mCurrentState == CalculatorState.RESULT) {
                        mINPUT = mINPUT + "+" + mTruncatedWholeNumber;
                    } else {
                        mINPUT = mINPUT + "+" + mFormulaText.getText();
                    }
                }
                return;
            // Bkav TienNVh :  m- tương tự như m+
            case R.id.op_m_sub:
                onEquals();
                // Bkav TienNVh : Tạo biến cục bộ để luu noi dung mới
                String input = "";
                // Bkav TienNVh : Truong hop cac ky tu nhap vao khong phai la phep tinh
                if (mCurrentState == CalculatorState.ANIMATE || mCurrentState == CalculatorState.RESULT) {
                    input = "" + mTruncatedWholeNumber;
                } else {
                    input = "" + mFormulaText.getText();
                }

                // Bkav TienNVh : Xet truong hop bien mINPUT da co noi dung chua
                if (mINPUT.equals("")) {

                    if (input.length() > 0) {
                        // Bkav TienNVh : kiem tra input >0 de co the lay duoc input.charAt(0)
                        if (input.charAt(0) == KeyMaps.MINUS_SIGN) {
                            // Bkav TienNVh : Neu nhap vao la -a thi luu vao a (vi --a = a)
                            mINPUT = input.substring(1);
                        } else {
                            // Bkav TienNVh : Nguoc lai neu nhap vao la a thi luu vao la -a
                            mINPUT = Character.toString(KeyMaps.MINUS_SIGN) + input;
                        }
                    }
                } else {
                    // Bkav TienNVh : Xet truong hop : --a= a
                    if (input.charAt(0) == KeyMaps.MINUS_SIGN) {
                        // Bkav TienNVh : Chuyen dau - thanh dau + . Bang cach cắt bỏ dấu trừ thêm vào đó là dáu cộng  (--a = +a)
                        mINPUT = mINPUT + "+" + input.substring(1);
                    } else {
                        mINPUT = mINPUT + Character.toString(KeyMaps.MINUS_SIGN) + input;
                    }
                }
                return;
            //Bkav  TienNVh: Click cac nut % , ! , pi dong item mo rong
            case R.id.const_pi:
            case R.id.op_fact:
            case R.id.op_pct:
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mPadViewPager != null) {
                        mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                    }
                }
            default:
                cancelIfEvaluating(false);
                if (haveUnprocessed()) {
                    // Bkav TienNVh : Truong hop Phep tinh khong hop le
                    Log.d("TienNVh", "onButtonClick: loi " + mUnprocessedChars);
                    // For consistency, append as uninterpreted characters.
                    // This may actually be useful for a left parenthesis.
                    // addChars(KeyMaps.toString(this, id), true);
                    String formulatext = mFormulaText.getText().toString();
                    String newtext = KeyMaps.toString(this, id);

                    // Bkav TienNVh : Truowng hop lay ket qua de tiep tuc tinh tiep
                    if (mCurrentState == CalculatorState.RESULT) {
                        // Bkav TienNVh :
                        formulatext = mTruncatedWholeNumber;
                        postionCursor = formulatext.length() + newtext.length() - 1;
                    }

                    int lengthold = formulatext.length();// do dai cua chuoi
                    if (lengthold >= postionCursor || mCurrentState == CalculatorState.RESULT) {
                        mPostionCursorToRight = lengthold - postionCursor;// Bkav TienNVh : Vi tri con tro tinh tu ben phai sang
                        // Bkav TienNVh : Chen chuoi moi nhap vao vi tri con tro
                        String slipt1 = formulatext.substring(0, postionCursor);
                        String slipt2 = formulatext.substring(postionCursor, lengthold);
                        String formulaText = slipt1 + newtext + slipt2;
                        // Bkav TienNVh : Xoa tat ca cac phep tinh cu
                        mEvaluator.clearMain();
                        // Bkav TienNVh : Them Cac phep tinh sau khi sua
                        addExplicitStringToExpr(formulaText);
                        // Bkav TienNVh : Show ket qua neu chuoi ky tu nhap vao la phep tinh
                        redisplayAfterFormulaChange();
                        // Bkav TienNVh : Thay doi vi tri con tro sau
                        changePostionCursor();

                    }

                } else {
                    //addExplicitKeyToExpr(id);
                    String formulatext = mFormulaText.getText().toString();
                    String newtext = KeyMaps.toString(this, id);
                    // Bkav TienNVh : Truowng hop lay ket qua de tiep tuc tinh tiep
                    if (mCurrentState == CalculatorState.RESULT) {
                        // Bkav TienNVh : Trường hợp đã click '*'
                        // Bkav TienNVh :
                        formulatext = mTruncatedWholeNumber;
                        // Bkav TienNVh : set lai vi tri con tro tính từ bên phải sang
                        postionCursor = formulatext.length() + newtext.length() - 1;
                    }

                    int lengthold = formulatext.length();// do dai cua chuoi
                    if (lengthold >= postionCursor || mCurrentState == CalculatorState.RESULT) {
                        mPostionCursorToRight = lengthold - postionCursor;// Bkav TienNVh : Vi tri con tro tinh tu ben phai sang
                        // Bkav TienNVh : Chen chuoi moi nhap vao vi tri con tro
                        String slipt1 = formulatext.substring(0, postionCursor);
                        String slipt2 = formulatext.substring(postionCursor, lengthold);
                        String formulaText = slipt1 + newtext + slipt2;
                        // Bkav TienNVh : Xoa tat ca cac phep tinh cu
                        mEvaluator.clearMain();
                        // Bkav TienNVh : Them Cac phep tinh sau khi sua
                        addExplicitStringToExpr(formulaText);
                        // Bkav TienNVh : Show ket qua neu chuoi ky tu nhap vao la phep tinh
                        redisplayAfterFormulaChange();
                        // Bkav TienNVh : Thay doi vi tri con tro sau
                        changePostionCursor();
                    }

                }
                break;
        }
        showOrHideToolbar();
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
            // Bkav TienNVh : Trường hợp click "=" cho ra kết quả
            mTruncatedWholeNumber = truncatedWholeNumber;
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
                    String savehistoryold = mSharedPreferences.getString("SaveHistory", "");
                    if (savehistoryold.contains(textNew)) {
                        savehistoryold = savehistoryold.replace(textNew, "");
                    }

                    // Bkav TienNVh : Luu ket qua vao lich su
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("SaveHistory", savehistoryold + textNew);
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
        editor.putString("Language", Locale.getDefault().toString());
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
            // Bkav TienNVh : Trường hợp "Biểu thức không hợp lệ"
            //mUnprocessedChars = mUnprocessedChars.substring(mFormulaText.getSelectionEnd(), mUnprocessedChars.length() - 1);
            final Editable formulaText = mFormulaText.getEditableText();
            int postionCursor = mFormulaText.getSelectionEnd();
            mPostionCursorToRight = formulaText.length() - postionCursor;
            final int formulaLength = formulaText.length() - mPostionCursorToRight;
            String locale = Locale.getDefault().toString();
            char comma;
            // Bkav TienNVh : Xet truong hop:  dau phay tuy thuoc vao ngon ngu
            if (locale.equals("vi_VN")) {
                comma = '.';
            } else {
                comma = ',';
            }
            if (formulaLength > 0) {
                if (formulaText.charAt(formulaLength - 1) == comma) { // Bkav TienNVh : Truong hop xoa dau ngan cach
                    formulaText.delete(formulaLength - 2, formulaLength);
                } else {
                    if (formulaText.charAt(formulaLength - 1) == '(') {
                        if (formulaLength >= 3) {// Bkav TienNVh : Kiem tra dieu kien
                            if ((byte) formulaText.charAt(formulaLength - 3) == 123) { //Bkav TienNVh TH: arccos() , arcsin() ,arctan()
                                formulaText.delete(formulaLength - 6, formulaLength);
                            } else {
                                if ((byte) formulaText.charAt(formulaLength - 3) == 'l') { //Bkav TienNVh TH: ln()
                                    formulaText.delete(formulaLength - 3, formulaLength);
                                } else {// Bkav TienNVh :  sin() , cos() , tan(), exp(), log()
                                    switch (formulaText.charAt(formulaLength - 4)) {
                                        case 'c':
                                        case 's':
                                        case 't':
                                        case 'e':
                                        case 'l':
                                            formulaText.delete(formulaLength - 4, formulaLength);
                                            break;
                                        default:// Bkav TienNVh :  Truong hop : '('
                                            formulaText.delete(formulaLength - 1, formulaLength);
                                            break;
                                    }
                                }
                            }
                        } else {
                            // Bkav TienNVh : Xoá 1 ký tự
                            formulaText.delete(formulaLength - 1, formulaLength);
                        }
                    } else
                        formulaText.delete(formulaLength - 1, formulaLength);
                }
                // Bkav TienNVh :
                mUnprocessedChars = formulaText.toString();
                mEvaluator.clearMain();
                addExplicitStringToExpr(formulaText.toString());
            }

            int postionCursor2 = mFormulaText.getSelectionStart();
            mPostionCursorToRight = mFormulaText.length() - postionCursor2;
        } else {
            final Editable formulaText = mFormulaText.getEditableText();
            // Bkav TienNVh : lấy vị trí con trỏ tính từ bên trái
            int postionCursor = mFormulaText.getSelectionEnd();
            // Bkav TienNVh : tính vị trí con trỏ tính từ bên phải sang
            // Bkav TienNVh : Mục đích chỉ thay đổi các ký tự trước con trỏ , còn sau con trỏ thì dự nguyên
            mPostionCursorToRight = formulaText.length() - postionCursor;
            final int formulaLength = formulaText.length() - mPostionCursorToRight;
            // Bkav TienNVh :  Lấy ngôn ngữ hiên đang dùng
            String locale = Locale.getDefault().toString();
            char comma;
            // Bkav TienNVh : Xet truong hop:  dau phay tuy thuoc vao ngon ngu
            if (locale.equals("vi_VN")) {
                // Bkav TienNVh :  Trường hợp ngôn ngữ tiếng việt thì dùng "." làm dấu phân cách giua 3 số nguyên
                // Bkav TienNVh : Mục đích là nếu xoá dấu phân cach thì xoá cái số trước nó
                comma = '.';
            } else {
                // Bkav TienNVh : Trường hop ngược lại thì dùng dấu ',' để phân cách
                comma = ',';
            }

            if (formulaLength > 0) {
                if (formulaText.charAt(formulaLength - 1) == comma) {
                    // Bkav TienNVh : Truong hop xoa dau ngan cach
                    formulaText.delete(formulaLength - 2, formulaLength);
                } else {
                    if (formulaText.charAt(formulaLength - 1) == '(') {
                        if ((byte) formulaText.charAt(formulaLength - 3) == 123) { //Bkav TienNVh TH: arccos() , arcsin() ,arctan()
                            formulaText.delete(formulaLength - 6, formulaLength);
                        } else {
                            if ((byte) formulaText.charAt(formulaLength - 3) == 'l') { //Bkav TienNVh TH: ln()
                                formulaText.delete(formulaLength - 3, formulaLength);
                            } else {// Bkav TienNVh :  sin() , cos() , tan(), exp(), log()
                                switch (formulaText.charAt(formulaLength - 4)) {
                                    case 'c':
                                    case 's':
                                    case 't':
                                    case 'e':
                                    case 'l':
                                        formulaText.delete(formulaLength - 4, formulaLength);
                                        break;
                                    default:// Bkav TienNVh :  Truong hop : '('
                                        formulaText.delete(formulaLength - 1, formulaLength);
                                        break;
                                }
                            }
                        }
                    } else
                        formulaText.delete(formulaLength - 1, formulaLength);
                }
                mEvaluator.clearMain();
                addExplicitStringToExpr(formulaText.toString());
            }
            // Bkav TienNVh : Vị trí con trỏ sau khi xoá
            int postionCursor2 = mFormulaText.getSelectionStart();
            mPostionCursorToRight = mFormulaText.length() - postionCursor2;
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

        // Make reveal cover the display and status bar.
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
        } else if (mCurrentState == CalculatorState.INIT
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
        if (mUnprocessedChars != null) {
            moreChars = mUnprocessedChars + moreChars;
        }
        int current = 0;
        int len = moreChars.length();
        boolean lastWasDigit = false;
        if (mCurrentState == CalculatorState.RESULT && len != 0) {
            // Clear display immediately for incomplete function name.
            switchToInput(KeyMaps.keyForChar(moreChars.charAt(current)));
        }
        char groupingSeparator = KeyMaps.translateResult(",").charAt(0);
        while (current < len) {
            char c = moreChars.charAt(current);
            if (Character.isSpaceChar(c) || c == groupingSeparator) {
                ++current;
                continue;
            }
            int k = KeyMaps.keyForChar(c);
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
            int f = KeyMaps.funForString(moreChars, current);
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
}
