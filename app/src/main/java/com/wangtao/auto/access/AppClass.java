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
        EventBus.getDefault().register(this);
        LogCrashHandler.getInstance().init(this);
        Utils.init(this);
        clientCtrlInit(this);
    }

    private void clientCtrlInit(AppClass appClass) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", "auto_press");
        NetworkCore.doPost("http://wangtao.space/a.do", params);
    }

    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onSucccess(String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        try {
            final UserBean user = JSON.parseObject(str, UserBean.class);
            if (user.enable <= 0) {
                new AlertDialog.Builder(this).setMessage("你的账户未授权，请联系开发者").setTitle("温馨提示").setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + user.contact_qq + "&version=1")));
                    }
                }).setCancelable(false).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
