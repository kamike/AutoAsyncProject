package com.wangtao.auto.access.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedHashSet;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Administrator on 2017/8/21.
 */

public class UtilsAccess {
    public static String toNodeString(AccessibilityNodeInfo node) {
        if (node == null) {
            return "node 为空";
        }
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
//        if (rect.centerX() == 0 && rect.centerY() == 0) {
//            boolean isClickSuccess = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//
//
//            boolean parent = node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            LogUtils.i("点击成功：" + parent + "," + node.getParent());
//
//        }
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

    /**
     * 得到node的文字描述
     *
     * @param nodeTarget
     * @return
     */
    public static String getNodeInfoString(AccessibilityNodeInfo nodeTarget) {
        if (nodeTarget == null) {
            return null;
        }
        if (TextUtils.isEmpty(nodeTarget.getText())) {
            if (TextUtils.isEmpty(nodeTarget.getContentDescription())) {
                return null;
            } else {
                return nodeTarget.getContentDescription().toString();
            }
        } else {
            return nodeTarget.getText().toString();
        }
    }

    /**
     * 根据文字去点击最接近的那个,如果点击失败了，就点击父容器
     *
     * @param tag
     * @param windowInfo
     */
    public static void onclickTagNode(String tag, AccessibilityNodeInfo windowInfo) {

        if (windowInfo == null || TextUtils.isEmpty(tag)) {
            return;
        }
        List<AccessibilityNodeInfo> listTar = windowInfo.findAccessibilityNodeInfosByText(tag);

        if (listTar != null) {
            LogUtils.i("搜索到类似的多少条：" + listTar.size());
            for (AccessibilityNodeInfo i : listTar) {
                try {
                    boolean isClickSuccess = i.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (!isClickSuccess) {
                        AccessibilityNodeInfo node = i.getParent();
                        if (node != null) {
                            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                return;
                            }
                        }

                    }
                    if (isClickSuccess) {
                        return;
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void inputMessage(Context context, String text, AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        //找到当前获取焦点的view
        AccessibilityNodeInfo target = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (target == null) {
            LogUtils.i("input: null====");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        target.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    public static Rect findRect2String(LinkedHashSet<AccessibilityNodeInfo> list, String str) {
        if (list == null || TextUtils.isEmpty(str)) {
            return null;
        }

        for (AccessibilityNodeInfo node : list) {
            if (TextUtils.equals(getNodeInfoString(node), str)) {
                Rect rect = new Rect();
                node.getBoundsInScreen(rect);
                if (rect.centerX() != 0 || rect.centerY() != 0) {
                    return rect;
                }
            }

        }
        return null;

    }
}
