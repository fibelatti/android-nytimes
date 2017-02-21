package com.fibelatti.nytimes;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

public class AndroidApplication
        extends Application {
    public static final String TAG = AndroidApplication.class.getSimpleName();
    public static AndroidApplication app;

    public AndroidApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(true)
                        .build()
                ).build());

        LeakCanary.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
