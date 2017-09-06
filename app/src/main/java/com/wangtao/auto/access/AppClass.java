package com.wangtao.auto.access;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.blankj.utilcode.util.Utils;
import com.wangtao.auto.access.utils.ClientCtrl;
import com.wangtao.auto.access.utils.LogCrashHandler;
import com.wangtao.auto.access.utils.LogUtils;
import com.wangtao.auto.access.utils.NetworkCore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/21.
 */

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        LogCrashHandler.getInstance().init(this);
        Utils.init(this);

    }


}
