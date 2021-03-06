package com.hold.electrify.util;

import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import com.hold.electrify.R;

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

    public static void showSuccessToast(String msg) {
        View view = ToastUtils.showCustomShort(R.layout.view_toast_success);
        TextView tv = view.findViewById(R.id.textView);
        tv.setText(msg);
    }

    public static void showErrorToast(String msg) {
        View view = ToastUtils.showCustomShort(R.layout.view_toast_error);
        TextView tv = view.findViewById(R.id.textView);
        tv.setText(msg);
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
}
