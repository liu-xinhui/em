package com.powershare.etm.util;


import androidx.lifecycle.Observer;

import com.powershare.etm.bean.ApiResult;

public abstract class MyObserver<T> implements Observer<ApiResult<T>> {

    public void onStart() {
    }

    public void onError(Throwable e) {
        CommonUtil.showErrorToast(CommonUtil.getExceptionMsg(e));
    }

    public abstract void onSuccess(T t);

    public void onFinish() {
    }

    @Override
    public void onChanged(ApiResult<T> t) {
        if (t == null) {
            onError(new MyException("未知错误"));
            return;
        }
        switch (t.requestState) {
            case START:
                onStart();
                break;
            case SUCCESS:
                try {
                    onSuccess(t.getData());
                    onFinish();
                } catch (Exception e) {
                    onError(e);
                }
                break;
            case ERROR:
                onError(t.error);
                onFinish();
                break;
        }
    }

}
