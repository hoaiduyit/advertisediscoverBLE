package com.hoaiduy.blebeacon.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

import com.hoaiduy.blebeacon.R;

/**
 * Created by hoaiduy2503 on 8/1/2017.
 */

public class DialogUtils {

    public static boolean isContextValid(final Context context) {
        if (context == null || ((context instanceof Activity) && ((Activity) context).isFinishing()))
            return false;
        else
            return true;
    }

    public static ProgressDialog getLoadingProgressDialog(Context context) {
        if (!isContextValid(context)) {
            return null;
        }
        ProgressDialog dialog = new ProgressDialog(context);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog);
        return dialog;
    }
}
