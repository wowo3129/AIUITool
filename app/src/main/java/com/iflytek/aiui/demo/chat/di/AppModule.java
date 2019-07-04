package com.iflytek.aiui.demo.chat.di;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger2 Module
 */

@Module(includes = ViewModelModule.class)
public class AppModule {
    @Provides
    @Singleton
    public Context providesContext(Application application) {
        return application;
    }


    @Provides
    @Singleton
    public ScheduledExecutorService provideExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
