package com.wangtao.auto.access.utils;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Administrator on 2017/8/21.
 */

public class Utils {
    public static String toNodeString(AccessibilityNodeInfo node) {
        Rect rect=new Rect();
        node.getBoundsInScreen(rect);
        return node.getText()+","+node.getContentDescription()+","+rect.centerX()+","+rect.centerY();
    }
}
