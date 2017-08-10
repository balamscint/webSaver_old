package com.wpdf.application;

/**
 * Created by balam_000 on 8/28/2015.
 **/

import android.app.Application;
import android.os.Build;

public class WebSaverApplication extends Application {

    public static final int iSdkVersion = Build.VERSION.SDK_INT;
    //public static final int iAppVersion = BuildConfig.VERSION_CODE;
    public static final String strOs = "android";
    public static final int CACHE_EXPIRE = 1;//In Minutes
    public static final boolean isLive = true;

}
