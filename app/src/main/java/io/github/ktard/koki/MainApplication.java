package io.github.ktard.koki;

import android.app.Application;
import android.content.Context;

import com.tencent.mmkv.MMKV;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.qualifiers.ApplicationContext;
import rxdogtag2.RxDogTag;
import rxdogtag2.autodispose2.AutoDisposeConfigurer;
import timber.log.Timber;

@HiltAndroidApp
public class MainApplication extends Application {
    // TODO: Delete when https://github.com/google/dagger/issues/3601 is resolved.
    @Inject
    @ApplicationContext
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        // Initialise app database
        MMKV.initialize(this);

        // Better error handling for cases where accidental `onError` happens
        RxDogTag.builder()
                .configureWith(AutoDisposeConfigurer::configure)
                .install();
    }
}
