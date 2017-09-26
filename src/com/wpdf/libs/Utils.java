package com.wpdf.libs;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wpdf.application.WebSaverApplication;
import com.wpdf.websaver.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 10/08/17.
 */

public class Utils {

    public static void Log(String tag, String message) {
        if (!WebSaverApplication.isLive) {
            Log.e(tag, message);
        }
    }

    public static File getFile(Context context) {

        File root = Environment.getExternalStorageDirectory();

        File dir = new File(root.getAbsolutePath() + File.separator +
                context.getString(R.string.app_name));

        if (!dir.exists() || !root.isDirectory()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static void toast(int type, int duration, String message, Context context) {

        int color = context.getResources().getColor(R.color.colorPrimary);

        if (type == 2)
            color = context.getResources().getColor(R.color.colorLightGrey);

        if (type == 3)
            color = context.getResources().getColor(R.color.colorRed);

        try {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup)
                    ((Activity) context).findViewById(R.id.toast_layout_root));

            TextView text = layout.findViewById(R.id.text);
            text.setText(message);
            text.setTextColor(color);

            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

            if (duration == 2)
                toast.setDuration(Toast.LENGTH_LONG);
            else
                toast.setDuration(Toast.LENGTH_SHORT);

            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checking for all possible internet providers
     **/
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.
                CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        boolean mExternalStorageAvailable = false;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {


            // We can read and write the media
            mExternalStorageAvailable = true;
        } else { // We can only read the media
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }

        return mExternalStorageAvailable;
    }

    /*public static void setTaskBarColored(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window w = context.getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //status bar height
            int statusBarHeight = Utilities.getStatusBarHeight(context);

            View view = new View(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(view);
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryTaskBar));
        }
    }

    public static int getStatusBarHeight(Activity context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    */

    public static List<String> splitLongText(String string, int length) {

        List<String> strings = new ArrayList<>();
        // int index = 0;

        //
        Pattern p = Pattern.compile("\\G\\s*(.{1," + length + "})(?=\\s|$)", Pattern.DOTALL);
        Matcher m = p.matcher(string);
        while (m.find())
            strings.add(m.group(1));
        //

      /*  while (index < string.length()) {
            strings.add(string.substring(index, Math.min(index + length, string.length())));
            index += length;
        }*/
        return strings;
    }

}
