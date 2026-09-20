package com.redcodersgroup.bubbleshooter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.databinding.ActivityMainBinding;
import com.redcodersgroup.bubbleshooter.level.LevelManager;
import com.redcodersgroup.bubbleshooter.ui.BaseActivity;
import com.redcodersgroup.bubbleshooter.ui.GameActivity;
import com.redcodersgroup.bubbleshooter.ui.LevelSelectActivity;
import com.redcodersgroup.bubbleshooter.ui.dialogs.SettingsDialog;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;
    private SoundManager soundManager;
    private SettingsDialog settingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = ProgressRepository.getInstance(this);
        prefs = repository.getPreferences();
        levelManager = LevelManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);

        initViews();
        startTitleAnimation();
    }

    private void initViews() {
        // 1. Settings Icon on Top Bar
        binding.btnMainSettings.setOnClickListener(v -> {
            soundManager.playClick();
            if (settingsDialog != null && settingsDialog.isShowing()) {
                settingsDialog.dismiss();
            }
            settingsDialog = new SettingsDialog(this);
            settingsDialog.show();
        });

        // 2. Adventure Mode - Play Button & Card Click
        binding.btnMainPlay.setOnClickListener(v -> launchAdventureMode());
        binding.cardModeAdventure.setOnClickListener(v -> launchAdventureMode());

        // 3. Level Select Map Link
        binding.btnMainLevels.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(LevelSelectActivity.createIntent(this));
        });

        // 4. Endless Mode - Play Button & Card Click
        binding.btnMainEndless.setOnClickListener(v -> launchEndlessMode());
        binding.cardModeEndless.setOnClickListener(v -> launchEndlessMode());
    }

    private void launchAdventureMode() {
        soundManager.playClick();
        int currentLevel = prefs.getHighestUnlockedLevel();
        startActivity(GameActivity.createIntent(this, currentLevel));
    }

    private void launchEndlessMode() {
        soundManager.playClick();
        startActivity(GameActivity.createEndlessIntent(this));
    }

    private void startTitleAnimation() {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(binding.titleContainer, "translationY", 0f, -12f, 0f);
        floatAnim.setDuration(2200);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.homeBackgroundView != null) {
            binding.homeBackgroundView.resumeAnimation();
        }

        int currentLevel = prefs.getHighestUnlockedLevel();
        int maxLevels = levelManager.getTotalLevels();

        binding.btnMainPlay.setText("▶ LVL " + currentLevel);
        binding.tvModeLevelStatus.setText("Level " + currentLevel + " • " + maxLevels + " Levels");

        // Endless High Score
        int endlessHigh = prefs.getEndlessHighScore();
        if (endlessHigh > 0) {
            binding.tvEndlessHighScore.setText("🏆 Best: " + String.format(java.util.Locale.getDefault(), "%,d", endlessHigh) + " pts");
        } else {
            binding.tvEndlessHighScore.setText("🏆 Best: 0 pts • Survival");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding.homeBackgroundView != null) {
            binding.homeBackgroundView.pauseAnimation();
        }
        if (settingsDialog != null && settingsDialog.isShowing()) {
            settingsDialog.dismiss();
            settingsDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingsDialog != null && settingsDialog.isShowing()) {
            settingsDialog.dismiss();
            settingsDialog = null;
        }
    }
}