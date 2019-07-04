package com.iflytek.aiui.demo.chat.ui.test;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.databinding.HttpTestFragmentBinding;
import com.iflytek.aiui.demo.chat.repository.ping.HttpTestChartManager;
import com.iflytek.aiui.demo.chat.repository.ping.HttpTestData;
import com.iflytek.aiui.demo.chat.repository.ping.HttpTestResult;
import com.iflytek.aiui.demo.chat.repository.ping.HttpTestRepo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.android.support.AndroidSupportInjection;

/**
 * 文件描述：
 * Created by ybyu3 on 2018/6/13.
 */

public class HttpTestFragement extends Fragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    HttpTestViewModel mHttpTestViewModel;
    private HttpTestFragmentBinding mHttpTestFragmentBinding;
    private HttpTestData mHttpTestData;

    private HttpTestChartManager mChartManager;

    private boolean mShowTipEnable = true;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initHttpTestView();
    }

    private void initHttpTestView(){
        mHttpTestViewModel = ViewModelProviders.of(this, mViewModelFactory).get(HttpTestViewModel.class);
        mHttpTestData = new HttpTestData();
        mHttpTestData.firstUrl.set(HttpTestRepo.DEFAULT_TIP_URL[0]);
        mHttpTestData.secondUrl.set(HttpTestRepo.DEFAULT_TIP_URL[2]);
        mHttpTestFragmentBinding.setTestdata(mHttpTestData);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        mHttpTestFragmentBinding.firstUrlText.setAdapter(adapter);
        mHttpTestFragmentBinding.secondUrlText.setAdapter(adapter);

        mChartManager = new HttpTestChartManager(mHttpTestFragmentBinding.dataChart);

        mHttpTestFragmentBinding.testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mHttpTestData.firstUrl.get())){
                    if(!mHttpTestData.isTesting.get()){
                        mChartManager.reset();
                        mHttpTestData.isTesting.set(true);
                        mHttpTestData.enableSecondUrl.set(false);
                        mShowTipEnable = true;
                        mHttpTestViewModel.startHttpTest(true, mHttpTestData.firstUrl.get(), mHttpTestData.secondUrl.get());
                    } else {
                        mHttpTestViewModel.stopAllTest();
                        mChartManager.reset();
                        mHttpTestData.enableSecondUrl.set(true);
                        mHttpTestData.isTesting.set(false);
                    }
                } else {
                    Snackbar.make(getView(), getString(R.string.empty_url), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mHttpTestFragmentBinding.firstUrlText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    mHttpTestData.showFirstClearBtn.set(true);
                    mHttpTestData.enableSecondUrl.set(true);
                } else {
                    mHttpTestData.showFirstClearBtn.set(false);
                    mHttpTestData.enableSecondUrl.set(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mHttpTestFragmentBinding.secondUrlText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    mHttpTestData.showSecondClearBtn.set(true);
                } else {
                    mHttpTestData.showSecondClearBtn.set(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mHttpTestFragmentBinding.firstClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHttpTestData.firstUrl.set("");
            }
        });

        mHttpTestFragmentBinding.secondClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHttpTestData.secondUrl.set("");
            }
        });

        mHttpTestViewModel.getTipUrls().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                adapter.clear();
                adapter.addAll(strings);
                adapter.notifyDataSetChanged();
            }
        });

        mHttpTestViewModel.getTestResult().observe(this, new Observer<HttpTestResult>() {
            @Override
            public void onChanged(HttpTestResult httpTestResult) {
                if(httpTestResult.isNoError()) {
                    if(mHttpTestData.isTesting.get()) {
                        mChartManager.addEntry(httpTestResult.isFirstResult(), httpTestResult.getTestUrl(), httpTestResult.getResponseTime());
                        mHttpTestViewModel.startHttpTest(false, mHttpTestData.firstUrl.get(), mHttpTestData.secondUrl.get());
                    } else {
                        if(mShowTipEnable){
                            Snackbar.make(getView(), getString(R.string.http_test_stop_tip), Snackbar.LENGTH_LONG).show();
                            mShowTipEnable = false;
                        }
                    }
                } else {
                    String tip = "";
                    if(httpTestResult.getCode() == HttpTestResult.INVALID_REQUEST) {
                        tip = httpTestResult.getTestUrl() + getString(R.string.invalid_request);
                    } else if (httpTestResult.getCode() == HttpTestResult.REQUEST_EXCEPTION){
                        tip = httpTestResult.getTestUrl() + " " + getString(R.string.http_test_request_exception) + httpTestResult.getException();
                    }
                    Snackbar.make(getView(), tip, Snackbar.LENGTH_LONG).show();
                    if ((TextUtils.isEmpty(mHttpTestData.secondUrl.get()) && mHttpTestViewModel.getFirstTestStoped()) ||
                            (mHttpTestViewModel.getFirstTestStoped() && mHttpTestViewModel.getSecondTestStoped())){
                        mHttpTestData.enableSecondUrl.set(true);
                        mHttpTestData.isTesting.set(false);
                        mShowTipEnable = false;
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mHttpTestFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.http_test_fragment,
                container, false);
        return mHttpTestFragmentBinding.getRoot();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHttpTestData.isTesting.set(false);
    }

}
