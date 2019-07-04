package com.iflytek.aiui.demo.chat.repository.ping;


public class HttpTestResult {

    public static final int INVALID_REQUEST = -1;
    public static final int REQUEST_EXCEPTION = -2;

    private int code;
    private boolean noError;
    private long responseTime;
    private boolean isFirstResult;
    private String testUrl;

    private String exception;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isNoError() {
        return noError;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public boolean isFirstResult() {
        return isFirstResult;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public HttpTestResult(boolean isFirstResult, String url, int code, boolean noError, long time){
        this.isFirstResult = isFirstResult;
        this.testUrl = url;
        this.code = code;
        this.noError = noError;
        this.responseTime = time;
    }
}
