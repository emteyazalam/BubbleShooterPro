package com.redcodersgroup.bubbleshooter;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.data.WorldConfigManager;
import com.redcodersgroup.bubbleshooter.data.WorldConfigManager.WorldModel;
import com.redcodersgroup.bubbleshooter.databinding.ActivityMainBinding;
import com.redcodersgroup.bubbleshooter.databinding.DialogLevelPreviewBinding;
import com.redcodersgroup.bubbleshooter.level.Level;
import com.redcodersgroup.bubbleshooter.level.LevelManager;
import com.redcodersgroup.bubbleshooter.ui.GameActivity;
import com.redcodersgroup.bubbleshooter.ui.adapter.WorldMapPagerAdapter;
import com.redcodersgroup.bubbleshooter.ui.dialogs.ProfileDialog;
import com.redcodersgroup.bubbleshooter.ui.dialogs.SettingsDialog;
import com.redcodersgroup.bubbleshooter.ui.dialogs.StarChestDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;
    private SoundManager soundManager;
    private WorldConfigManager worldConfigManager;
    private WorldMapPagerAdapter worldMapAdapter;

    private SettingsDialog settingsDialog;
    private ProfileDialog profileDialog;
    private StarChestDialog starChestDialog;
    private Dialog activePreviewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = ProgressRepository.getInstance(this);
        prefs = repository.getPreferences();
        levelManager = LevelManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);
        worldConfigManager = WorldConfigManager.getInstance(this);

        initViews();
    }

    private void initViews() {
        // 1. Settings Dialog
        binding.btnMainSettings.setOnClickListener(v -> {
            soundManager.playClick();
            if (settingsDialog != null && settingsDialog.isShowing()) {
                settingsDialog.dismiss();
            }
            settingsDialog = new SettingsDialog(this);
            settingsDialog.show();
        });

        // 2. Profile Avatar Dialog
        binding.layoutPlayerProfile.setOnClickListener(v -> {
            soundManager.playClick();
            if (profileDialog != null && profileDialog.isShowing()) {
                profileDialog.dismiss();
            }
            profileDialog = new ProfileDialog(this, "Player", name -> {
                Toast.makeText(MainActivity.this, "Profile updated: " + name, Toast.LENGTH_SHORT).show();
            });
            profileDialog.show();
        });

        // 3. Star Chest Dialog
        binding.layoutStarChestBadge.setOnClickListener(v -> {
            soundManager.playClick();
            if (starChestDialog != null && starChestDialog.isShowing()) {
                starChestDialog.dismiss();
            }
            int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
            int chestTarget = 20;
            starChestDialog = new StarChestDialog(this, totalStars % chestTarget, chestTarget);
            starChestDialog.show();
        });

        // 4. Endless Mode Floating Button
        binding.btnHomeEndlessMode.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(GameActivity.createEndlessIntent(this));
        });

        // 5. Floating Play Button on Bottom Right
        binding.btnFloatingPlay.setOnClickListener(v -> {
            soundManager.playClick();
            int currentLevel = prefs.getHighestUnlockedLevel();
            showLevelPreviewDialog(currentLevel);
        });

        // 6. Coins & Lives Clickables
        binding.layoutCoinsCounter.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "★ Coins Balance: " + (prefs.getHighestUnlockedLevel() * 50) + " Coins", Toast.LENGTH_SHORT).show();
        });

        binding.layoutLivesCounter.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "❤️ Lives: FULL (5/5)", Toast.LENGTH_SHORT).show();
        });

        // 7. World Map ViewPager2 setup
        worldMapAdapter = new WorldMapPagerAdapter(this, new WorldMapPagerAdapter.OnWorldInteractionListener() {
            @Override
            public void onLevelSelected(int levelNumber) {
                showLevelPreviewDialog(levelNumber);
            }

            @Override
            public void onWorldGiftClaimed(int worldNumber, int giftIndex, int bonusCoins) {
                soundManager.playWin();
                Toast.makeText(MainActivity.this, "🎁 Mystery Gift Unlocked! +" + bonusCoins + " Coins!", Toast.LENGTH_LONG).show();
                int currentLevel = prefs.getHighestUnlockedLevel();
                binding.tvHomeCoins.setText(String.valueOf(currentLevel * 50 + bonusCoins));
            }
        });
        binding.viewPagerWorldMaps.setAdapter(worldMapAdapter);
        // Start at World 1 (which is the LAST page in reversed order)
        int initialPagerPos = worldMapAdapter.toPagerPosition(0);
        binding.viewPagerWorldMaps.setCurrentItem(initialPagerPos, false);

        binding.viewPagerWorldMaps.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Convert reversed pager position back to world index for UI
                int worldIndex = worldMapAdapter.toWorldIndex(position);
                updateWorldSwitcherUI(worldIndex);
            }
        });

        // Initial world title update
        updateWorldSwitcherUI(0);
    }

    private void updateWorldSwitcherUI(int worldIndex) {
        WorldModel world = worldConfigManager.getWorldByIndex(worldIndex);
        if (world != null && binding.tvCurrentWorldTitle != null) {
            binding.tvCurrentWorldTitle.setText(world.subtitle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentLevel = prefs.getHighestUnlockedLevel();
        int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());

        binding.tvFloatingLevelNumber.setText(String.valueOf(currentLevel));
        binding.tvStarChestProgress.setText((totalStars % 20) + "/20");
        binding.tvHomeCoins.setText(String.valueOf(currentLevel * 50));

        int targetWorldIndex = worldConfigManager.getWorldIndexForLevel(currentLevel);
        int pagerPos = worldMapAdapter.toPagerPosition(targetWorldIndex);
        android.util.Log.d("MainActivity", "onResume: highestUnlockedLevel=" + currentLevel + " -> worldIndex=" + targetWorldIndex + " -> pagerPos=" + pagerPos);
        binding.viewPagerWorldMaps.setCurrentItem(pagerPos, false);
        updateWorldSwitcherUI(targetWorldIndex);

        if (worldMapAdapter != null) {
            worldMapAdapter.notifyDataSetChanged();
        }

        int endlessHigh = prefs.getEndlessHighScore();
        if (endlessHigh > 0) {
            binding.tvEndlessBestTag.setText("🏆 Best: " + String.format(java.util.Locale.getDefault(), "%,d", endlessHigh));
        } else {
            binding.tvEndlessBestTag.setText("🏆 Best: 0");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingsDialog != null && settingsDialog.isShowing()) settingsDialog.dismiss();
        if (profileDialog != null && profileDialog.isShowing()) profileDialog.dismiss();
        if (starChestDialog != null && starChestDialog.isShowing()) starChestDialog.dismiss();
        if (activePreviewDialog != null && activePreviewDialog.isShowing()) activePreviewDialog.dismiss();
    }

    private void showLevelPreviewDialog(int level) {
        if (isFinishing() || isDestroyed()) return;
        if (activePreviewDialog != null && activePreviewDialog.isShowing()) {
            activePreviewDialog.dismiss();
        }

        soundManager.playClick();
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogLevelPreviewBinding previewBinding = DialogLevelPreviewBinding.inflate(getLayoutInflater());
        dialog.setContentView(previewBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        WorldModel world = worldConfigManager.getWorldForLevel(level);
        previewBinding.tvPreviewWorld.setText(world.subtitle);
        previewBinding.tvPreviewLevel.setText("LEVEL " + level);

        int stars = prefs.getStarsForLevel(level);
        previewBinding.ivPreviewStar1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);



        previewBinding.btnToggleBoosterRainbow.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "★ Rainbow Booster Ready", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterFireball.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "🔥 Fireball Booster Ready", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterLightning.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "⚡ Lightning Booster Ready", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterBomb.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "💣 Bomb Booster Ready", Toast.LENGTH_SHORT).show();
        });

        previewBinding.btnStartLevel.setOnClickListener(v -> {
            soundManager.playClick();
            dialog.dismiss();
            activePreviewDialog = null;
            startActivity(GameActivity.createIntent(MainActivity.this, level));
        });

        previewBinding.btnClosePreview.setOnClickListener(v -> {
            soundManager.playClick();
            dialog.dismiss();
            activePreviewDialog = null;
        });

        activePreviewDialog = dialog;
        dialog.show();
    }
}