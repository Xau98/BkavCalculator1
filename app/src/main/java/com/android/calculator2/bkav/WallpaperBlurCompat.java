package com.android.calculator2.bkav;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by duclq on 07/06/2017.
 */

public class WallpaperBlurCompat {

    private static final int WIDTH_SCACE_BLUR = 54;
    private static final int HEIGHT_SCARE_BLUR = 96;
    private static final float BLUR_RADIUS = 25f;
    private ScriptIntrinsicBlur mScriptIntrinsicBlur;
    private RenderScript mRenderScript;
    private WallpaperManager mWallpaperManager;
    private Context mContext;
    private CheckPermission mCheckPermission; // Bien check P


    public WallpaperBlurCompat(Activity context, CheckPermission checkPermission){
        mWallpaperManager = WallpaperManager.getInstance(context);
        mCheckPermission = checkPermission;
        initBlur(context);
        mContext = context;
    }

    /**
     * Bkav DucLQ ham nay invoke dong de lay ra Bitmap da blur san trong bphone
     * @return
     */
    public Bitmap getWallpaperBlur(){
        Bitmap wallpaperBlur = null;

        try {
            Method method = mWallpaperManager.getClass().getMethod("getBitmapWallpaperBlur");
            method.setAccessible(true);
            wallpaperBlur = (Bitmap) method.invoke(mWallpaperManager);
        } catch (NoSuchMethodException e) {
            return blurWallpaper();
        } catch (InvocationTargetException e) {
            return blurWallpaper();
        } catch (IllegalAccessException e) {
            return blurWallpaper();
        }
        if (wallpaperBlur == null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mCheckPermission.hasPermission((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE) ) {
                mCheckPermission.checkPremission(CheckPermission.LIST_PERMS);
            } else {
                wallpaperBlur = blurWallpaper();
            }
        }
        return wallpaperBlur;
    }

    private Bitmap blurWallpaper() {
        Drawable wallpaper = mWallpaperManager.getDrawable();
        Bitmap bitmapWallpaper = Bitmap.createBitmap(wallpaper.getIntrinsicWidth(),
                wallpaper.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWallpaper);
        wallpaper.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        wallpaper.draw(canvas);
        if (bitmapWallpaper == null) {
            return null;
        }
        Bitmap wallpaperBlur = Blur(bitmapWallpaper);
        return wallpaperBlur;
    }

    /**Bkav DucLQ
     * @param bitmap
     * @return
     */
    private Bitmap Blur(Bitmap bitmap){
        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH_SCACE_BLUR, HEIGHT_SCARE_BLUR, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        Allocation tmpIn = Allocation.createFromBitmap(mRenderScript, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(mRenderScript, outputBitmap);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mScriptIntrinsicBlur.setRadius(BLUR_RADIUS);
            mScriptIntrinsicBlur.setInput(tmpIn);
            mScriptIntrinsicBlur.forEach(tmpOut);
        }

        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private void initBlur(Context context){
        mRenderScript = RenderScript.create(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mScriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript,
                    Element.U8_4(mRenderScript));
        }
    }
}
