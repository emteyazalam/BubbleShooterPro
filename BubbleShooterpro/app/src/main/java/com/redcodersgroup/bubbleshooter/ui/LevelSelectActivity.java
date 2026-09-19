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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.databinding.ActivityLevelSelectBinding;
import com.redcodersgroup.bubbleshooter.databinding.DialogLevelPreviewBinding;
import com.redcodersgroup.bubbleshooter.databinding.ItemLevelGridBinding;
import com.redcodersgroup.bubbleshooter.level.LevelManager;

public class LevelSelectActivity extends BaseActivity {

    private ActivityLevelSelectBinding binding;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;
    private SoundManager soundManager;

    private SagaLevelAdapter adapter1;
    private SagaLevelAdapter adapter2;
    private SagaLevelAdapter adapter3;
    private Dialog activePreviewDialog;

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

        initViews();
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });

        // Setup 3 Biome Grids (4 columns each)
        binding.rvLevelsWorld1.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvLevelsWorld2.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvLevelsWorld3.setLayoutManager(new GridLayoutManager(this, 4));

        adapter1 = new SagaLevelAdapter(1, 10);
        adapter2 = new SagaLevelAdapter(11, 20);
        adapter3 = new SagaLevelAdapter(21, 30);

        binding.rvLevelsWorld1.setAdapter(adapter1);
        binding.rvLevelsWorld2.setAdapter(adapter2);
        binding.rvLevelsWorld3.setAdapter(adapter3);

        updateHeader();

        // Milestone Chests
        binding.layoutChestBiome1.setOnClickListener(v -> {
            soundManager.playWin();
            Toast.makeText(this, "🎁 Biome 1 Star Chest: +150 Coins & +1 Rainbow Booster!", Toast.LENGTH_LONG).show();
        });

        binding.layoutChestBiome2.setOnClickListener(v -> {
            soundManager.playWin();
            Toast.makeText(this, "🎁 Biome 2 Star Chest: +200 Coins & +1 Bomb Booster!", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeader();
        if (adapter1 != null) adapter1.notifyDataSetChanged();
        if (adapter2 != null) adapter2.notifyDataSetChanged();
        if (adapter3 != null) adapter3.notifyDataSetChanged();
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

        String biome;
        if (level <= 10) {
            biome = "WORLD 1 • BUBBLE MEADOWS";
        } else if (level <= 20) {
            biome = "WORLD 2 • CRYSTAL CAVERNS";
        } else {
            biome = "WORLD 3 • CELESTIAL COSMOS";
        }
        previewBinding.tvPreviewWorld.setText(biome);
        previewBinding.tvPreviewLevel.setText("LEVEL " + level);

        int stars = prefs.getStarsForLevel(level);
        previewBinding.ivPreviewStar1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        previewBinding.ivPreviewStar3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);

        previewBinding.tvPreviewObjective.setText("Clear all bubbles with limited shots!");

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
