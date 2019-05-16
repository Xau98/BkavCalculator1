package com.android.calculator2.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by trungth on 12/10/2017.
 */

public class CheckPermission {
    public static final int PERMISSION_REQUEST_CODE = 1111;

    public static final String[] LIST_PERMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] LIST_LOCATIONS_PERMS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    private Activity mActivity;

    public CheckPermission(Activity activity) {
        mActivity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean canAccessWriteStorage() {
        return (hasPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean canAccessPhoneState() {
        return (hasPermission(mActivity, Manifest.permission.READ_PHONE_STATE));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasPermission(Activity activity, String perm) {
        return (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(perm));
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean canAccessFineLocation() {
        return (CheckPermission.hasPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                CheckPermission.hasPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPremission(String[] listsPermission) {
        PermissionUtil.checkPermission(listsPermission, PERMISSION_REQUEST_CODE, mActivity, (PermissionUtil.CallbackCheckPermission) mActivity);
    }
}
