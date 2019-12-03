package com.powershare.etm.bean;


import com.powershare.etm.util.MyException;

import lombok.Data;

@Data
public class ApiResult<T> {
    //这三个为接口返回
    private Integer status;
    private String msg;
    private T data;

    //这两个表示liveData状态
    public Throwable error;
    public RequestState requestState;

    public boolean isApiSuccess() {
        return status != null && status == 200;
    }

    public enum RequestState {
        START,
        SUCCESS,
        ERROR
    }

    public ApiResult<T> success() {
        this.requestState = RequestState.SUCCESS;
        return this;
    }

    public ApiResult<T> error() {
        this.requestState = RequestState.ERROR;
        this.error = new MyException(msg);
        return this;
    }

    public static <T> ApiResult<T> start() {
        ApiResult<T> resource = new ApiResult<>();
        resource.requestState = RequestState.START;
        return resource;
    }

    public static <T> ApiResult<T> success(T t) {
        ApiResult<T> resource = new ApiResult<>();
        resource.requestState = RequestState.SUCCESS;
        resource.data = t;
        return resource;
    }

    public static <T> ApiResult<T> error(Throwable t) {
        ApiResult<T> resource = new ApiResult<>();
        resource.requestState = RequestState.ERROR;
        resource.error = t;
        return resource;
    }

    public static <T> ApiResult<T> error(String errorMsg) {
        return error(new MyException(errorMsg));
    }
}
