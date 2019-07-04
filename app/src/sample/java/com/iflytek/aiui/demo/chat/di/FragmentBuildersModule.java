package com.iflytek.aiui.demo.chat.di;

import com.iflytek.aiui.demo.chat.ui.about.AboutFragment;
import com.iflytek.aiui.demo.chat.ui.chat.ChatFragment;
import com.iflytek.aiui.demo.chat.ui.detail.DetailFragment;
import com.iflytek.aiui.demo.chat.ui.settings.SettingsFragment;
import com.iflytek.aiui.demo.chat.ui.test.HttpTestFragement;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger2 Fragment Module
 */

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract ChatFragment contributesChatFragment( );

    @ContributesAndroidInjector
    abstract DetailFragment contributeDetailFragment( );

    @ContributesAndroidInjector
    abstract AboutFragment contributeAboutFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment contributeSettingFragment( );

    @ContributesAndroidInjector
    abstract HttpTestFragement contributeHttpTestFragment();
}
