package com.iflytek.aiui.demo.chat.ui.about;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iflytek.aiui.demo.chat.BuildConfig;
import com.iflytek.aiui.demo.chat.R;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;


/**
 * 关于界面Fragment
 */
public class AboutFragment extends Fragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    AboutViewModel mAboutModel;

    protected TextView mUIDTxt;
    public AboutFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        ((TextView) view.findViewById(R.id.versionCode)).setText("Version: " + BuildConfig.VERSION_NAME);
        mUIDTxt = view.findViewById(R.id.uid);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAboutModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(AboutViewModel.class);
        //展示当前用户UID
        mAboutModel.getUID().observe(this, s -> mUIDTxt.setText("UID: " + s));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
