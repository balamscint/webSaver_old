package com.wpdf.application;

/**
 * Created by balam_000 on 8/28/2015.
 **/

import android.app.Application;
import android.os.Build;

import com.wpdf.websaver.BuildConfig;

public class WebSaverApplication extends Application {

    public static final int iSdkVersion = Build.VERSION.SDK_INT;
    public static final int iAppVersion = BuildConfig.VERSION_CODE;
    public static final boolean isLive = false;

    public static final String strPDFExt = ".pdf";

}
