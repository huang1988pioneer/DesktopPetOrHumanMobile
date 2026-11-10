package com.pioneer.desktoppetorhumanmobile;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 2001;
    private PetPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new PetPreferences(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.rgb(248, 246, 241));
        getWindow().setNavigationBarColor(Color.rgb(248, 246, 241));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 12);
            }
        }
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
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(Color.rgb(248, 246, 241));

        TextView eyebrow = new TextView(this);
        eyebrow.setText("DESKTOP COMPANIONS");
        eyebrow.setTextColor(Color.rgb(105, 101, 115));
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(eyebrow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView hero = new ImageView(this);
        int heroArt = getResources().getIdentifier("gpt_daughter_idle", "drawable", getPackageName());
        if (heroArt != 0) {
            hero.setImageResource(heroArt);
        }
        hero.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable heroCard = new GradientDrawable();
        heroCard.setColor(Color.WHITE);
        heroCard.setCornerRadius(dp(28));
        hero.setBackground(heroCard);
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(dp(196), dp(196));
        heroParams.setMargins(0, dp(16), 0, dp(12));
        root.addView(hero, heroParams);

        TextView title = new TextView(this);
        title.setText(R.string.launcher_title);
        title.setTextColor(Color.rgb(30, 31, 36));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setText(R.string.launcher_body);
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

        Button restrictedButton = makeButton(getString(R.string.restricted_settings_button));
        restrictedButton.setVisibility(hasUsageAccess() ? View.GONE : View.VISIBLE);
        restrictedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppDetailsSettings();
            }
        });
        root.addView(restrictedButton);

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

        final boolean stayOnTop = preferences.isStayOnTop();
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
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(root);
        setContentView(scroll);
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
        boolean next = !preferences.isStayOnTop();
        preferences.saveStayOnTop(next);
        if (OverlayPetService.running) {
            Intent intent = new Intent(this, OverlayPetService.class);
            intent.setAction(OverlayPetService.ACTION_SET_STAY_ON_TOP);
            intent.putExtra(OverlayPetService.EXTRA_STAY_ON_TOP, next);
            startService(intent);
        }
        renderLauncher();
    }

    private void openAppDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void requestUsageAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        try {
            startActivity(intent);
        } catch (Exception ignored) {
            openAppDetailsSettings();
        }
    }

    private void startPetOverlay() {
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
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
