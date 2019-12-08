package com.powershare.etm;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.bugtags.library.Bugtags;
import com.bugtags.library.BugtagsOptions;
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
        //Bugtags
        BugtagsOptions options = new BugtagsOptions.Builder().
                trackingBackgroundCrash(true).
                trackingLocation(false).//是否获取位置，默认 true
                trackingCrashLog(true).//是否收集crash，默认 true
                trackingConsoleLog(true).//是否收集console log，默认 true
                trackingUserSteps(true).//是否收集用户操作步骤，默认 true
                trackingNetworkURLFilter("(.*)").//自定义网络请求跟踪的 url 规则，默认 null
                build();
        Bugtags.start("439fc09d99d5cafd8d09f5d8f29e35a4", this, Bugtags.BTGInvocationEventBubble, options);
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