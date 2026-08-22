package com.pioneer.desktoppetorhumanmobile;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.rgb(248, 246, 241));
        getWindow().setNavigationBarColor(Color.rgb(248, 246, 241));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 12);
        }
        renderLauncher();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderLauncher();
    }

    private void renderLauncher() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(44), dp(28), dp(28));
        root.setBackgroundColor(Color.rgb(248, 246, 241));

        TextView title = new TextView(this);
        title.setText(R.string.overlay_permission_title);
        title.setTextColor(Color.rgb(30, 31, 36));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setText(R.string.overlay_permission_body);
        body.setTextColor(Color.rgb(75, 79, 87));
        body.setTextSize(16);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyParams.setMargins(0, dp(18), 0, dp(28));
        root.addView(body, bodyParams);

        Button permissionButton = makeButton(getString(R.string.overlay_permission_button));
        permissionButton.setVisibility(hasOverlayPermission() ? View.GONE : View.VISIBLE);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestOverlayPermission();
            }
        });
        root.addView(permissionButton);

        Button usageButton = makeButton(getString(R.string.usage_access_button));
        usageButton.setVisibility(hasUsageAccess() ? View.GONE : View.VISIBLE);
        usageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestUsageAccess();
            }
        });
        root.addView(usageButton);

        Button startButton = makeButton(getString(R.string.start_pet_button));
        startButton.setEnabled(hasOverlayPermission());
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPetOverlay();
            }
        });
        root.addView(startButton);

        final boolean stayOnTop = OverlayPetService.isStayOnTop(this);
        Button pinButton = makeButton(getString(stayOnTop ? R.string.stay_on_top_on : R.string.stay_on_top_off));
        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStayOnTop();
            }
        });
        root.addView(pinButton);

        Button stopButton = makeButton(getString(R.string.stop_pet_button));
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, OverlayPetService.class));
            }
        });
        root.addView(stopButton);
        setContentView(root);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        params.setMargins(0, dp(10), 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
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

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
        }
    }

    private void toggleStayOnTop() {
        boolean next = !OverlayPetService.isStayOnTop(this);
        OverlayPetService.saveStayOnTop(this, next);
        if (OverlayPetService.running) {
            Intent intent = new Intent(this, OverlayPetService.class);
            intent.setAction(OverlayPetService.ACTION_SET_STAY_ON_TOP);
            intent.putExtra(OverlayPetService.EXTRA_STAY_ON_TOP, next);
            startService(intent);
        }
        renderLauncher();
    }

    private void requestUsageAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (Exception ignored) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private void startPetOverlay() {
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }
        if (!hasUsageAccess()) {
            requestUsageAccess();
            return;
        }
        Intent intent = new Intent(this, OverlayPetService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        moveTaskToBack(true);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
