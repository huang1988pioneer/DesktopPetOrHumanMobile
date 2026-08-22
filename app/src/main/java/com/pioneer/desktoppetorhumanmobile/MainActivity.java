package com.pioneer.desktoppetorhumanmobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    private DesktopPetView desktopPetView;
    private TextView moodText;
    private TextView happinessLabel;
    private TextView energyLabel;
    private TextView fullnessLabel;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;
    private ProgressBar fullnessBar;
    private boolean humanMode;
    private int happiness = 68;
    private int energy = 72;
    private int fullness = 58;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(248, 246, 241));
        getWindow().setNavigationBarColor(Color.rgb(248, 246, 241));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(248, 246, 241));
        root.setPadding(dp(18), dp(16), dp(18), dp(16));

        moodText = new TextView(this);
        moodText.setTextColor(Color.rgb(31, 41, 51));
        moodText.setTextSize(22);
        moodText.setGravity(Gravity.CENTER);
        moodText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(moodText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        desktopPetView = new DesktopPetView(this);
        desktopPetView.setCompanionListener(new DesktopPetView.CompanionListener() {
            @Override
            public void onTouched() {
                updateStats(3, -1, -1);
            }

            @Override
            public void onDragged() {
                updateStats(1, -1, -1);
            }
        });
        LinearLayout.LayoutParams petParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        petParams.setMargins(0, dp(14), 0, dp(12));
        root.addView(desktopPetView, petParams);

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER);
        modeRow.addView(makeModeButton("Pet", false));
        modeRow.addView(makeModeButton("Human", true));
        root.addView(modeRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        happinessLabel = addMeter(root, "Happiness");
        happinessBar = (ProgressBar) happinessLabel.getTag();
        energyLabel = addMeter(root, "Energy");
        energyBar = (ProgressBar) energyLabel.getTag();
        fullnessLabel = addMeter(root, "Fullness");
        fullnessBar = (ProgressBar) fullnessLabel.getTag();

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        actionRow.setPadding(0, dp(10), 0, 0);
        actionRow.addView(makeActionButton("Feed", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStats(4, -2, 18);
                desktopPetView.say(humanMode ? "That helps." : "Snack time.");
            }
        }));
        actionRow.addView(makeActionButton("Play", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStats(16, -12, -5);
                desktopPetView.say(humanMode ? "Let's move." : "Again.");
            }
        }));
        actionRow.addView(makeActionButton("Rest", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStats(3, 20, -2);
                desktopPetView.say(humanMode ? "Quiet minute." : "Nap mode.");
            }
        }));
        root.addView(actionRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        applyMode(false);
        renderStats();
    }

    private Button makeModeButton(String label, final boolean mode) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyMode(mode);
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button makeActionButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);
        return button;
    }

    private TextView addMeter(LinearLayout root, String label) {
        TextView textView = new TextView(this);
        textView.setTextColor(Color.rgb(31, 41, 51));
        textView.setTextSize(13);
        textView.setPadding(0, dp(8), 0, dp(3));
        root.addView(textView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        root.addView(progressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8)));
        textView.setTag(progressBar);
        textView.setText(label);
        return textView;
    }

    private void applyMode(boolean human) {
        humanMode = human;
        desktopPetView.setHumanMode(human);
        desktopPetView.say(human ? "Ready for the day." : "Tiny friend online.");
        renderStats();
    }

    private void updateStats(int happinessDelta, int energyDelta, int fullnessDelta) {
        happiness = clamp(happiness + happinessDelta);
        energy = clamp(energy + energyDelta);
        fullness = clamp(fullness + fullnessDelta);
        renderStats();
    }

    private void renderStats() {
        String mood = humanMode ? "Pocket Human" : "Pocket Pet";
        String suffix = happiness > 80 ? "Happy" : energy < 30 ? "Sleepy" : fullness < 30 ? "Hungry" : "Calm";
        moodText.setText(String.format(Locale.US, "%s - %s", mood, suffix));
        happinessLabel.setText(String.format(Locale.US, "Happiness %d%%", happiness));
        energyLabel.setText(String.format(Locale.US, "Energy %d%%", energy));
        fullnessLabel.setText(String.format(Locale.US, "Fullness %d%%", fullness));
        happinessBar.setProgress(happiness);
        energyBar.setProgress(energy);
        fullnessBar.setProgress(fullness);
        desktopPetView.setStats(happiness, energy, fullness);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

