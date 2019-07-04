package com.iflytek.aiui.demo.chat.repository.ping;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;


public class HttpTestData {

    public ObservableBoolean isTesting = new ObservableBoolean();
    public ObservableBoolean enableSecondUrl = new ObservableBoolean();
    public ObservableBoolean showFirstClearBtn = new ObservableBoolean();
    public ObservableBoolean showSecondClearBtn = new ObservableBoolean();
    public ObservableField<String> firstUrl = new ObservableField<>();
    public ObservableField<String> secondUrl = new ObservableField<>();

}
