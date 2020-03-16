package com.android.calculator2.bkav;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Created by trungth on 12/10/2017.
 */

public class CheckPermission {
    public static final int PERMISSION_REQUEST_CODE = 1111;

    public static final String[] LIST_PERMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Activity mActivity;

    public CheckPermission(Activity activity) {
        mActivity = activity;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean canAccessWriteStorage() {
        return (hasPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE));
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasPermission(Activity activity, String perm) {
        return (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(perm));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPremission(String[] listsPermission) {
        PermissionUtil.checkPermission(listsPermission, PERMISSION_REQUEST_CODE, mActivity, (PermissionUtil.CallbackCheckPermission) mActivity);
    }

}
