package com.haleydu.cimoc.utils;

import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

public class HintUtils {

    public static void showSnackbar(View layout, String msg) {
        if (layout != null && layout.isShown()) {
            Snackbar.make(layout, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

}
