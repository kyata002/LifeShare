package com.example.doan.view.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doan.R;


public class DialogChange{
    //    public int currentState = -1;
    protected static Dialog mVolumeDialog;
    protected static ProgressBar mDialogVolumeProgressBar;
    protected static TextView mDialogVolumeTextView;
    protected static ImageView mDialogVolumeImageView;
    protected static Dialog mBrightnessDialog;
    protected static ProgressBar mDialogBrightnessProgressBar;
    protected static TextView mDialogBrightnessTextView;
    public static Context context;

    public static void showVolumeDialog(int volumePercent, Context ct) {
        context=ct;
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(context).inflate(R.layout.dialog_volume, null);
            mDialogVolumeImageView = localView.findViewById(R.id.volume_image_tip);
            mDialogVolumeTextView = localView.findViewById(R.id.tv_volume);
            mDialogVolumeProgressBar = localView.findViewById(R.id.volume_progressbar);
            mVolumeDialog = createDialogWithView(localView);
        }
        if (!mVolumeDialog.isShowing()) {
            try{
                mVolumeDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (volumePercent <= 0) {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.vp_close_volume);
        } else {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.vp_add_volume);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        mDialogVolumeTextView.setText(volumePercent + "%");
        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    public static void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
            mVolumeDialog = null;
        }
    }

    public static Dialog createDialogWithView(View localView) {
        Dialog dialog = new Dialog(context, R.style.jz_style_dialog_progress);
        dialog.setContentView(localView);
        Window window = dialog.getWindow();
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(localLayoutParams);
        return dialog;
    }
    public static void showBrightnessDialog(int brightnessPercent,Context ct) {
        context = ct;
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(context).inflate(R.layout.dialog_brightness, null);
            mDialogBrightnessTextView = localView.findViewById(R.id.tv_brightness);
            mDialogBrightnessProgressBar = localView.findViewById(R.id.brightness_progressbar);
            mBrightnessDialog = createDialogWithView(localView);
        }
        if (!mBrightnessDialog.isShowing()) {
            try{
                mBrightnessDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        mDialogBrightnessTextView.setText(brightnessPercent + "%");
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
//        onCLickUiToggleToClear();
    }

    public static void dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
            mBrightnessDialog = null;
        }
    }

}
