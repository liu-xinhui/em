package com.powershare.etm;

import android.app.Application;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.pgyersdk.PgyerActivityManager;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.crash.PgyerCrashObservable;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //蒲公英
        PgyCrashManager.register();
        PgyerCrashObservable.get().attach((thread, throwable) -> {
            LogUtils.e(throwable, Log.getStackTraceString(throwable));
        });
        PgyerActivityManager.set(this);
    }

    public static App getInstance() {
        return app;
    }
}