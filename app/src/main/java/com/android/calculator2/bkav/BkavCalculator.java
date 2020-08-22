package com.android.calculator2.bkav;

import android.app.FragmentManager;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.calculator2.Calculator;
import com.android.calculator2.Evaluator;
import com.android.calculator2.KeyMaps;
import com.bkav.calculator2.R;

import java.util.Arrays;
import java.util.Locale;

public class BkavCalculator extends Calculator implements BkavHistoryAdapter.OnClickItemHistory{
    // Bkav TienNVh :
    private CheckPermission mCheckPermission;

    private WallpaperBlurCompat mWallpaperBlurCompat;

    Bitmap bitmapBlurHis = null;

    private BkavHistoryLayout mRelativeLayoutHistory;

    private BkavAdvancedLayout mCalculatorPadLayout;

    private Button mBtDelHistory;

    // Bkav TienNVh : biến SharedPreferences dùng để lưu phép tính cuối cùng
    // Bkav TienNVh : SharedPreferences lưu theo cấu trúc, và data ít nên ko cần dùng , cách này lấy data dễ hơn
    private SharedPreferences mSharedPreferences;

    private String mSharePreFile = "SaveCalCulator";

    private  static String SHAREDPREFERENCES_FORMULATEXT = "FormulaText";

    private static  String SHAREDPREFERENCES_LANGUAGE = "Language";

    public static String LANGUAGE_VN = "vi_VN";

    public static int POSITION_HISTORY_VIEWPAGER = 0;

    public static int POSITION_NUMBER_VIEWPAGER = 1;

    public static int POSITION_ADVENCE_VIEWPAGER = 2;

    // Bkav TienNVh : Trong TH màn hình dọc thì có 3 tab trong viewpager
    public static int GET_CHILD_VIEWPAGER = 3;

    // Bkav TienNVh : Listener khi paste đoạn text
    private final BkavCalculatorFormula.OnFormulaContextMenuClickListener mOnFormulaContextMenuClickListener =
            new BkavCalculatorFormula.OnFormulaContextMenuClickListener() {
                @Override
                public boolean onPaste(ClipData clip) {
                    final ClipData.Item item = clip.getItemCount() == 0 ? null : clip.getItemAt(0);
                    // Bkav TienNVh : Check đã có dữ liệu để dán chưa
                    if (item == null) {
                        // nothing to paste, bail early...
                        return false;
                    }
                    //Bkav AnhNDd TODO lên google đọc vê việc lấy context trong android, có mấy hàm, và chọn cách nào
                    // Bkav TienNVh :có các cách getApplicationContext(), getContext(), getBaseContext(),this / getActivity()
                    // Bkav TienNVh : không nên dùng cách lấy getApplicationContext() này
                    // vì có thể gây ra ro ri bộ nhớ
                    //Bkav TienNVh: lay du lieu copy
                    String textNew = item.coerceToText(BkavCalculator.this).toString() + "";
                    //Bkav TienNVh: dự liệu phép tính đang tính
                    String formula = mFormulaText.getText().toString();
                    //Bkav TienNVh:Cắt chuỗi tại vị trí con trỏ chia thành 2 phần: 1 phần trước con trỏ, sau con trỏ
                    String formula1 = formula.substring(0, mFormulaText.getSelectionStart());
                    String formula2 = formula.substring(mFormulaText.getSelectionEnd());
                    // Bkav TienNVh: Nối chuỗi đoạn text copy vào vị trí con trỏ
                    String result = formula1 + textNew + formula2;
                    // Bkav TienNVh : Xác định lại ví trí con trỏ sau khi dán
                    mPostionCursorToRight = result.length() - textNew.length() - formula1.length();
                    // Bkav TienNVh : Xoa cac phep tinh hiện tại
                    mEvaluator.clearMain();
                    // Bkav TienNVh : add cac phep tính mới
                    addChars(result, false);
                    // Bkav TienNVh : Hiện thị phép tính , và kết quả lên màn hình
                    redisplayAfterFormulaChange();
                    // Bkav TienNVh : thay doi vi tri con tro
                    changePostionCursor();
                    return true;
                }

                @Override
                public void onMemoryRecall() {
                    onRecallMemory();
                }
            };

    // Bkav TienNVh : Lấy dữ kiệu trong bộ nhớ. để phục vụ tính năng MR và tính năng past
    private  void onRecallMemory(){
        clearIfNotInputState();
        long memoryIndex = mEvaluator.getMemoryIndex();
        if (memoryIndex != 0) {
            mEvaluator.appendExpr(mEvaluator.getMemoryIndex());
            redisplayAfterFormulaChange();
            // Bkav TienNVh : set vi tri 0 đê đưa con trỏ về cuối phép tính
            mPostionCursorToRight = 0;
            // Bkav TienNVh : thay doi vi tri con tro
            changePostionCursor();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBtDelHistory =findViewById(R.id.delHistory);
        mCheckPermission = new CheckPermission(this);
        mCalculatorPadLayout = (BkavAdvancedLayout) findViewById(R.id.pad_advanced);
        // Bkav TienNVh : làm trong suốt Statusbar
        overlapStatusbar();
        // Bkav TienNVh : Set tab hien thị đầu tiên
        if(mPadViewPager!=null)
            mPadViewPager.setCurrentItem(POSITION_NUMBER_VIEWPAGER);
        // Bkav TienNVh :listen  event tab History Open
        mPadViewPager.setCallInvisibleTabHistory(mCallInvisibleTabHistory);
        mSharedPreferences = getSharedPreferences(mSharePreFile, MODE_PRIVATE);
        // Bkav TienNVh : listen Paste memory
        mFormulaText.setOnContextMenuClickListener(mOnFormulaContextMenuClickListener);
        // Bkav TienNVh : Update lại các view ẩn khi click INV
        updateInverseButtons();
        // Bkav TienNVh : thực hiện lấy phép tính cuối cùng để tính khi mở app
        mEvaluator.clearMain();
        String formulaText = mSharedPreferences.getString(SHAREDPREFERENCES_FORMULATEXT, "");
        formulaText = formatStringfllowLanguage(formulaText);
        addChars(formulaText, false);
     //   mCurrentState = CalculatorState.ANIMATE;
        restoreDisplay();
        // Bkav TienNVh : set vi tri 0 đê đưa con trỏ về cuối phép tính
        mPostionCursorToRight = 0;
        // Bkav TienNVh : thay doi vi tri con tro
        changePostionCursor();
    }

    // Bkav TienNVh :
   void updateInverseButtons(){
       int orientation = getResources().getConfiguration().orientation;
       // Bkav TienNVh : Chức năng của button INV là hiện và ẩn các view
       // Bkav TienNVh : Trường hợp có dưới 3 tab => 2 tab (Xoay ngang màn hình )
       // Bkav TienNVh : Trường hợp ở chia đôi màn hình
       if (mPadViewPager.getChildCount() < GET_CHILD_VIEWPAGER ||  orientation== Configuration.ORIENTATION_LANDSCAPE) {
           //Bkav TienNVh :mInvertibleButtons/mInverseButtons là mảng lưu danh sách các view khi click INV thì hiện/ẩn
           // Bkav TienNVh :  Mở rộng số phần tử trong mảng mInvertibleButtons/mInverseButtons
           mInvertibleButtons = Arrays.copyOf(mInvertibleButtons, 8);
           mInverseButtons = Arrays.copyOf(mInverseButtons, 8);
           // Bkav TienNVh :  thêm các view tính năng M vào mảng
           mInvertibleButtons[6] = findViewById(R.id.op_m_sub);
           mInverseButtons[6] = findViewById(R.id.op_m_c);
           mInvertibleButtons[7] = findViewById(R.id.op_m_plus);
           mInverseButtons[7] = findViewById(R.id.op_m_r);
       }
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

    @Override
    public int getMainResId() {
        return R.layout.bkav_activity_calculator_main;
    }

    private void setBlurBackground() {
        Bitmap backgroundBitmapFromRom = getBluredBackgroundFromRom();
        mDragLayout.setBackground(new BitmapDrawable(backgroundBitmapFromRom));
        if (mPadViewPager != null)
            mPadViewPager.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected int setBackgroundDisplayWhenClear() {
        return  R.color.bkav_calculator_primary_color;
    }

    // Bkav TienNVh : Em viết theo code của android gốc
    // Bkav TienNVh : Cập nhật background cho phần display và Statusbar
    @Override
    protected void setState(Calculator.CalculatorState state) {
        super.setState(state);
        if (mCurrentState != CalculatorState.RESULT) {
            mFormulaText.setTextColor(
                    ContextCompat.getColor(this, R.color.bkav_display_formula_text_color));
            mResultText.setTextColor(
                    ContextCompat.getColor(this, R.color.bkav_display_result_text_color));
        }
        if (mCurrentState == CalculatorState.ERROR) {
            final int errorColor =
                    ContextCompat.getColor(this, R.color.calculator_error_color);
            mFormulaText.setTextColor(errorColor);
            mResultText.setTextColor(errorColor);
            // Bkav TienNVh: Làm trong suốt Statusbar. khi trong trạng thái phép tính lỗi
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (mCurrentState != CalculatorState.RESULT) {
            mFormulaText.setTextColor(
                    ContextCompat.getColor(this, R.color.bkav_display_formula_text_color));
            mResultText.setTextColor(
                    ContextCompat.getColor(this, R.color.bkav_display_result_text_color));
            // Bkav TienNVh :Làm trong suốt Statusbar. khi trong trạng thái có kết quả
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    // Bkav TienNVh : lay hinh nen
    private Bitmap getBluredBackgroundFromRom() {
        if (mWallpaperBlurCompat == null) {
            mWallpaperBlurCompat = new WallpaperBlurCompat(this, mCheckPermission);
        }
        return mWallpaperBlurCompat.getWallpaperBlur();
    }

    @Override
    protected void dispatchTouchEventOutsideDisPlay(MotionEvent ev) {
        // Bkav TienNVh :  Check vị trí click có nằm trong vùng hiện thị không
        //nếu nămf trong thì cho ẩn mode (paste)
        if (ev.getY() < mDisplayView.getHeight())
            if (mFormulaText != null) mFormulaText.touchOutSide((int) ev.getX(), (int) ev.getY());
    }

    // Bkav TienNVh: Nhận sự kiện click button INV
    @Override
    protected void onInverseToggled(boolean showInverse) {

        super.onInverseToggled(showInverse);
    }

    @Override
    public void onButtonClick(View view) {
        super.onButtonClick(view);
        final int id = view.getId();
        switch (id) {
            //Bkav  TienNVh: Click cac nut % , ! , pi dong tab Advance
            case R.id.op_fact:
            case R.id.op_pct:
            case R.id.const_pi:
                if (mPadViewPager.getChildCount() > POSITION_NUMBER_VIEWPAGER && mPadViewPager.getCurrentItem() == POSITION_ADVENCE_VIEWPAGER) {
                    mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
                }
                break;
        }
    }

    // Bkav TienNVh: Muc dich bo Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    // Bkav TienNVh : Hiện thị tab history
    @Override
    protected void showHistoryFragment() {
        if (getHistoryFragment() != null) {
            // If the fragment already exists, do nothing.
            return;
        }

        final FragmentManager manager = getFragmentManager();
        if (manager == null || manager.isDestroyed() || !prepareForHistory()) {
            return;
        }
        // Bkav TienNVh : bỏ ActionMode trên display
        stopActionModeOrContextMenu();
        // Bkav TienNVh :check nếu có lịch sự thì hiện button Xoá lịch sử
        if (mEvaluator.getMaxIndex() != 0) {
            mBtDelHistory.setVisibility(View.VISIBLE);
        } else {
            mBtDelHistory.setVisibility(View.GONE);
        }
        // Bkav TienNVh : Hiện thị tab history
        BkavHistoryFragment bkavHistoryFragment = new BkavHistoryFragment();
        manager.beginTransaction()
                .replace(R.id.bkav_history_frame, bkavHistoryFragment, BkavHistoryFragment.TAG)
                .addToBackStack(BkavHistoryFragment.TAG)
                .commit();
    }

    @Override
    protected void setBackgroungViewPager() {
        final ViewTreeObserver vto = mDragLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Bkav TienNVh : Sau khi đã hoàn thành view thì bỏ lắng nghe sự kiện
                mDragLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Bitmap bitmap = convertViewToBitmap(mDragLayout);
                // Bkav TienNVh :  set background cho tab History
                BlurManager blur = new BlurManager();
                Bitmap cutBitmapHistory = cutImageToBackgroundHistory(bitmap);
                // Bkav TienNVh : Tránh một số trường hợp khi chưa kịp lấy kích thước thì đã load giao diện
                Bitmap mContainerFilter = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter.eraseColor(getResources().getColor(R.color.colorHistory));
                Bitmap bmHistory = overlayBitmap(mContainerFilter, cutBitmapHistory, 255);
                blur.bitmapScale(0.05f).build(getApplicationContext(), bmHistory);
                bitmapBlurHis = blur.blur(20f);
                // Bkav TienNVh :  Set background cho tab Advanced
                BlurManager blurAd = new BlurManager();
                final Bitmap cutBitmapAd = cutImageToBackgroundAdvence(bitmap);
                Bitmap bitmapBlurAd = null;
                // Bkav TienNVh : Tránh một số trường hợp khi chưa kịp lấy kích thước thì đã load giao diện
                Bitmap mContainerFilter1 = Bitmap.createBitmap(cutBitmapHistory.getWidth(), cutBitmapHistory.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mContainerFilter1.eraseColor(getResources().getColor(R.color.colorHistory));
                Bitmap bmAd = overlayBitmap(mContainerFilter1, cutBitmapAd, 255);
                blurAd.bitmapScale(0.05f).build(getApplicationContext(), bmAd);
                bitmapBlurAd = blurAd.blur(20f);
                // Bkav TienNVh : sự kiện sang trang
                final Bitmap finalBitmapBlurAd = bitmapBlurAd;
                mRelativeLayoutHistory = (BkavHistoryLayout) findViewById(R.id.relativeLayout_history);
                // Bkav TienNVh : Set background cho History
                mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, (float) 0.0);
                // Bkav TienNVh : set background cho Advance
                final int orientation = getResources().getConfiguration().orientation;
                //Bkav TienNVh:Khi đang mở tab advance thì thay đổi kích thước màn hình=> load lại background
                if (mPadViewPager.getCurrentItem() == POSITION_ADVENCE_VIEWPAGER) {
                    mCalculatorPadLayout.setInforScrollViewpager(finalBitmapBlurAd, (float) 1, mPadViewPager.getWidth());
                }
                mPadViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == POSITION_HISTORY_VIEWPAGER) {
                            // Bkav TienNVh :  position =0 là tab history
                            mRelativeLayoutHistory.setInforScrollViewpager(bitmapBlurHis, positionOffset);
                        } else {
                            if (position == POSITION_NUMBER_VIEWPAGER) {
                                // Bkav TienNVh : Trong truong hop xoay ngang thì sự kiện vuốt sang trái vô hiệu
                                // Bkav TienNVh : Các TH load tab Advance : dọc , chia đôi màn hình
                                if (orientation != Configuration.ORIENTATION_LANDSCAPE || isInMultiWindowMode())
                                    mCalculatorPadLayout.setInforScrollViewpager(finalBitmapBlurAd, positionOffset, positionOffsetPixels);
                            }
                        }
                    }

                    @Override
                    public void onPageSelected(int i) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {  }
                });
            }
        });
    }

    @Override
    public void handleDefaultEvent(int id) {
        switch (id) {
            // Bkav TienNVh :  đóng/ mở tab history trên Viewpager
            case R.id.bt_history:
                mDisplayView.setEnableToolbar(true);
                if (mPadViewPager.getCurrentItem() == POSITION_NUMBER_VIEWPAGER) {
                    mPadViewPager.setCurrentItem(POSITION_HISTORY_VIEWPAGER);
                } else {
                    mPadViewPager.setCurrentItem(POSITION_NUMBER_VIEWPAGER);
                }

                break;
            // Bkav TienNVh :  Xử lý xoá lịch sử
            case R.id.delHistory:
                removeHistoryFragment();
                // Bkav TienNVh :  Xoá date lịch sử
                mEvaluator.clearEverything();
                // Bkav TienNVh :  ẩn button Xoá nhật ký
                mBtDelHistory.setVisibility(View.GONE);
                // Bkav TienNVh :  Hiện thị giao diện lịch sự trống
                findViewById(R.id.emptyhistory).setVisibility(View.VISIBLE);
                break;
            // Bkav TienNVh :  Sự kiện đóng mở tab Advance
            case R.id.bt_more:
                if (mPadViewPager.getChildCount() > POSITION_ADVENCE_VIEWPAGER) {
                    if (mPadViewPager.getCurrentItem() == POSITION_NUMBER_VIEWPAGER) {
                        mPadViewPager.setCurrentItem(POSITION_ADVENCE_VIEWPAGER);
                    } else {
                        mPadViewPager.setCurrentItem(POSITION_NUMBER_VIEWPAGER);
                    }
                }
                break;

            // Bkav TienNVh : replace bo nho
            case R.id.op_m_c:
                if(!mResultText.getText().toString().equals("")){
                    mResultText.onMemoryStore();
                    Toast.makeText(getApplicationContext(),R.string.toast_replace_memory,Toast.LENGTH_LONG).show();
                }

                return;
            // Bkav TienNVh : Tính năng Mr : Lấy dữ liệu trong bộ nhớ tạm
            case R.id.op_m_r:
                onRecallMemory();
                return;
            // Bkav TienNVh : Tính năng m+ : thêm  vào bộ nhớ tam
            case R.id.op_m_plus:
                // Bkav TienNVh : Thuc hien phep tinh
                onEquals();
                // Bkav TienNVh :  Khi nào có phép tinh đó hợp lệ thì mới dùng được tính năng M
                if (mCurrentState == CalculatorState.RESULT|| mCurrentState== CalculatorState.ANIMATE) {
                    mResultText.onMemoryAdd();
                }
                return;
            // Bkav TienNVh :  m- tương tự như m+
            case R.id.op_m_sub:
                onEquals();
                if (mCurrentState == CalculatorState.RESULT|| mCurrentState== CalculatorState.ANIMATE) {
                    mResultText.onMemorySubtract();
                }
                return;

            default:

                cancelIfEvaluating(false);
                // Bkav TienNVh : Lấy nội dung đang hiện thị
                String formulatext = mFormulaText.getText().toString();
                // Bkav TienNVh :  Chuyển từ id sang giá trị
                String newtext = KeyMaps.toString(this, id);
                // Bkav TienNVh :
                addFomulaNew(formulatext, newtext, id);
                break;
        }
    }

    //Bkav AnhNDd TODO Mỗi lần bấm 1 event lại xử lý 1 dống logic, rồi tính toán lại từ đầu ???? Chốt lại cách làm
    // Bkav TienNVh : Hiện tại e chưa nghĩ ra hướng nào hay hơn
    private void addFomulaNew(String formulatext, String newtext, int idFomulaNew) {
        // Bkav TienNVh : postionCursor là biến lưu vị trí con trỏ
        int postionCursor = mFormulaText.getSelectionEnd();
        // Bkav TienNVh : Truowng hop lay ket qua de tiep tuc tinh tiep
        if (mCurrentState == CalculatorState.RESULT) {
            // Bkav TienNVh : Check xem ký tự nhập tiếp theo có phải phép tính ko?
            // Nếu phải thì giữ kết quả để tính tiếp , ngược lại thì xoá kết quả
            if (checkFormulaNext(idFomulaNew)) {
                // Bkav TienNVh : Trong trường hợp lấy kết quả để tính tiếp thì chèn ký tự vừa nhập vào sau kết quả
                formulatext = mTruncatedWholeNumber + newtext;
                postionCursor = formulatext.length() + newtext.length() - 1;
                // Bkav TienNVh :  Sau khi xoá thì reset lại vị trí con trỏ
                mPostionCursorToRight = 0;
            } else {
                formulatext = newtext;
                // Bkav TienNVh :  Sau khi xoá thì reset lại vị trí con trỏ
                postionCursor = 0;
                mPostionCursorToRight = 0;
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
            // Bkav TienNVh : tính năng mở rộng thêm dấu ( sau các sin( , cos(...
            if (KeyMaps.isFunc(idFomulaNew)) {
                addChars(formulaText+")", true);
                mPostionCursorToRight++;
            }else
                addChars(formulaText, true);
            // Bkav TienNVh : Show ket qua neu chuoi ky tu nhap vao la phep tinh
            redisplayAfterFormulaChange();
            // Bkav TienNVh : Thay doi vi tri con tro sau
            changePostionCursor();
        }
    }

    // Bkav TienNVh : Hàm này có chức năng thực hiện phép tính đầu vào là chuỗi
    @Override
    protected void addChars(String moreChars, boolean explicit) {
        mEvaluator.clearMain();
        int current = 0;
        int len = moreChars.length();
        boolean lastWasDigit = false;
        if (mCurrentState == CalculatorState.RESULT && len != 0) {
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

    // Bkav TienNVh : sự kiện xoá ký tự tại vị trí con trỏ
    @Override
    protected void onDeleteBkavCalculator() {
        if (haveUnprocessed()) {
            mUnprocessedChars = mFormulaText.getText() + "";
            mPostionCursorToRight = mUnprocessedChars.length() - mFormulaText.getSelectionEnd();
            if (mFormulaText.getSelectionEnd() >= 1 && mUnprocessedChars.length() > 0)
                mUnprocessedChars = mUnprocessedChars.substring(0, mFormulaText.getSelectionEnd() - 1) + mUnprocessedChars.substring(mFormulaText.getSelectionEnd());
            addChars(mUnprocessedChars, false);
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
                // Bkav TienNVh : tự động xoá dấu ) khi còn lại mỗi mình nó
                if (formulaText.length() == 1 && formulaText.charAt(0) == ')')
                    formulaText.clear();
            } else {
                // Bkav TienNVh :  Xoá những ký tự đã bôi đen
                formulaText.delete(mFormulaText.getSelectionStart(), mFormulaText.getSelectionEnd());
            }
            mEvaluator.clearMain();
            // Bkav TienNVh : Vị trí con trỏ sau khi xoá
            int postionCursor2 = mFormulaText.getSelectionStart();
            // Bkav TienNVh :  add lại phép tính
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

    @Override
    protected void onPause() {
        // Bkav TienNVh : Luu trang thai ngon ngu.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SHAREDPREFERENCES_LANGUAGE, Locale.getDefault().toString());
        editor.putString(SHAREDPREFERENCES_FORMULATEXT, mFormulaText.getText() + "");
        editor.putString(SHAREDPREFERENCES_LANGUAGE, Locale.getDefault().toString() + "");
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onResumeBkavCalculator() {
        setBackgroungViewPager();
        //Bkav TienNVh :Setbackground
        setBlurBackground();
        // Bkav TienNVh :check nếu có lịch sự thì hiện button Xoá lịch sử
        if (mEvaluator.getMaxIndex() != 0) {
            mBtDelHistory.setVisibility(View.VISIBLE);
        } else {
            mBtDelHistory.setVisibility(View.GONE);
        }
    }

    // Bkav TienNVh : Mục địch format lại phép toán khi thay đổi ngôn ngữ
    private String formatStringfllowLanguage(String formulaText) {
        String language = mSharedPreferences.getString(SHAREDPREFERENCES_LANGUAGE, LANGUAGE_VN);
        String languageCurrent = Locale.getDefault().toString();
        String comma = ",";
        String dots = ".";
        if (!language.equals("")) {
            // Bkav TienNVh : Chuyen dau phay cho phu hop voi ngon ngu
            if (!language.equals(languageCurrent)) {
                if (languageCurrent.equals(LANGUAGE_VN)) {
                    formulaText = formulaText.replace(comma, "");
                    formulaText = formulaText.replace(dots, comma);
                } else {
                    formulaText = formulaText.replace(dots, "");
                    formulaText = formulaText.replace(comma, dots);
                }
            }
        }
        return formulaText;
    }

    // Bkav Phongngb convert view to bitmap
    private Bitmap convertViewToBitmap(View view) {
        // if (view != null)
        // view.setDrawingCacheEnabled(true);
        // view.buildDrawingCache();
        // Bitmap bm = view.getDrawingCache();
        // return bm;
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return b;
    }

    //Bkav Phongngb tao ra 1 bitmap b1 tuong tu nhu mot bitmap thu 2
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

    //Bkav TienNVh : Cut bitmap phan nam duoi history
    private Bitmap cutImageToBackgroundHistory(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight = displayMetrics.heightPixels;
        int mScreenWidth = displayMetrics.widthPixels;
        int heightChild = findViewById(R.id.relativeLayout_history).getHeight();
        Bitmap cutBitmap = null;
        int orientation = getResources().getConfiguration().orientation;
        if (bitmap != null && orientation == Configuration.ORIENTATION_PORTRAIT) {
            int y = mScreenHeight - heightChild;
            // Bkav TienNVh :  khi y<0 thì khi chưa có kích thước => ko thể lấy được bitmap
            cutBitmap = Bitmap.createBitmap(bitmap, 0, y + heightChild > bitmap.getHeight() ? bitmap.getHeight() - heightChild : y,
                    (int) (mScreenWidth * 0.8), heightChild);
        } else {
            // Bkav TienNVh : Trường hợp xoay ngang màn hình
            cutBitmap = Bitmap.createBitmap(bitmap, 0, mScreenHeight - heightChild,
                    (int) (mScreenWidth * 0.4), heightChild - 100);
        }
        return cutBitmap;
    }

    // Bkav TienNVh : cutbitmap phan nam duoi advence
    private Bitmap cutImageToBackgroundAdvence(Bitmap bitmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight = displayMetrics.heightPixels;
        int heightChild = findViewById(R.id.pad_advanced).getHeight();
        Bitmap cutBitmap = null;
        // Bkav TienNVh :
        if (bitmap != null && (mScreenHeight - heightChild) > 0 && heightChild > 100) {
            cutBitmap = Bitmap.createBitmap(bitmap, (int) (bitmap.getWidth() * 0.2),
                    mScreenHeight - heightChild, (int) (bitmap.getWidth() * 0.8), heightChild - 100);
        }
        return cutBitmap;
    }

    // Bkav TienNVh :xử lý vuốt back khi có các tab History/Advance thì đóng tab đó
    // Khi ở tab chính thì đóng app
    @Override
    protected boolean onSetPositonViewPagerWhenBackPressed() {
        if (mPadViewPager != null && mPadViewPager.getCurrentItem() != POSITION_NUMBER_VIEWPAGER) {
            // Select the previous pad.
            mPadViewPager.setCurrentItem(POSITION_NUMBER_VIEWPAGER);
            return true;
        }
        return false;
    }

    // Bkav TienNVh :Khi có kết quả sau khi click "="
    // check ký tự tiếp theo có phải là phép tính hoặc các hậu tố (!,%)
    // thì dự lại kết quả để thực hiện phép tính
    // Ngược lại thì xoá kết quả để thực hiện phép tính mới
    private boolean checkFormulaNext(int id) {
        if (KeyMaps.isBinary(id) || KeyMaps.isSuffix(id))
            return true;
        return false;
    }

    // Bkav TienNVh : Hàm này trả về kết quả đầy đủ cho phép tính trước khi format
    @Override
    protected void getTruncatedWholeNumber(String truncatedWholeNumber) {
        // Bkav TienNVh :  truncatedWholeNumber là kết quả hiện thị đầy đủ ở dạng số nguyên
        // Bkav TienNVh : check muc dich la lay ket qua de tiep tuc tinh phep tinh tiep theo
        //Bkav TienNVh : khi tính ra có kết quả lớn (khi xuất hiện ký tưự E) thì lấy truncatedWholeNumber để thực hiện phép tính tiếp theo
        // Nếu không phải phép tính lớn thfi lấy kết quả đang hiện thị trên màn hình để thực hiện phép t
        if (mResultText.getText().toString().contains("E")) {
            // Bkav TienNVh : Check ket qua cho ra so lon thi lay phan nguyen cua ket qua
            mTruncatedWholeNumber = truncatedWholeNumber;
        } else {
            // Bkav TienNVh :  Nguoc lai thi lay ket qua
            mTruncatedWholeNumber = mResultText.getText().toString();
        }
    }

    // Bkav TienNVh :Biến để lưu giá trị kết quả  đầy đủ
    String mTruncatedWholeNumber = null;
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

    // Bkav TienNVh : Listens event yêu cầu đóng /mở tab history
    private final BkavCalculatorPadViewPager.CallInvisibleTabHistory mCallInvisibleTabHistory = new BkavCalculatorPadViewPager.CallInvisibleTabHistory() {
        @Override
        public void onCallVisiblleHistory() {
            findViewById(R.id.emptyhistory).setVisibility(View.GONE);
            showHistoryFragment();
        }

        @Override
        public void onCloseHistory() {
            removeHistoryFragment();
        }
    };

    //Bkav AnhNDd TODO cái này có còn lỗi như trước đã report? Nhảy vị trí linh tinh
    // Bkav TienNVh :  Cái này e đã fix được cái lỗi đã report
    @Override
    public void onClickItemHistory(String result) {
        // Bkav TienNVh : Xử lý lấy dữ liệu trong tab lịch sử để tính
        // vì Lấy dự liệu ra thì theo format tiếng anh
        // Cần phải format lại phù hợp với ngôn ngữ
        String languageCurrent = Locale.getDefault().toString();
        if (languageCurrent.equals(LANGUAGE_VN)) {
            result = result.replace(",", "");
            result = result.replace(".", ",");
        }
        // Bkav TienNVh :  Xử lý chuỗi để thực hiện lại phép tính
        String fomulaOld = mFormulaText.getText().toString();
        String fomulaCrrent = fomulaOld + result;
        // Bkav TienNVh : TH đang có kết quả thì bỏ fomulaOld
        if(mCurrentState == CalculatorState.RESULT)
            fomulaCrrent=result;
        mEvaluator.clearMain();
        // Bkav TienNVh :Thực hiện phép tính
        addChars(fomulaCrrent, false);
        redisplayFormula();
        mPostionCursorToRight = 0;
        changePostionCursor();
    }
}