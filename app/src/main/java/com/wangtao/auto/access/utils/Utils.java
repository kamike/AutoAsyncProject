package com.wangtao.auto.access.utils;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/8/21.
 */

public class Utils {
    public static String toNodeString(AccessibilityNodeInfo node) {
        if (node == null) {
            return "node 为空";
        }
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        if (rect.centerX() == 0 && rect.centerY() == 0) {
            boolean isClickSuccess = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            LogUtils.i("点击成功：" + isClickSuccess + "," + node);

        }
        return node.getText() + "," + node.getContentDescription() + "," + rect.centerX() + "," + rect.centerY() + "," + node.getClassName();
    }

    public static void toNodeString(List<AccessibilityNodeInfo> list) {
        if (list == null) {
            return;
        }
        for (AccessibilityNodeInfo node : list) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            LogUtils.i("node：" + node.getText() + "," + node.getContentDescription() + "," + rect.centerX() + "," + rect.centerY() + "," + node.getClassName());
        }
    }
}
