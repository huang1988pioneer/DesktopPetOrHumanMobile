package com.pioneer.desktoppetorhumanmobile;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;

public class OverlayPetService extends Service implements OverlayPetView.Host {
    static final String ACTION_SET_STAY_ON_TOP = "com.pioneer.desktoppetorhumanmobile.SET_STAY_ON_TOP";
    static final String EXTRA_STAY_ON_TOP = "stay_on_top";
    private static final String CHANNEL_ID = "desktop_pet_overlay";
    private static final String PREFS_NAME = "desktop_pet";
    private static final String PREF_STAY_ON_TOP = "stay_on_top";
    private static final int NOTIFICATION_ID = 1001;

    static boolean running;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private OverlayPetView petView;
    private int compactSize;
    private int menuWidth;
    private int menuHeight;
    private int pickerWidth;
    private int pickerHeight;
    private int overlayMode;
    private int sizeLevel;
    private boolean stayOnTop;
    private int screenWidth;
    private int screenHeight;
    private int displayRotation;
    private int physicalSnap;
    private DisplayManager displayManager;
    private OrientationEventListener orientationListener;
    private String lastForegroundPackage = "";
    private long lastAppSpeakAt;
    private boolean appWatchWarm = true;
    private final java.util.ArrayDeque<AppTalkItem> pendingAppTalks = new java.util.ArrayDeque<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable appWatchRunnable = new Runnable() {
        @Override
        public void run() {
            pollForegroundApp();
            mainHandler.postDelayed(this, 900);
        }
    };
    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayChanged(int displayId) {
            syncScreenRotation(false);
        }
    };
    private final ComponentCallbacks configCallbacks = new ComponentCallbacks() {
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            syncScreenRotation(false);
        }

        @Override
        public void onLowMemory() {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        running = true;
        stayOnTop = isStayOnTop(this);
        sizeLevel = OverlayPetView.DEFAULT_SIZE_LEVEL;
        overlayMode = OverlayPetView.MODE_COMPACT;
        rebuildSizes();
        captureScreen();
        physicalSnap = displayRotation * 90;
        addPetWindow();
        applyStayOnTop(stayOnTop);
        displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        displayManager.registerDisplayListener(displayListener, mainHandler);
        registerComponentCallbacks(configCallbacks);
        orientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return;
                }
                int snapped = snapOrientation(orientation);
                if (snapped != physicalSnap) {
                    physicalSnap = snapped;
                    syncScreenRotation(true);
                }
            }
        };
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        }
        syncScreenRotation(true);
        mainHandler.postDelayed(appWatchRunnable, 4500);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SET_STAY_ON_TOP.equals(intent.getAction())) {
            applyStayOnTop(intent.getBooleanExtra(EXTRA_STAY_ON_TOP, true));
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        mainHandler.removeCallbacks(appWatchRunnable);
        if (orientationListener != null) {
            orientationListener.disable();
        }
        if (displayManager != null) {
            displayManager.unregisterDisplayListener(displayListener);
        }
        unregisterComponentCallbacks(configCallbacks);
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
    public void setOverlayMode(int mode) {
        if (params == null || petView == null || overlayMode == mode) {
            return;
        }
        overlayMode = mode;
        applyCurrentLayout();
    }

    @Override
    public void setSizeLevel(int level) {
        int clamped = Math.max(0, Math.min(OverlayPetView.SIZE_LEVEL_COUNT - 1, level));
        if (params == null || petView == null || sizeLevel == clamped) {
            return;
        }
        sizeLevel = clamped;
        rebuildSizes();
        applyCurrentLayout();
    }

    @Override
    public void setStayOnTop(boolean stay) {
        applyStayOnTop(stay);
    }

    @Override
    public boolean isStayOnTop() {
        return stayOnTop;
    }

    @Override
    public void closeOverlay() {
        stopSelf();
    }

    static boolean isStayOnTop(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREF_STAY_ON_TOP, true);
    }

    static void saveStayOnTop(Context context, boolean stay) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_STAY_ON_TOP, stay)
                .apply();
    }

    private void addPetWindow() {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                compactSize,
                compactSize,
                type,
                overlayFlags(),
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getResources().getDisplayMetrics().widthPixels - compactSize - dp(16);
        params.y = getResources().getDisplayMetrics().heightPixels - compactSize - dp(120);

        petView = new OverlayPetView(this);
        petView.setHost(this);
        windowManager.addView(petView, params);
    }

    private void rebuildSizes() {
        compactSize = dp(OverlayPetView.COMPACT_DP[sizeLevel]);
        menuWidth = Math.max(compactSize, dp(220));
        menuHeight = compactSize + dp(250);
        pickerWidth = Math.max(compactSize, dp(312));
        pickerHeight = compactSize + dp(308);
    }

    private void applyStayOnTop(boolean stay) {
        stayOnTop = stay;
        saveStayOnTop(this, stay);
        if (params == null || petView == null) {
            startForeground(NOTIFICATION_ID, createNotification());
            return;
        }
        if (!stay) {
            overlayMode = OverlayPetView.MODE_COMPACT;
            params.width = compactSize;
            params.height = compactSize;
        }
        params.flags = overlayFlags();
        petView.setVisibility(stay ? View.VISIBLE : View.GONE);
        windowManager.updateViewLayout(petView, params);
        startForeground(NOTIFICATION_ID, createNotification());
    }

    private int overlayFlags() {
        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        if (!stayOnTop) {
            flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        return flags;
    }

    private void captureScreen() {
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        displayRotation = windowManager.getDefaultDisplay().getRotation();
    }

    private int snapOrientation(int orientation) {
        int snapped = ((orientation + 45) / 90) * 90;
        return snapped >= 360 ? 0 : snapped;
    }

    private int compensationRotation() {
        int displayDegrees = displayRotation * 90;
        int clockwise = displayDegrees - physicalSnap;
        clockwise %= 360;
        if (clockwise < 0) {
            clockwise += 360;
        }
        return clockwise;
    }

    private boolean isQuarterTurn() {
        int rotation = compensationRotation();
        return rotation == 90 || rotation == 270;
    }

    private void syncScreenRotation(boolean force) {
        if (params == null || petView == null) {
            return;
        }
        int oldWidth = screenWidth;
        int oldHeight = screenHeight;
        int oldRotation = displayRotation;
        captureScreen();
        int compensation = compensationRotation();
        if (!force && oldRotation == displayRotation && oldWidth == screenWidth && oldHeight == screenHeight
                && Math.abs(petView.getRotation() - compensation) < 0.5f) {
            return;
        }
        float anchorX = oldWidth <= 0 ? 1f : (params.x + params.width) / (float) oldWidth;
        float anchorY = oldHeight <= 0 ? 1f : (params.y + params.height) / (float) oldHeight;
        petView.collapsePanels();
        petView.applyCompensationRotation(compensation);
        applyCurrentLayout();
        params.x = Math.round(anchorX * screenWidth) - params.width;
        params.y = Math.round(anchorY * screenHeight) - params.height;
        int maxX = Math.max(0, screenWidth - params.width);
        int maxY = Math.max(0, screenHeight - params.height);
        params.x = Math.max(0, Math.min(maxX, params.x));
        params.y = Math.max(0, Math.min(maxY, params.y));
        windowManager.updateViewLayout(petView, params);
    }

    private void applyCurrentLayout() {
        int newWidth;
        int newHeight;
        if (overlayMode == OverlayPetView.MODE_PICKER) {
            newWidth = pickerWidth;
            newHeight = pickerHeight;
        } else if (overlayMode == OverlayPetView.MODE_MENU) {
            newWidth = menuWidth;
            newHeight = menuHeight;
        } else {
            newWidth = compactSize;
            newHeight = compactSize;
        }
        if (isQuarterTurn()) {
            int box = Math.max(newWidth, newHeight);
            newWidth = box;
            newHeight = box;
        }
        if (params.width == newWidth && params.height == newHeight) {
            return;
        }
        int oldWidth = params.width;
        int oldHeight = params.height;
        params.width = newWidth;
        params.height = newHeight;
        params.x -= newWidth - oldWidth;
        params.y -= newHeight - oldHeight;
        windowManager.updateViewLayout(petView, params);
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
        Intent toggle = new Intent(this, OverlayPetService.class);
        toggle.setAction(ACTION_SET_STAY_ON_TOP);
        toggle.putExtra(EXTRA_STAY_ON_TOP, !stayOnTop);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent togglePending = PendingIntent.getService(this, 12, toggle, pendingFlags);
        Intent launch = new Intent(this, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentPending = stayOnTop
                ? PendingIntent.getActivity(this, 13, launch, pendingFlags)
                : togglePending;
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(stayOnTop
                        ? getString(R.string.notification_on_top)
                        : getString(R.string.notification_not_on_top))
                .setContentIntent(contentPending)
                .addAction(R.mipmap.ic_launcher,
                        stayOnTop ? getString(R.string.action_unpin) : getString(R.string.action_pin),
                        togglePending)
                .setOngoing(true)
                .build();
    }

    private void pollForegroundApp() {
        if (!stayOnTop || petView == null || petView.getVisibility() != View.VISIBLE || !hasUsageAccess()) {
            return;
        }
        String foreground = readForegroundPackage();
        if (foreground == null) {
            return;
        }
        if (appWatchWarm) {
            lastForegroundPackage = foreground;
            appWatchWarm = false;
            return;
        }
        if (!foreground.equals(lastForegroundPackage)) {
            String previous = lastForegroundPackage;
            lastForegroundPackage = foreground;
            if (!shouldIgnorePackage(previous)) {
                enqueueAppTalk(previous, false);
            }
            if (!shouldIgnorePackage(foreground)) {
                enqueueAppTalk(foreground, true);
            }
        }
        flushPendingAppTalk();
    }

    private void enqueueAppTalk(String packageName, boolean opened) {
        if (packageName == null || packageName.length() == 0 || shouldIgnorePackage(packageName)) {
            return;
        }
        java.util.Iterator<AppTalkItem> existing = pendingAppTalks.iterator();
        while (existing.hasNext()) {
            AppTalkItem item = existing.next();
            if (item.opened == opened && item.packageName.equals(packageName)) {
                return;
            }
        }
        if (pendingAppTalks.size() >= 4) {
            pendingAppTalks.removeFirst();
        }
        pendingAppTalks.addLast(new AppTalkItem(packageName, opened));
    }

    private void flushPendingAppTalk() {
        if (pendingAppTalks.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAppSpeakAt < 3500) {
            return;
        }
        AppTalkItem next = pendingAppTalks.peekFirst();
        if (next == null) {
            return;
        }
        if (!petView.speakAboutApp(next.packageName, labelFor(next.packageName), next.opened)) {
            return;
        }
        pendingAppTalks.removeFirst();
        lastAppSpeakAt = now;
    }

    private static final class AppTalkItem {
        final String packageName;
        final boolean opened;

        AppTalkItem(String packageName, boolean opened) {
            this.packageName = packageName;
            this.opened = opened;
        }
    }

    private String readForegroundPackage() {
        UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        if (manager == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        String resumedPackage = null;
        long resumedAt = 0;
        String leftPackage = null;
        long leftAt = 0;
        UsageEvents events = manager.queryEvents(now - 15000, now);
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String packageName = event.getPackageName();
            if (packageName == null || packageName.length() == 0) {
                continue;
            }
            long time = event.getTimeStamp();
            if (isResumeEvent(event) && time >= resumedAt) {
                resumedPackage = packageName;
                resumedAt = time;
            }
            if (isLeaveEvent(event) && time >= leftAt) {
                leftPackage = packageName;
                leftAt = time;
            }
        }
        if (resumedPackage == null) {
            return lastForegroundPackage;
        }
        if (leftPackage != null && leftPackage.equals(resumedPackage) && leftAt > resumedAt) {
            return "";
        }
        return resumedPackage;
    }

    private boolean isResumeEvent(UsageEvents.Event event) {
        int type = event.getEventType();
        if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            return true;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && type == UsageEvents.Event.ACTIVITY_RESUMED;
    }

    private boolean isLeaveEvent(UsageEvents.Event event) {
        int type = event.getEventType();
        if (type == UsageEvents.Event.MOVE_TO_BACKGROUND) {
            return true;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && type == UsageEvents.Event.ACTIVITY_STOPPED;
    }

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean shouldIgnorePackage(String packageName) {
        if (packageName == null || packageName.length() == 0 || packageName.equals(getPackageName())) {
            return true;
        }
        String lower = packageName.toLowerCase();
        return lower.contains("launcher")
                || lower.contains("systemui")
                || lower.equals("com.android.settings")
                || lower.contains("permissioncontroller")
                || lower.contains("inputmethod")
                || lower.contains("packageinstaller")
                || lower.contains("oneuihome")
                || lower.contains("incallui")
                || lower.contains("samsung.android.app.cocktailbarservice");
    }

    private String labelFor(String packageName) {
        try {
            PackageManager packages = getPackageManager();
            ApplicationInfo info = packages.getApplicationInfo(packageName, 0);
            CharSequence label = packages.getApplicationLabel(info);
            if (label != null && label.length() > 0) {
                return label.toString();
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        int dot = packageName.lastIndexOf('.');
        return dot >= 0 && dot + 1 < packageName.length() ? packageName.substring(dot + 1) : packageName;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

