package com.redcodersgroup.bubbleshooter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
        // Play: launches the latest unlocked level directly
        binding.btnMainPlay.setOnClickListener(v -> {
            soundManager.playClick();
            int currentLevel = prefs.getHighestUnlockedLevel();
            startActivity(GameActivity.createIntent(this, currentLevel));
        });

        // Levels: launches World Saga Map
        binding.btnMainLevels.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(LevelSelectActivity.createIntent(this));
        });

        binding.btnMainTrophy.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(LevelSelectActivity.createIntent(this));
        });

        binding.layoutStarChest.setOnClickListener(v -> {
            soundManager.playClick();
            int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
            if (totalStars >= 10) {
                Toast.makeText(this, "🎁 Chest Opened! +1 Rainbow & +1 Bomb Booster!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Earn " + (10 - totalStars) + " more stars to unlock this chest!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMainGift.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "🎁 Daily Gift: +100 Coins & +1 Rainbow Booster!", Toast.LENGTH_LONG).show();
        });

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

    private void startTitleAnimation() {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(binding.titleContainer, "translationY", 0f, -14f, 0f);
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
        int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
        int currentLevel = prefs.getHighestUnlockedLevel();
        int maxLevels = levelManager.getTotalLevels();

        binding.tvMainStars.setText(totalStars + "/" + (maxLevels * 3));
        binding.btnMainPlay.setText("▶ PLAY • LVL " + currentLevel);
        binding.tvPlayerRank.setText("Level " + currentLevel + " Popper");

        // Star chest progress (10 stars threshold)
        int chestStars = Math.min(10, totalStars);
        binding.pbChest.setProgress(chestStars);
        if (totalStars >= 10) {
            binding.tvChestProgress.setText("Chest Ready to Open! Tap here! 🎁");
        } else {
            binding.tvChestProgress.setText("Earn " + (10 - totalStars) + " more stars to unlock!");
        }
    }
}