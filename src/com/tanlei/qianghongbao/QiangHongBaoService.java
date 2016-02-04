package com.tanlei.qianghongbao;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class QiangHongBaoService extends AccessibilityService {

    static final String TAG = "QiangHongBao";

    /** 微信的包名 */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /** 红包消息的关键字 */
    static final String HONGBAO_TEXT_KEY = "[微信红包]";
    static Map<String, Long> fetchedIdentifiers = new HashMap<String, Long>();

    static boolean flag = true;

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        // android.os.Debug.waitForDebugger();
        Log.d(TAG, "事件---->" + event);

        // 通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY) && text.compareTo(HONGBAO_TEXT_KEY) > 0) {
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /** 打开通知栏消息 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        // 将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
            Thread.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        checkHongbao();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        // android.os.Debug.waitForDebugger();
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkHongbao() {
        // android.os.Debug.waitForDebugger();
        try {
            if (!flag) {
                return;
            }
            synchronized (this) {
                if (flag) {
                    flag = false;
                }
            }
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

            if (nodeInfo == null) {
                Log.w(TAG, "rootWindow为空");
                flag = true;
                return;
            }
            List<AccessibilityNodeInfo> listHBB = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");
            if (!listHBB.isEmpty()) {
                AccessibilityNodeInfo n2 = listHBB.get(0);
                n2.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
            List<AccessibilityNodeInfo> listHongbao = nodeInfo.findAccessibilityNodeInfosByText("[微信红包]");
            for (AccessibilityNodeInfo accessibilityNodeInfo : listHongbao) {
                if (String.valueOf(accessibilityNodeInfo.getContentDescription()).contains(HONGBAO_TEXT_KEY) && String.valueOf(accessibilityNodeInfo.getContentDescription()).contains("条未读")) {
                    Log.i(TAG, "-->微信红包:" + accessibilityNodeInfo);
                    // TODO
                    // 判断是否是新消息
                    if (accessibilityNodeInfo.isClickable()) {
                        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    return;
                }
            }

            List<AccessibilityNodeInfo> list4 = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b_");

            Comparator<AccessibilityNodeInfo> comparator = new Comparator<AccessibilityNodeInfo>() {

                @Override
                public int compare(AccessibilityNodeInfo m1, AccessibilityNodeInfo m2) {

                    Rect outBounds1 = new Rect();
                    Rect outBounds2 = new Rect();
                    m1.getBoundsInScreen(outBounds1);
                    int m1B = outBounds1.bottom;
                    m1.getBoundsInScreen(outBounds2);
                    int m2B = outBounds2.bottom;
                    return m1B > m2B ? 0 : -1;
                }

            };

            Collections.sort(list4, comparator);

            if (list4 != null && list4.size() > 0) {
                for (int i = 0; i < list4.size(); i++) {
                    AccessibilityNodeInfo accessibilityNodeInfo = list4.get(i);
                    if (accessibilityNodeInfo.findAccessibilityNodeInfosByText("微信红包").isEmpty()) {
                        continue;
                    }
                    // AccessibilityNodeInfo parent =
                    // accessibilityNodeInfo.getChild(0);
                    String picStr = String.valueOf(accessibilityNodeInfo.getParent().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bb").get(0).getContentDescription());
                    String hbMsg = String.valueOf(accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e3").get(0).getText());
                    // String msg = String.valueOf(parent.getText()) + hbMsg;
                    String msg = picStr + hbMsg;
                    String json = (accessibilityNodeInfo.hashCode() + "" + getNodeId(accessibilityNodeInfo) + msg).hashCode() + "";
                    long current = System.currentTimeMillis();
                    if (fetchedIdentifiers.containsKey(json) && current - fetchedIdentifiers.get(json) < 11000) {// &&
                        // fetchedIdentifiers.put(json, current);
                        continue;
                    }
                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e4").get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    fetchedIdentifiers.put(json, current);
                    return;
                }
            }

            List<AccessibilityNodeInfo> listOver = nodeInfo.findAccessibilityNodeInfosByText("手慢了，红包派完了");
            if (listOver != null && listOver.size() > 0) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
            List<AccessibilityNodeInfo> list3 = nodeInfo.findAccessibilityNodeInfosByText("红包记录");
            if (list3 != null && list3.size() > 0) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
            List<AccessibilityNodeInfo> listDetail = nodeInfo.findAccessibilityNodeInfosByText("红包详情");
            if (listDetail != null && listDetail.size() > 0) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            flag = true;
        }
    }

    /**
     * 获取节点对象唯一的id，通过正则表达式匹配 AccessibilityNodeInfo@后的十六进制数字
     *
     * @param node
     *            AccessibilityNodeInfo对象
     * @return id字符串
     */
    private String getNodeId(AccessibilityNodeInfo node) {
        /* 用正则表达式匹配节点Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
        objHashMatcher.find();

        return objHashMatcher.group(0);
    }

}
