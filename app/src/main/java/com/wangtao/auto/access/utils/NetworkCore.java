package com.wangtao.auto.access.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by wangtao on 2017/2/21.
 */

public class NetworkCore {
    public static final String BASEURL = "";

    public static final MediaType JSON_TYPE = MediaType.parse("application/json");

    public static void doPost(String urlName, HashMap<String, Object> params) {
        if (TextUtils.isEmpty(urlName)) {
            return;
        }
        //忽略所有证书
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        String content = JSON.toJSONString(params);
        LogUtils.e("content:" + content);
        RequestBody body = RequestBody.create(JSON_TYPE, content);
        final Request req = new Request.Builder().url( urlName).post(body).addHeader("Content-Type", "application/json").build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                EventBus.getDefault().post("");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException, JSONException {
                String resoult = response.body().string();
                if (TextUtils.isEmpty(resoult)) {
                    EventBus.getDefault().post("");
                    return;
                }
                EventBus.getDefault().post(resoult);


            }
        });

    }


}
