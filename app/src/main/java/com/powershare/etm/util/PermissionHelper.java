package com.powershare.etm.util;

import android.app.Activity;
import android.app.AlertDialog;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.PermissionUtils.OnRationaleListener.ShouldRequest;

import java.util.List;

public class PermissionHelper {

    public static void showRationaleDialog(final ShouldRequest shouldRequest) {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage("必须赋予相应权限才可使用")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> shouldRequest.again(true))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> shouldRequest.again(false))
                .setCancelable(false)
                .create()
                .show();

    }

    public static void showOpenAppSettingDialog() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage("必须赋予相应权限才可使用，请在设置中开启")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> PermissionUtils.launchAppDetailsSettings())
                .setCancelable(false)
                .create()
                .show();
    }

    public static void getPermission(Callback callback, @PermissionConstants.Permission final String... permissions) {
        PermissionUtils.permission(permissions)
                .rationale(PermissionHelper::showRationaleDialog)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        callback.onGranted();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        if (!permissionsDeniedForever.isEmpty()) {
                            PermissionHelper.showOpenAppSettingDialog();
                        }
                    }
                })
                .request();
    }

    public static void getLocPermission(Callback callback) {
        getPermission(callback, PermissionConstants.LOCATION);
    }

    public interface Callback {
        void onGranted();
    }
}
