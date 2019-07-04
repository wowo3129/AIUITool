package com.iflytek.aiui.demo.chat.di;

import com.iflytek.aiui.demo.chat.ChatActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger2 Activity Module
 */

@Module
public abstract class ChatActivityModule {
    @ContributesAndroidInjector(modules = {FragmentBuildersModule.class})
    public abstract ChatActivity contributesChatActivity();
}
