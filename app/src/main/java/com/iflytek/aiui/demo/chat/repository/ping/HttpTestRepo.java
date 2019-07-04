package com.iflytek.aiui.demo.chat.repository.ping;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpTestRepo {
    private static final String TEST_HISTORY = "test_history";
    private static final String HISTORY_URLS = "history_urls";

    public static final String[] DEFAULT_TIP_URL = {"http://aiui-ipv6.openspeech.cn","http://aiui.xfyun.cn","http://www.baidu.com"};

    private SharedPreferences mHistoryUrlsPref;

    private MutableLiveData<List<String>> mTipUrls = new MutableLiveData<>();
    private MutableLiveData<HttpTestResult> mTestResult = new MutableLiveData<>();
    private Set<String> mTipUrlsSet= new HashSet<>();

    private long mFirstUrlStartTime;
    private long mSecondUrlStartTime;
    private OkHttpClient mFirstClient;
    private OkHttpClient mSecondClient;

    private boolean mFirstTestStopped = true;
    private boolean mSecondTestStopped = true;

    private String mFirstTestUrl;
    private String mSecondTestUrl;

    @Inject
    public HttpTestRepo(Context context){
        mFirstTestStopped = true;
        mSecondTestStopped = true;

        Set<String> defaultUrlsSet= new HashSet<>();
        defaultUrlsSet.addAll(Arrays.asList(DEFAULT_TIP_URL));
        mHistoryUrlsPref = context.getSharedPreferences(TEST_HISTORY, Context.MODE_PRIVATE);
        mTipUrlsSet = mHistoryUrlsPref.getStringSet(HISTORY_URLS, defaultUrlsSet);
        mTipUrls.postValue(new ArrayList<>(mTipUrlsSet));

        mFirstClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();
        mSecondClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        mFirstRequestThread.start();
        mSecondRequestThread.start();
    }

    public void addUrlToHistory(String url){
        if(!mTipUrlsSet.contains(url) && !TextUtils.isEmpty(url)){
            mTipUrlsSet.add(url);
            SharedPreferences.Editor editor = mHistoryUrlsPref.edit();
            editor.clear();
            editor.putStringSet(HISTORY_URLS, mTipUrlsSet);
            editor.apply();

            mTipUrls.postValue(new ArrayList<>(mTipUrlsSet));
        }
    }

    private Callback mFirstRspCb = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            mFirstTestStopped = true;
            HttpTestResult result = new HttpTestResult(true, mFirstTestUrl, HttpTestResult.REQUEST_EXCEPTION, false, 0);
            result.setException(e.getMessage());
            mTestResult.postValue(result);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            int code = response.code();
            long rspTime = System.currentTimeMillis() - mFirstUrlStartTime;
            HttpTestResult result = new HttpTestResult(true, mFirstTestUrl, code, true, rspTime);
            mTestResult.postValue(result);
        }
    };

    private Callback mSecondRspCb = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            mSecondTestStopped = true;
            HttpTestResult result = new HttpTestResult(false, mSecondTestUrl, HttpTestResult.REQUEST_EXCEPTION, false, 0);
            result.setException(e.getMessage());
            mTestResult.postValue(result);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            int code = response.code();
            long rspTime = System.currentTimeMillis() - mSecondUrlStartTime;
            HttpTestResult result = new HttpTestResult(false, mSecondTestUrl, code, true, rspTime);
            mTestResult.postValue(result);
        }
    };

    public void startHttpTest(boolean start, boolean isFirst, String url){
        if(start) {
            if(isFirst) {
                mFirstTestUrl = url;
                mFirstTestStopped = false;
            } else {
                mSecondTestUrl = url;
                mSecondTestStopped = false;
            }
        }
    }

    public void stopAllTest(){
        mFirstTestStopped = true;
        mSecondTestStopped = true;
    }

    public void revertToDftHistory(){
        Set<String> defaultUrlsSet= new HashSet<>();
        defaultUrlsSet.addAll(Arrays.asList(DEFAULT_TIP_URL));
        SharedPreferences.Editor editor = mHistoryUrlsPref.edit();
        editor.putStringSet(HISTORY_URLS, defaultUrlsSet);
        editor.apply();
    }

    public LiveData<List<String>> getTipUrls() {
        return mTipUrls;
    }

    public LiveData<HttpTestResult> getTestResult(){
        return mTestResult;
    }

    private Thread mFirstRequestThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if(!mFirstTestStopped && mFirstTestUrl != null){
                        Request request = new Request.Builder().url(mFirstTestUrl).build();
                        mFirstUrlStartTime = System.currentTimeMillis();
                        mFirstClient.newCall(request).enqueue(mFirstRspCb);
                    }
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }catch (IllegalArgumentException e){
                    mFirstTestStopped = true;
                    HttpTestResult result = new HttpTestResult(true, mFirstTestUrl, HttpTestResult.INVALID_REQUEST, false, 0);
                    result.setException(e.getMessage());
                    mTestResult.postValue(result);
                    e.printStackTrace();
                }
            }
        }
    });

    private Thread mSecondRequestThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if(!mSecondTestStopped && mSecondTestUrl != null) {
                        Request request = new Request.Builder().url(mSecondTestUrl).build();
                        mSecondUrlStartTime = System.currentTimeMillis();
                        mSecondClient.newCall(request).enqueue(mSecondRspCb);
                    }
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }catch (IllegalArgumentException e){
                    mSecondTestStopped = true;
                    HttpTestResult result = new HttpTestResult(false, mSecondTestUrl, HttpTestResult.INVALID_REQUEST, false, 0);
                    result.setException(e.getMessage());
                    mTestResult.postValue(result);
                    e.printStackTrace();
                }
            }

        }
    });

    public boolean getFirstTestStoped(){
        return mFirstTestStopped;
    }

    public boolean getSecondTestStoped(){
        return mSecondTestStopped;
    }

}
