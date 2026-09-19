package com.redcodersgroup.bubbleshooter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.databinding.ActivityMainBinding;
import com.redcodersgroup.bubbleshooter.level.LevelManager;
import com.redcodersgroup.bubbleshooter.ui.BaseActivity;
import com.redcodersgroup.bubbleshooter.ui.GameActivity;
import com.redcodersgroup.bubbleshooter.ui.LevelSelectActivity;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;
    private SoundManager soundManager;

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
        // 1. Adventure Mode - Play Button & Card Click
        binding.btnMainPlay.setOnClickListener(v -> launchAdventureMode());
        binding.cardModeAdventure.setOnClickListener(v -> launchAdventureMode());

        // 2. Endless Mode - Play Button & Card Click
        binding.btnMainEndless.setOnClickListener(v -> launchEndlessMode());
        binding.cardModeEndless.setOnClickListener(v -> launchEndlessMode());

        // 3. Levels / World Saga Map
        binding.btnMainLevels.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(LevelSelectActivity.createIntent(this));
        });

        binding.btnMainTrophy.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(LevelSelectActivity.createIntent(this));
        });

        // 4. Daily Star Chest
        binding.layoutStarChest.setOnClickListener(v -> {
            soundManager.playClick();
            int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
            if (totalStars >= 10) {
                Toast.makeText(this, "🎁 Star Chest Opened! +200 Coins & +1 Bomb Booster!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Earn " + (10 - totalStars) + " more stars to unlock this chest!", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Daily Gift
        binding.btnMainGift.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "🎁 Daily Gift: +100 Coins & +1 Rainbow Booster!", Toast.LENGTH_LONG).show();
        });

        // 6. Sound & Haptics
        binding.btnMainSound.setOnClickListener(v -> {
            boolean current = prefs.isSoundEnabled();
            prefs.setSoundEnabled(!current);
            soundManager.playClick();
            updateSoundButton();
        });

        binding.btnMainHaptic.setOnClickListener(v -> {
            boolean current = prefs.isHapticEnabled();
            prefs.setHapticEnabled(!current);
            soundManager.playClick();
            updateHapticButton();
        });

        updateSoundButton();
        updateHapticButton();
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

    private void updateSoundButton() {
        if (prefs.isSoundEnabled()) {
            binding.btnMainSound.setImageResource(R.drawable.btn_sound_green);
            binding.btnMainSound.setAlpha(1.0f);
        } else {
            binding.btnMainSound.setImageResource(R.drawable.btn_sound_gray);
            binding.btnMainSound.setAlpha(0.65f);
        }
    }

    private void updateHapticButton() {
        if (prefs.isHapticEnabled()) {
            binding.btnMainHaptic.setImageResource(R.drawable.btn_vibration_yellow);
            binding.btnMainHaptic.setAlpha(1.0f);
        } else {
            binding.btnMainHaptic.setImageResource(R.drawable.btn_vibration_gray);
            binding.btnMainHaptic.setAlpha(0.65f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.homeBackgroundView != null) {
            binding.homeBackgroundView.resumeAnimation();
        }

        int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
        int currentLevel = prefs.getHighestUnlockedLevel();
        int maxLevels = levelManager.getTotalLevels();
        int maxPossibleStars = maxLevels * 3;

        binding.tvMainStars.setText(totalStars + "/" + maxPossibleStars);
        binding.btnMainPlay.setText("▶ LVL " + currentLevel);
        binding.tvPlayerRank.setText("Level " + currentLevel + " Popper");
        binding.tvModeLevelStatus.setText("Level " + currentLevel + " • " + maxLevels + " Epic Levels");
        binding.tvAdventureStarsPill.setText("⭐ " + totalStars + "/" + maxPossibleStars + " Stars");

        // Endless High Score
        int endlessHigh = prefs.getEndlessHighScore();
        if (endlessHigh > 0) {
            binding.tvEndlessHighScore.setText("🏆 Best: " + String.format("%,d", endlessHigh) + " pts");
        } else {
            binding.tvEndlessHighScore.setText("🏆 Best: 0 pts • Survival");
        }

        // Star chest progress (10 stars threshold)
        int chestStars = Math.min(10, totalStars);
        binding.pbChest.setProgress(chestStars);
        binding.tvChestProgressNum.setText(chestStars + "/10 ⭐");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding.homeBackgroundView != null) {
            binding.homeBackgroundView.pauseAnimation();
        }
    }
}