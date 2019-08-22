package com.android.calculator2.bkav;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.calculator2.Calculator;
import com.android.calculator2.CalculatorNumericPadLayout;
import com.android.calculator2.CalculatorPadLayout;
import com.android.calculator2.CalculatorPadViewPager;
import com.android.calculator2.bkav.EqualsImageButton.State;
import com.android.calculator2.utils.CheckPermission;
import com.android.calculator2.utils.PermissionUtil;
import com.bkav.calculator2.R;
import com.xlythe.math.Constants;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.view.View.VISIBLE;

/**
 * Created by anhbm on 07/06/2017.
 */

public class BkavCalculator extends Calculator implements PermissionUtil.CallbackCheckPermission {

    private RelativeLayout mRootView;
    private CalculatorPadViewPager mCalculatorPadViewPager;
    private Toolbar mToolbar;
    private LinearLayout mView;
    private static final int SIZE_HISTORY = 10;
    private CalculatorPadLayout mCalculatorPadLayout;
    private BkavHistoryLayout mRelativeLayoutHistory;
    private boolean mHasImmersive;
    private boolean mCached = false;
    private Button mButtonHistory;
    private Bitmap bitmapBlurHis = null;
    private CalculatorNumericPadLayout mCalculatorNumericPadLayoutl;

    private ListView mListView;
    protected Button mClearHistory;
    private BkavCalculatorViewpager mCalculatorViewpager;

    private static final String PREFS_NAME_CACULATOR = "caculator_value";
    private static final String TIME_EXIT_SYSTEM = "timeSystem";
    private static final String VALUE_CACULTOR = "valueCaculator";
    private static final String DECIMAL_SEPARATOR_OLD_SAVE = "DECIMAL_SEPARATOR_OLD_SAVE"; // TrungTH them vao
    private static final String HISTORY_CACULATOR = "historyCaculator";
    private String mHistoryCaculator = "";
    private SharedPreferences mSharedPreferences;
    private BkavHistoryAdapter mAdapter;
    private static final String TAG = "BkavCalculator";
    private List<String> mListHistory = new ArrayList<>();
    private Pattern mPattern = Pattern.compile("\\d*");
    private String mInput = "";

    private BroadcastReceiver mLangReceiver = null;
    private CheckPermission mCheckPermission; // Bien check P

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLangReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Bkav QuanTHb: kill de load lai resource lien quan toi dau thap phan
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mLangReceiver, filter);

        // Bkav AnhBM: khong cho ban phim hien len.
        hideSoftKeyboard();

        super.onCreate(savedInstanceState);

        mRootView = (RelativeLayout) findViewById(R.id.root_layout);
        mCalculatorPadViewPager = (CalculatorPadViewPager) findViewById(R.id.pad_pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mCalculatorPadLayout = (CalculatorPadLayout) findViewById(R.id.pad_advanced);

        mCalculatorNumericPadLayoutl = (CalculatorNumericPadLayout) findViewById(R.id.pad_numeric);
//        Bkav Phongngb
        mListView = (ListView) findViewById(R.id.listview_history);
        mListView.setEmptyView(findViewById(R.id.emptyElement));
        mClearHistory = (Button) findViewById(R.id.clear_history);
        mButtonHistory = (Button) findViewById(R.id.digit_history);
        mRelativeLayoutHistory = (BkavHistoryLayout) findViewById(R.id.relative_layout_history);
        // TrungTH không cần ?
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/helveticaNeueThin.ttf");
//        mClearHistory.setTypeface(typeface);
         mCheckPermission = new CheckPermission(this);
         setBlurBackground();

        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Phongngb set lai man hinh hien thi
            mCalculatorPadViewPager.setCurrentItem(1);
//            Kiem tra coi may co thanh Navigationbar hay khong
            if (hasImmersive(getApplicationContext())) {
                mView = (LinearLayout) findViewById(R.id.view);
                Button myButton = new Button(this);
                myButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getNavigationBarHeight()));

                myButton.setBackgroundColor(getResources().getColor(R.color.colorNumberic));
                mView.addView(myButton);
            }
        } else {
            // Bkav phongngb : Hien thi man hinh khi mann hinh xoay ngang
            mCalculatorViewpager = (BkavCalculatorViewpager) findViewById(R.id.pager);
            mCalculatorViewpager.setCurrentItem(1);
            //Bkav AnhBM: chinh sua giao dien xoay ngang khi co phim dieu huong
            if (hasImmersive(getApplicationContext())) {
                mView = (LinearLayout) findViewById(R.id.view);
                OrientationEventListener mOrientationEventListener = new OrientationEventListener(this) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        if (orientation == ORIENTATION_UNKNOWN) return;

                        int rotation = getWindowManager().getDefaultDisplay().getRotation();
                        switch (rotation) {
                            case Surface.ROTATION_90:
                                mView.setPadding(0, 0, getNavigationBarHeight(), 0);
                                break;
                            case Surface.ROTATION_270:
                                mView.setPadding(getNavigationBarHeight(), 0, 0, 0);
                                break;
                        }
                    }
                };

                if (mOrientationEventListener.canDetectOrientation()) {
                    mOrientationEventListener.enable();
                }
            }
        }

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
                mDeleteButton.setVisibility(VISIBLE);
                mClearButton.setVisibility(View.GONE);
            }
        });

        // Bkav Phongngb Set lai adapter cho listview history
        refreshEvents();

        //Bkav  Phongngb bat su kien khi nhan vao item cua listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String history = mListHistory.get(position);
                String[] array = history.split("=");
                mFormulaEditText.setText(mFormulaEditText.getText() + array[1]);
            }
        });

        // Bkav Phongngb xoa lich su
        mClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHistoryCaculator = "";
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(HISTORY_CACULATOR, mHistoryCaculator);
                editor.commit();
                mListHistory.clear();
                mAdapter = new BkavHistoryAdapter(getApplicationContext(), R.layout.line_history, mListHistory);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                mClearHistory.setVisibility(View.INVISIBLE);

                //Bkav ThanhNgD: Xu li bug khi dang o page lich su( dang co du lieu) ma` xoa' lich xu
                // thi` click se bi click button 123... cua page 1
                if (mCalculatorPadViewPager != null) {
                    TextView emptyElement = (TextView) findViewById(R.id.emptyElement);
                    if (emptyElement != null && emptyElement.getVisibility() == VISIBLE) {
                        mCalculatorPadViewPager.recursivelySetEnabled(mCalculatorNumericPadLayoutl, false);
                    }
                }
                if (mCalculatorViewpager != null) {
                    TextView emptyElement = (TextView) findViewById(R.id.emptyElement);
                    if (emptyElement != null && emptyElement.getVisibility() == VISIBLE) {
                        mCalculatorViewpager.recursivelySetEnabled(mCalculatorNumericPadLayoutl, false);
                    }
                }
            }
        });

//        Bkav phongngb xu li sau 0.1 s thi moi load giao dien xu ly
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = convertViewToBitmap(mRootView);
                BlurManager blur = new BlurManager();
                Bitmap cutBitmapHistory = cutImageToBackgroundHistory(bitmap);

                Bitmap mContainerFilter = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter.eraseColor(getResources().getColor(R.color.colorHistory));
                Bitmap bmHistory = overlayBitmap(mContainerFilter, cutBitmapHistory, 255);
                blur.bitmapScale(0.05f).build(getApplicationContext(), bmHistory);
                bitmapBlurHis = blur.blur(20f);

                BlurManager blurAd = new BlurManager();
                final Bitmap cutBitmapAd = cutImageToBackgroundAdvence(bitmap);
                Bitmap mContainerFilter1 = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter1.eraseColor(getResources().getColor(R.color.colorAdvenced));
                Bitmap bmAd = overlayBitmap(mContainerFilter1, cutBitmapAd, 255);
                blurAd.bitmapScale(0.05f).build(getApplicationContext(), bmAd);
                final Bitmap bitmapBlurAd = blurAd.blur(20f);

                if (mCalculatorPadViewPager != null) {
                    mCalculatorPadViewPager.setOnScrollViewPager(new CalculatorPadViewPager.IScrollViewPager() {
                        @Override
                        public void onScroll(int position, float positionOffset, int positionOffsetPixels) {
                            if (position == 0) {
                                mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis,
                                        position, positionOffset, positionOffsetPixels);
                            } else if (position == 1) {
                                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    if (cutBitmapAd != null) {
                                        mCalculatorPadLayout.setInforScrollViewpager(bitmapBlurAd
                                                , positionOffset, positionOffsetPixels);
                                    }
                                }
                            }
                        }
                    });
                }

                if (mCalculatorViewpager != null) {
                    mCalculatorViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int i, float v, int i1) {
                            mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, i, v, i1);
                        }

                        @Override
                        public void onPageSelected(int i) {
                            //Bkav ThanhNgD: Goi lai onPageSelected() de nhan su kien khi changed page
                            // de xu li bug lich su trong' khi o page 0 van~ click dc button 123... cua page 1
                            mCalculatorViewpager.getmOnPageChangeListener().onPageSelected(i);
                        }

                        @Override
                        public void onPageScrollStateChanged(int i) {
                        }
                    });
                }
            }
        }, 100);
    }

    /**
     * TrungTH muc dich la se check lai khi ngon ngu co su thay doi va doi lai gia tri cho chuan
     *
     * @return
     */
    private String getSavedExpression() {
        String valueCaculator = mSharedPreferences.getString(VALUE_CACULTOR, "");
        if (TextUtils.isEmpty(valueCaculator)) {
            return valueCaculator; // ko phai lam gi
        }
        Constants.rebuildConstants();
        String oldLocale = mSharedPreferences.getString(DECIMAL_SEPARATOR_OLD_SAVE, "");
        String newlocale = getResources().getConfiguration().locale.getLanguage();;
        if (TextUtils.isEmpty(oldLocale) || TextUtils.isEmpty(newlocale) || oldLocale.equals(newlocale)) {
            return valueCaculator; // Ko phai lam gi
        } else {
            // Phai doi lai
            // vi du 1.000,01 => 1,000.01
            NumberFormat parser = NumberFormat.getNumberInstance(new Locale(oldLocale));
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale(newlocale));
            Number rawNumber = null;
            try {
                rawNumber = parser.parse(valueCaculator);
                valueCaculator = formatter.format(rawNumber);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return valueCaculator;
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

    //    Bkav Phongngb get chieu cao cua thanh NavigationBar

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    //   Bkav Phongngb Kiem tra xem may co thanh Navigationbar khong
    public boolean hasImmersive(Context ctx) {
        if (!mCached) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                mHasImmersive = false;
                mCached = true;
                return false;
            }
            Display d = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            mHasImmersive = (realWidth > displayWidth) || (realHeight > displayHeight);
            mCached = true;
        }

        return mHasImmersive;
    }

    // Bkav Phongngb :' setAdpater Listview ,
    private void refreshEvents() {
        mSharedPreferences = getSharedPreferences(PREFS_NAME_CACULATOR, Context.MODE_PRIVATE);
        mHistoryCaculator = mSharedPreferences.getString(HISTORY_CACULATOR, "");
        this.mListHistory.clear();
        if (mHistoryCaculator.trim().length() > 0) {
            mClearHistory.setVisibility(VISIBLE);
            mListHistory.addAll(Arrays.asList(mHistoryCaculator.split(";")));
            if (mListHistory.size() > SIZE_HISTORY) {
                mListHistory = mListHistory.subList(0, SIZE_HISTORY);
                int index = mHistoryCaculator.lastIndexOf(";");
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(HISTORY_CACULATOR, mHistoryCaculator.substring(0, index));
                editor.commit();

            }

            Collections.reverse(mListHistory);
            mAdapter = new BkavHistoryAdapter(this, R.layout.line_history, mListHistory);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            mClearHistory.setVisibility(View.INVISIBLE);
        }
    }


    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //    Blur from view
    private Bitmap getBlurredBackground(View view) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Bitmap bitmap = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();
        BlurManager blurManager = new BlurManager(view, null);
        blurManager.bitmapScale(0.5f).build(this, bitmap);
        bitmap = blurManager.blur(20f);
        return bitmap;

    }

    private WallpaperBlurCompat mWallpaperBlurCompat;
    private Bitmap getBluredBackgroundFromRom() {
        if(mWallpaperBlurCompat == null) {
            mWallpaperBlurCompat = new WallpaperBlurCompat(this, mCheckPermission);
        }
        return mWallpaperBlurCompat.getWallpaperBlur();
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

    //    Bkav Phonggnb luu lich su tinh toan
    private void saveHistory() {
        String expression = mFormulaEditText.getCleanText();
        String result = mResultEditText.getText().toString();
        if (shouldBeSavedInHistory(expression, result)) {
            mHistoryCaculator = (mHistoryCaculator.trim().length() > 0)
                    ? (mHistoryCaculator = expression + "=" + result + ";" + mHistoryCaculator)
                    : (expression + "=" + result);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(HISTORY_CACULATOR, mHistoryCaculator);

            editor.commit();

        }
        refreshEvents();
    }

    @Override
    protected void onEquals() {
        // Bkav Phongngb : save phep toan va set lai adpter cho listview
        saveHistory();
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
                    mEvaluator.evaluate(mFormulaEditText.getCleanText(), this);
                    break;
                case NEXT:
                    mFormulaEditText.next();
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLangReceiver); // QuanTHb: huy broadcast
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bkav PhongNGb : tu dong luu trong vong 15 phut khi thoat ra
        long timeExit = mSharedPreferences.getLong(TIME_EXIT_SYSTEM, 0);

        if (System.currentTimeMillis() - timeExit < DateUtils.MINUTE_IN_MILLIS * 15 /*&& getPreferences()*/) {
            mFormulaEditText.setText(getSavedExpression());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Bkav PhongNGb: luu de khoi phuc lai luc vao lai neu trong vong 15p.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(VALUE_CACULTOR, mFormulaEditText.getText().toString());
        // Luu them gia tri nay de check khi thay doi ngon ngu
        editor.putString(DECIMAL_SEPARATOR_OLD_SAVE, getResources().getConfiguration().locale.getLanguage());
        editor.putLong(TIME_EXIT_SYSTEM, System.currentTimeMillis());
        editor.commit();
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


    // cutbitmap phan nam duoi advence
    private Bitmap cutImageToBackgroundAdvence(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight = displayMetrics.heightPixels;
        int heightChild = mCalculatorPadLayout.getHeight();
        Bitmap cutBitmap = null;
        if (bitmap != null) {
            cutBitmap = Bitmap.createBitmap(bitmap, (int) (bitmap.getWidth() * 0.2),
                    mScreenHeight - heightChild, (int) (bitmap.getWidth() * 0.8), heightChild - 100);
        }
        return cutBitmap;
    }

    //    Cut bitmap phan nam duoi history
    private Bitmap cutImageToBackgroundHistory(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight = displayMetrics.heightPixels;
        int mScreenWidth = displayMetrics.widthPixels;

        int heightChild = mCalculatorPadLayout.getHeight();
        Bitmap cutBitmap = null;
        int orientation = getResources().getConfiguration().orientation;
        if (bitmap != null && orientation == Configuration.ORIENTATION_PORTRAIT) {
            cutBitmap = Bitmap.createBitmap(bitmap, 0, mScreenHeight - heightChild,
                    (int) (mScreenWidth * 0.8), heightChild);

        } else {
            cutBitmap = Bitmap.createBitmap(bitmap, 0, mScreenHeight - heightChild,
                    (int) (mScreenWidth * 0.4), heightChild - 100);
        }
        return cutBitmap;
    }

    //    Bkav Phongngb convert view to bitmap
    private Bitmap convertViewToBitmap(View view) {
        if (view != null)
            view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();
        return bm;
    }

    @Override
    protected void openHistory() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 1) {
                mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
            } else if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 0) {
                mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() + 1);
            }
        } else {
            if (mViewPager == null || mViewPager.getCurrentItem() == 1) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            } else if (mViewPager == null || mViewPager.getCurrentItem() == 0) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            }
        }
    }

    @Override
    protected void performMMinus() {
        if (mFormulaEditText.getText().toString().trim().length() != 0 && !mPattern.matcher(mFormulaEditText.getText().toString().trim()).matches()) {

            mInput += (mInput.equals("")) ? ("(-1*" + "(" + mFormulaEditText.getText().toString() + "))") : "-" + mFormulaEditText.getText().toString();
            setState(CalculatorState.EVALUATE);
            mEvaluator.evaluate(("-1*" + "(" + mFormulaEditText.getText().toString() + ")"), this);

        } else {
            mInput += (mInput.equals("")) ? ("(-1*" + "(" + mFormulaEditText.getText().toString() + "))") : "-" + mFormulaEditText.getText().toString();
        }
    }

    @Override
    protected void performMPlus() {
        if (mFormulaEditText.getText().toString().trim().length() != 0 && !mPattern.matcher(mFormulaEditText.getText().toString().trim()).matches()) {
            mInput += (mInput.equals("")) ? mFormulaEditText.getText().toString() : "+" + mFormulaEditText.getText().toString();
            setState(CalculatorState.EVALUATE);
            mEvaluator.evaluate(mFormulaEditText.getText().toString(), this);

        } else {
            mInput += (mInput.equals("")) ? mFormulaEditText.getText().toString() : "+" + mFormulaEditText.getText().toString();
        }
    }

    @Override
    protected void performMR() {
        setState(CalculatorState.EVALUATE);
        mEvaluator.evaluate(mInput, this);
    }

    @Override
    protected void performMC() {
        mInput = "";
    }

    @Override
    protected void revealViewSetBottom(View revealView, Rect displayRect) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            revealView.setBottom(displayRect.bottom + mPadViewPager.getPaddingTop());
        } else {
            super.revealViewSetBottom(revealView, displayRect);
        }
    }

    /**
     * QuanTHb: các phép tính có biểu thức khác null, đồng thời
     * kết quả của nó cũng khác null và không phải lỗi thì mới lưu vào lịch sử
     */
    private boolean shouldBeSavedInHistory(String expression, String result) {
        // QuanTHb: các phép tính có biểu thức khác null.
        boolean expressionNotNull = !expression.trim().equals("");
        // QuanTHb: nếu kết quả lỗi thì kêt quả là 1 xâu có id là error_syntax.
        // so sánh kêt với xâu đó sẽ biết là phép tính có lỗi hay không?
        String error_syntax = getResources().getString(R.string.error_syntax);
        boolean notNull = !result.trim().equals("");
        boolean notAnError = !result.trim().equals(error_syntax);
        return expressionNotNull && notNull && notAnError;
    }

    @Override
    public void denyPermission(String[] pers) {

    }

    @Override
    public void acceptPermission(String[] pers) {
        if (mCheckPermission !=null && mCheckPermission.canAccessWriteStorage()) {
            setBlurBackground();
        }
    }

    @Override
    public void alwaysDeny(String[] pers) {
        PermissionUtil.showDialogOpenSetting(this, this, pers, CheckPermission.PERMISSION_REQUEST_CODE, R.string.title_request_permission, R.string.message_request_permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CheckPermission.PERMISSION_REQUEST_CODE) {
            PermissionUtil.onActivityPermissionResult(permissions, grantResults, this, this);
        }
    }

    private void setBlurBackground() {
        Bitmap backgroundBitmapFromRom = getBluredBackgroundFromRom();
        mRootView.setBackground(new BitmapDrawable(backgroundBitmapFromRom));
    }
}
