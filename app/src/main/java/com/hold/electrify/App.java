package com.hold.electrify;

import android.app.Application;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.pgyersdk.PgyerActivityManager;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.crash.PgyerCrashObservable;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //蒲公英
        PgyCrashManager.register();
        PgyerCrashObservable.get().attach((thread, throwable) -> {
            LogUtils.e(throwable, Log.getStackTraceString(throwable));
        });
        PgyerActivityManager.set(this);
    }
}