package com.currency.rates.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.currency.rates.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ProgressDialogUtil {
    private ProgressDialogUtil(){}

    public static AlertDialog createProgressDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View progressView = inflater.inflate(R.layout.progress_view, null);
        builder.setView(progressView);
        return builder.create();
    }

    public static void showProgressDialog(AlertDialog progressDialog) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public static void hideProgressDialog(AlertDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
