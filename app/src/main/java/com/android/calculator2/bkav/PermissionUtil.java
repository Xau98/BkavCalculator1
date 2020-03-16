package com.android.calculator2.bkav;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by anhdt on 17/11/2017.
 *
 */

public class PermissionUtil {

    private static final String PREF_FIRST_RUN_CHECK_PRE = "PREF_FIRST_RUN_CHECK_PRE";

    public static final String APPLICATION_ID = "com.bkav.calculator2";

    private static final int DELTA_REQUEST_OPEN_SETTINGS = 101;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkPermission(String[] pers, int request_code, Activity context, CallbackCheckPermission callback) {
        boolean denyContact = false;
        ArrayList<String> permissions = new ArrayList<>();
        for (String permission : pers) {
            int value = ContextCompat.checkSelfPermission(context, permission);
            if (value != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                        permission)|| isFirstTimeAskingPermission(context, permission)) {
                    permissions.add(permission);
                } else {
                    denyContact = true;
                }
            }
        }
        if (permissions.size() > 0) {
            if (denyContact) {
                callback.alwaysDeny(pers);
            } else {
                String[] arrPer = new String[permissions.size()];
                int i = 0;
                for (String permission : permissions) {
                    arrPer[i] = permission;
                    i++;
                }
                context.requestPermissions(arrPer, request_code);
            }
        } else {
            if (denyContact) {
                callback.alwaysDeny(pers);
            } else {
                callback.acceptPermission(pers);
            }
        }
    }

    /**
     * Anhdts callback lai cho activity la dang hien dialog xin cap quyen
     */



    public static void onActivityPermissionResult(String[] permissions, int[] grantResults, CallbackCheckPermission callbackCheckPermission, Activity activity) {
        int i = 0;
        boolean isDenied = false;
        boolean isAlwaysDenied = false;
        for (String permission : permissions) {
            if (isFirstTimeAskingPermission(activity, permission)) {
                firstTimeAskingPermission(activity, permission, false);
            }

            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permission)) {
                    isAlwaysDenied = true;
                }
                isDenied = true;
            }
            i++;
        }
        if (isDenied) {
            if (isAlwaysDenied) {
                callbackCheckPermission.alwaysDeny(permissions);
            } else {
                callbackCheckPermission.denyPermission(permissions);
            }
        } else {
            callbackCheckPermission.acceptPermission(permissions);
        }
    }

    public interface CallbackCheckPermission {
        void denyPermission(String[] pers);

        void acceptPermission(String[] pers);

        void alwaysDeny(String[] pers);

    }


    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences(PREF_FIRST_RUN_CHECK_PRE, Context.MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    public static boolean isFirstTimeAskingPermission(Context context, String permission) {
        return context.getSharedPreferences(PREF_FIRST_RUN_CHECK_PRE, Context.MODE_PRIVATE).getBoolean(permission, true);
    }
}
