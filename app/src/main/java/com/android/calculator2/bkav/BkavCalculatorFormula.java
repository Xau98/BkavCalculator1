package com.android.calculator2.bkav;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.RequiresApi;
import com.android.calculator2.CalculatorFormula;

public class BkavCalculatorFormula extends CalculatorFormula {
    public BkavCalculatorFormula(Context context) {
        super(context);
    }

    public BkavCalculatorFormula(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //Bkav TienNVh: Có thuộc tính ẩn bàn phím nhưng cũng đồng thời ẩn cusor. mục đích của mình là vừa ẩn bàn phím mà vẫn hiện cusor
    @Override
    protected void hideSoftInputOnFocus() {
        //Bkav tiennvh khong cho show ban phim
        setShowSoftInputOnFocus(false);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

    }

    // Bkav TienNVh : Ân con trỏ  ở vị trí cuối
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (getSelectionEnd() == length()) {
            setCursorVisible(false);
        } else
            setCursorVisible(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void hideActionModeOrigin() {
            // Bkav TienNVh : ẩn hiện paste , select all ... khi click vào dưới cursor
            setCustomInsertionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });

            // Bkav TienNVh : Custom Action mode để chỉ cho  hiện mỗi past
            setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mCheckActionMode= true;
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // Bkav TienNVh :  Lọc cho hiện thị mỗi paste
                    for (int i = 0; i < menu.size();) {
                        MenuItem item = menu.getItem(i);
                        if (item.getItemId() != android.R.id.paste) menu.removeItem(item.getItemId());
                        else i++;
                    }
                    menu.close();
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mCheckActionMode= false;
                }
            });
    }

    //===========================BKAV==========================
    private Rect mContainer = new Rect();
    // Bkav TienNVh : Biến để check Action mode của hệ thống  có đã tồn tại chưa
    boolean mCheckActionMode = false;

    //Bkav AnhNDd: kiểm tra xem toạ độ x,y có nàm ngoài view hay không
    public void touchOutSide(int x, int y) {
        // Bkav TienNVh : Trường hợp toạ độ (0,0,0,0) thì lấy lại vị trí
        if (mContainer.isEmpty()) {
            // Bkav TienNVh : set lại toạ độ
            getGlobalVisibleRect(mContainer);
        }
        // Bkav TienNVh : Biến Check Click  ngoài toạ độ edittext
        boolean isTouchOutSide = !mContainer.contains(x, y);
        // Bkav TienNVh : Check Click ngoài toạ độ edittext và Action mode đã hiện thi chưa
        if (isTouchOutSide && mActionMode != null) {
            // Bkav TienNVh : Tiến hành đóng Actionmode
            mActionMode.finish();
            mActionMode = null;
        }
        // Bkav TienNVh : Check Action mode hệ thống hiện thị hay không ?
        if (isTouchOutSide && mCheckActionMode) {
            // Bkav TienNVh : dịch chuyển con trỏ về sau đoạn text để đóng action  mode hệ thống
            setSelection(getSelectionEnd());
        }
    }

}
