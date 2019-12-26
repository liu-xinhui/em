package com.powershare.etm.ui.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;


public abstract class BaseActivity extends AppCompatActivity {

    private QMUITipDialog loadingDialog;

    protected abstract View initContentView();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏文字白色
        QMUIStatusBarHelper.setStatusBarDarkMode(this);
        setContentView(initContentView());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        createViewModel();
        onMounted(savedInstanceState);
        onMounted();
    }

    //此处创建viewModel
    protected void createViewModel() {
    }

    protected void onMounted() {
    }

    protected void onMounted(Bundle savedInstanceState) {
    }

    public void go(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] outLocation = new int[2];
            view.getLocationOnScreen(outLocation);
            float x = ev.getRawX() + view.getLeft() - outLocation[0];
            float y = ev.getRawY() + view.getTop() - outLocation[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) {
                QMUIKeyboardHelper.hideKeyboard(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new QMUITipDialog.Builder(this)
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("加载中")
                    .create();
        }
        loadingDialog.show();
    }

    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}