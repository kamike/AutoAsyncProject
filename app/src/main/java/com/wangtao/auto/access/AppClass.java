package com.wangtao.auto.access;

import android.app.Application;

import com.wangtao.auto.access.utils.LogCrashHandler;

/**
 * Created by Administrator on 2017/8/21.
 */

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogCrashHandler.getInstance().init(this);
    }
}
