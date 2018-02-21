package com.rwe.platform.rest;

public class RestResponse<T> {

    boolean success = true;

    String message = "";

    T data;


    public RestResponse() {
    }

    public RestResponse(T data) {
        this.data = data;
    }

    public RestResponse(boolean success, String message) {
        this.success = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
