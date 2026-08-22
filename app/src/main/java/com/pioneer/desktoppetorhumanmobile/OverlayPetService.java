package com.pioneer.desktoppetorhumanmobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

public class OverlayPetService extends Service implements OverlayPetView.Host {
    private static final String CHANNEL_ID = "desktop_pet_overlay";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private OverlayPetView petView;
    private int compactSize;
    private int expandedHeight;
    private boolean expanded;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        compactSize = dp(260);
        expandedHeight = dp(460);
        startForeground(NOTIFICATION_ID, createNotification());
        addPetWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (petView != null) {
            windowManager.removeView(petView);
            petView = null;
        }
        super.onDestroy();
    }

    @Override
    public void moveBy(float dx, float dy) {
        if (params == null || petView == null) {
            return;
        }
        params.x += Math.round(dx);
        params.y += Math.round(dy);
        windowManager.updateViewLayout(petView, params);
    }

    @Override
    public void setMenuExpanded(boolean shouldExpand) {
        if (params == null || petView == null || expanded == shouldExpand) {
            return;
        }
        expanded = shouldExpand;
        int oldHeight = params.height;
        params.height = expanded ? expandedHeight : compactSize;
        params.y -= params.height - oldHeight;
        windowManager.updateViewLayout(petView, params);
    }

    @Override
    public void closeOverlay() {
        stopSelf();
    }

    private void addPetWindow() {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                compactSize,
                compactSize,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getResources().getDisplayMetrics().widthPixels - compactSize - dp(16);
        params.y = getResources().getDisplayMetrics().heightPixels - compactSize - dp(120);

        petView = new OverlayPetView(this);
        petView.setHost(this);
        windowManager.addView(petView, params);
    }

    private Notification createNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Desktop pet overlay",
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("桌寵正在顯示於其他 App 上層")
                .setOngoing(true)
                .build();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

