package io.github.kdetard.koki;

import android.app.Application;

import com.mapbox.mapboxsdk.Mapbox;
import com.tencent.mmkv.MMKV;

import dagger.hilt.android.HiltAndroidApp;
import rxdogtag2.RxDogTag;
import rxdogtag2.autodispose2.AutoDisposeConfigurer;
import timber.log.Timber;

@HiltAndroidApp
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Better error handling when accidental `onError` happens
        RxDogTag.builder()
                .configureWith(AutoDisposeConfigurer::configure)
                .install();

        // Initialise app database
        MMKV.initialize(this);

        Mapbox.getInstance(this);
    }
}
