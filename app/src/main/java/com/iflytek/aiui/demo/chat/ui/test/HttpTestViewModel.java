package com.iflytek.aiui.demo.chat.ui.test;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.repository.ping.HttpTestResult;
import com.iflytek.aiui.demo.chat.repository.ping.HttpTestRepo;

import java.util.List;

import javax.inject.Inject;

/**
 * 文件描述：
 * Created by ybyu3 on 2018/6/14.
 */

public class HttpTestViewModel extends ViewModel{

    private HttpTestRepo mHttpTestRepo;

    @Inject
    public HttpTestViewModel(HttpTestRepo httpTestRepo){
        mHttpTestRepo = httpTestRepo;
    }

    public LiveData<List<String>> getTipUrls(){
        return mHttpTestRepo.getTipUrls();
    }

    private void addUrlToHistory(String url){
        mHttpTestRepo.addUrlToHistory(url);
    }

    public void startHttpTest(boolean start, String firstUrl, String secondUrl){
        mHttpTestRepo.startHttpTest(start, true, firstUrl);
        addUrlToHistory(firstUrl);

        if(!TextUtils.isEmpty(secondUrl)){
            mHttpTestRepo.startHttpTest(start, false, secondUrl);
            addUrlToHistory(secondUrl);
        }
    }

    public void stopAllTest(){
        mHttpTestRepo.stopAllTest();
    }

    public LiveData<HttpTestResult> getTestResult(){
        return mHttpTestRepo.getTestResult();
    }

    public boolean getFirstTestStoped(){
        return mHttpTestRepo.getFirstTestStoped();
    }

    public boolean getSecondTestStoped(){
        return mHttpTestRepo.getSecondTestStoped();
    }

}
