package com.powershare.etm;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.gyf.cactus.Cactus;
import com.gyf.cactus.callback.CactusCallback;

public class App extends Application {

    private static App app;

    public static App getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public void startKeepAlive() {
        Cactus.getInstance()
                .isDebug(true)
                .hideNotification(true)
                .hideNotificationAfterO(true)
                .addCallback(new CactusCallback() {
                    @Override
                    public void doWork(int times) {
                        LogUtils.d("服务开启");
                    }

                    @Override
                    public void onStop() {
                        LogUtils.d("服务关闭");
                    }
                })
                .register(this);
    }

    public void stopKeepAlive() {
        Cactus.getInstance().unregister(this);
    }
}