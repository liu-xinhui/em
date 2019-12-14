package com.powershare.etm.util;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import com.powershare.etm.R;
import com.powershare.etm.http.ApiManager;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

public class CommonUtil {

    public static String getExceptionMsg(Throwable e) {
        String msg;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            msg = "服务器错误:" + httpException.code();
        } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
            msg = "无法连接到服务器, 请检查您的网络";
        } else if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
            msg = "连接超时, 请检查您的网络";
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof MalformedJsonException || e instanceof IllegalStateException) {
            msg = "数据格式化出错";
        } else if (e instanceof MyException) {
            msg = e.getMessage();
        } else {
            msg = "未知错误";
        }
        LogUtils.e(e, msg);
        return msg;
    }

    public static void showErrorToast(String msg) {
        try {
            TSnackbar snackBar = TSnackbar.make(ActivityUtils.getTopActivity().findViewById(android.R.id.content), msg, TSnackbar.LENGTH_LONG);
            View snackBarView = snackBar.getView();
            snackBarView.setBackgroundColor(ColorUtils.getColor(R.color.colorAccent));
            TextView textView = snackBarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(ColorUtils.string2Int("#2E2E2E"));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            snackBar.show();
        } catch (Exception e) {
            ToastUtils.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
            View view = ToastUtils.showCustomShort(R.layout.view_toast);
            TextView tv = view.findViewById(R.id.textView);
            tv.setText(msg);
        }
    }


    /**
     * 类似String.format
     */
    public static String format(String str, Object... values) {
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            Object item = values[i];
            str = str.replace("{" + i + "}", item.toString());
        }
        return str;
    }

    public static String getImageUrl(String carModel, String imageId) {
        return CommonUtil.format("{0}carModel/photo?carModelCode={1}&photoId={2}", ApiManager.BASE_URL, carModel, imageId);
    }
}
