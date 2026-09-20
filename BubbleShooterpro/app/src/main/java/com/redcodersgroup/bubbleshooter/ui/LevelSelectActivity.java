package com.redcodersgroup.bubbleshooter.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.databinding.ActivityLevelSelectBinding;
import com.redcodersgroup.bubbleshooter.databinding.DialogLevelPreviewBinding;
import com.redcodersgroup.bubbleshooter.databinding.ItemBiomeCardBinding;
import com.redcodersgroup.bubbleshooter.databinding.ItemLevelGridBinding;
import com.redcodersgroup.bubbleshooter.level.Level;
import com.redcodersgroup.bubbleshooter.level.LevelManager;
import java.util.ArrayList;
import java.util.List;

public class LevelSelectActivity extends BaseActivity {

    private ActivityLevelSelectBinding binding;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;
    private SoundManager soundManager;

    private BiomeWorldAdapter biomeAdapter;
    private Dialog activePreviewDialog;

    public static class BiomeInfo {
        public final int worldNum;
        public final String worldTag;
        public final String title;
        public final int startLevel;
        public final int endLevel;
        public final int backgroundRes;
        public final String chestReward;

        public BiomeInfo(int worldNum, String worldTag, String title, int startLevel, int endLevel, int backgroundRes, String chestReward) {
            this.worldNum = worldNum;
            this.worldTag = worldTag;
            this.title = title;
            this.startLevel = startLevel;
            this.endLevel = endLevel;
            this.backgroundRes = backgroundRes;
            this.chestReward = chestReward;
        }
    }

    private final List<BiomeInfo> biomes = new ArrayList<>();

    public static Intent createIntent(Context context) {
        return new Intent(context, LevelSelectActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLevelSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = ProgressRepository.getInstance(this);
        prefs = repository.getPreferences();
        levelManager = LevelManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);

        initBiomeData();
        initViews();
    }

    private void initBiomeData() {
        biomes.clear();
        biomes.add(new BiomeInfo(1, "WORLD 1", "🌿 Bubble Meadows", 1, 30, R.drawable.bg_biome_meadows, "🎁 World 1 Chest: +150 Coins & +1 Rainbow Booster!"));
        biomes.add(new BiomeInfo(2, "WORLD 2", "💎 Crystal Caverns", 31, 60, R.drawable.bg_biome_crystals, "🎁 World 2 Chest: +200 Coins & +1 Bomb Booster!"));
        biomes.add(new BiomeInfo(3, "WORLD 3", "✨ Celestial Cosmos", 61, 90, R.drawable.bg_biome_cosmos, "🎁 World 3 Chest: +250 Coins & +1 Lightning Booster!"));
        biomes.add(new BiomeInfo(4, "WORLD 4", "🌋 Volcanic Forge", 91, 120, R.drawable.bg_biome_volcano, "🎁 World 4 Chest: +300 Coins & +1 Fireball Booster!"));
        biomes.add(new BiomeInfo(5, "WORLD 5", "⚡ Neon Cyberland", 121, 150, R.drawable.bg_biome_cyber, "🎁 World 5 Chest: +350 Coins & +2 Lightning Boosters!"));
        biomes.add(new BiomeInfo(6, "WORLD 6", "🌊 Sunken Atlantis", 151, 180, R.drawable.bg_biome_atlantis, "🎁 World 6 Chest: +400 Coins & +2 Rainbow Boosters!"));
        biomes.add(new BiomeInfo(7, "WORLD 7", "🌴 Enchanted Jungle", 181, 210, R.drawable.bg_biome_jungle, "🎁 World 7 Chest: +450 Coins & +2 Bomb Boosters!"));
        biomes.add(new BiomeInfo(8, "WORLD 8", "❄️ Frozen Glacier", 211, 240, R.drawable.bg_biome_glacier, "🎁 World 8 Chest: +500 Coins & +2 Fireball Boosters!"));
        biomes.add(new BiomeInfo(9, "WORLD 9", "🏜️ Desert Mirage", 241, 270, R.drawable.bg_biome_desert, "🎁 World 9 Chest: +600 Coins & +3 Boosters Pack!"));
        biomes.add(new BiomeInfo(10, "WORLD 10", "⚡ Thunder Peak", 271, 300, R.drawable.bg_biome_thunder, "🎁 World 10 Chest: +750 Coins & +3 Super Boosters!"));
        biomes.add(new BiomeInfo(11, "WORLD 11", "🌌 Infinity Realm", 301, 330, R.drawable.bg_biome_infinity, "🎁 Master Star Chest: +1000 Coins & Ultimate Trophy!"));
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });

        binding.rvBiomes.setLayoutManager(new LinearLayoutManager(this));
        biomeAdapter = new BiomeWorldAdapter(biomes);
        binding.rvBiomes.setAdapter(biomeAdapter);

        updateHeader();
        scrollToCurrentBiome();
    }

    private void scrollToCurrentBiome() {
        int currentLevel = prefs.getHighestUnlockedLevel();
        for (int i = 0; i < biomes.size(); i++) {
            BiomeInfo biome = biomes.get(i);
            if (currentLevel >= biome.startLevel && currentLevel <= biome.endLevel) {
                final int pos = i;
                binding.rvBiomes.post(() -> binding.rvBiomes.scrollToPosition(pos));
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeader();
        if (biomeAdapter != null) {
            biomeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activePreviewDialog != null && activePreviewDialog.isShowing()) {
            activePreviewDialog.dismiss();
            activePreviewDialog = null;
        }
    }

    private void updateHeader() {
        int totalStars = repository.getTotalStarsEarned(levelManager.getTotalLevels());
        int maxStars = levelManager.getTotalLevels() * 3;
        binding.tvTotalStars.setText(totalStars + "/" + maxStars);
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

        String biomeName = "WORLD " + ((level - 1) / 30 + 1);
        for (BiomeInfo b : biomes) {
            if (level >= b.startLevel && level <= b.endLevel) {
                biomeName = b.worldTag + " • " + b.title;
                break;
            }
        }

        previewBinding.tvPreviewWorld.setText(biomeName);
        previewBinding.tvPreviewLevel.setText("LEVEL " + level);

        int stars = prefs.getStarsForLevel(level);
        previewBinding.ivPreviewStar1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);

        Level levelData = levelManager.getLevel(level);
        if (levelData != null && levelData.getObjective() != null) {
            previewBinding.tvPreviewObjective.setText(levelData.getObjective().getInitialDescription());
        } else {
            previewBinding.tvPreviewObjective.setText("Clear all bubbles with limited shots!");
        }

        previewBinding.btnToggleBoosterRainbow.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "★ Rainbow Booster Equipped (Unlimited)", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterFireball.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "🔥 Fireball Booster Equipped (Unlimited)", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterLightning.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "⚡ Lightning Booster Equipped (Unlimited)", Toast.LENGTH_SHORT).show();
        });
        previewBinding.btnToggleBoosterBomb.setOnClickListener(v -> {
            soundManager.playClick();
            Toast.makeText(this, "💣 Bomb Booster Equipped (Unlimited)", Toast.LENGTH_SHORT).show();
        });

        previewBinding.btnStartLevel.setOnClickListener(v -> {
            soundManager.playClick();
            dialog.dismiss();
            activePreviewDialog = null;
            startActivity(GameActivity.createIntent(LevelSelectActivity.this, level));
        });

        previewBinding.btnClosePreview.setOnClickListener(v -> {
            soundManager.playClick();
            dialog.dismiss();
            activePreviewDialog = null;
        });

        activePreviewDialog = dialog;
        dialog.show();
    }

    // Outer Biome Adapter
    private class BiomeWorldAdapter extends RecyclerView.Adapter<BiomeWorldAdapter.BiomeViewHolder> {

        private final List<BiomeInfo> biomeList;

        public BiomeWorldAdapter(List<BiomeInfo> biomeList) {
            this.biomeList = biomeList;
        }

        @NonNull
        @Override
        public BiomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBiomeCardBinding cardBinding = ItemBiomeCardBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new BiomeViewHolder(cardBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull BiomeViewHolder holder, int position) {
            BiomeInfo info = biomeList.get(position);
            holder.binding.tvWorldTag.setText(info.worldTag);
            holder.binding.tvBiomeTitle.setText(info.title);
            holder.binding.layoutBiomeHeader.setBackgroundResource(info.backgroundRes);
            holder.binding.tvChestTitle.setText(info.title + " Star Chest");

            // Setup nested level grid (4 columns)
            holder.binding.rvBiomeLevels.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
            SagaLevelAdapter levelAdapter = new SagaLevelAdapter(info.startLevel, info.endLevel);
            holder.binding.rvBiomeLevels.setAdapter(levelAdapter);

            holder.binding.layoutMilestoneChest.setOnClickListener(v -> {
                soundManager.playWin();
                Toast.makeText(LevelSelectActivity.this, info.chestReward, Toast.LENGTH_LONG).show();
            });
        }

        @Override
        public int getItemCount() {
            return biomeList.size();
        }

        class BiomeViewHolder extends RecyclerView.ViewHolder {
            final ItemBiomeCardBinding binding;

            BiomeViewHolder(ItemBiomeCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    // Inner Grid Level Adapter
    private class SagaLevelAdapter extends RecyclerView.Adapter<SagaLevelAdapter.LevelViewHolder> {

        private final int startLevel;
        private final int endLevel;

        public SagaLevelAdapter(int startLevel, int endLevel) {
            this.startLevel = startLevel;
            this.endLevel = endLevel;
        }

        @NonNull
        @Override
        public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLevelGridBinding itemBinding = ItemLevelGridBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new LevelViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
            int level = startLevel + position;
            boolean unlocked = repository.isLevelUnlocked(level);
            int stars = prefs.getStarsForLevel(level);
            int highestUnlocked = prefs.getHighestUnlockedLevel();
            boolean isCurrent = (level == highestUnlocked);

            if (unlocked) {
                holder.binding.ivLock.setVisibility(View.GONE);
                holder.binding.tvLevelNumber.setVisibility(View.VISIBLE);
                holder.binding.tvLevelNumber.setText(String.valueOf(level));
                holder.binding.layoutBadge.setBackgroundResource(
                        stars > 0 ? R.drawable.btn_orb_blank_green : R.drawable.btn_orb_blank_yellow
                );
                holder.binding.tvHereTag.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
            } else {
                holder.binding.ivLock.setVisibility(View.GONE);
                holder.binding.tvLevelNumber.setVisibility(View.GONE);
                holder.binding.layoutBadge.setBackgroundResource(R.drawable.btn_lock_gray);
                holder.binding.tvHereTag.setVisibility(View.GONE);
            }

            holder.binding.ivStar1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            holder.binding.ivStar2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            holder.binding.ivStar3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);

            holder.itemView.setOnClickListener(v -> {
                if (unlocked) {
                    showLevelPreviewDialog(level);
                } else {
                    Toast.makeText(LevelSelectActivity.this, "Complete Level " + (level - 1) + " to unlock!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return (endLevel - startLevel) + 1;
        }

        class LevelViewHolder extends RecyclerView.ViewHolder {
            final ItemLevelGridBinding binding;

            LevelViewHolder(ItemLevelGridBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
