package com.redcodersgroup.bubbleshooter.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.bubble.BubbleType;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.data.ProgressRepository;
import com.redcodersgroup.bubbleshooter.databinding.ActivityGameBinding;
import com.redcodersgroup.bubbleshooter.game.GameEngine;
import com.redcodersgroup.bubbleshooter.level.Level;
import com.redcodersgroup.bubbleshooter.level.LevelManager;
import com.redcodersgroup.bubbleshooter.ui.dialogs.GameOverDialog;
import com.redcodersgroup.bubbleshooter.ui.dialogs.PauseDialog;
import com.redcodersgroup.bubbleshooter.ui.dialogs.VictoryDialog;

public class GameActivity extends BaseActivity implements GameEngine.GameEventListener {

    public static final String EXTRA_LEVEL_NUMBER = "extra_level_number";

    private ActivityGameBinding binding;
    private GameEngine gameEngine;
    private ProgressRepository repository;
    private PreferencesManager prefs;
    private LevelManager levelManager;

    private int currentLevelNumber = 1;
    private PauseDialog activePauseDialog;
    private VictoryDialog activeVictoryDialog;
    private GameOverDialog activeGameOverDialog;
    private boolean isGameOverOrWon = false;
    private boolean wasBackgrounded = false;

    public static Intent createIntent(Context context, int levelNumber) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(EXTRA_LEVEL_NUMBER, levelNumber);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        wasBackgrounded = false;
        currentLevelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);
        repository = ProgressRepository.getInstance(this);
        prefs = repository.getPreferences();
        levelManager = LevelManager.getInstance(this);

        initViews();
        setupGame();
    }

    private void initViews() {
        binding.btnPause.setOnClickListener(v -> showPauseDialog());

        binding.btnBoosterRainbow.setOnClickListener(v -> {
            playBoosterTapFeedback(binding.layoutBoosterRainbow);
            gameEngine.equipBooster(BubbleType.RAINBOW);
            updateBoosterCounts();
        });

        binding.btnBoosterFireball.setOnClickListener(v -> {
            playBoosterTapFeedback(binding.layoutBoosterFireball);
            gameEngine.equipBooster(BubbleType.FIREBALL);
            updateBoosterCounts();
        });

        binding.btnBoosterLightning.setOnClickListener(v -> {
            playBoosterTapFeedback(binding.layoutBoosterLightning);
            gameEngine.equipBooster(BubbleType.LIGHTNING);
            updateBoosterCounts();
        });

        binding.btnBoosterBomb.setOnClickListener(v -> {
            playBoosterTapFeedback(binding.layoutBoosterBomb);
            gameEngine.equipBooster(BubbleType.BOMB);
            updateBoosterCounts();
        });

        updateBoosterCounts();
    }

    private void playBoosterTapFeedback(View view) {
        if (view == null) return;
        view.animate().cancel();
        view.setScaleX(0.80f);
        view.setScaleY(0.80f);
        view.animate()
                .scaleX(1.20f)
                .scaleY(1.20f)
                .setDuration(110)
                .withEndAction(() -> view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(90).start())
                .start();
    }

    private void updateBoosterCounts() {
        binding.tvCountRainbow.setText("∞");
        binding.tvCountFireball.setText("∞");
        binding.tvCountLightning.setText("∞");
        binding.tvCountBomb.setText("∞");
    }

    private void setupGame() {
        gameEngine = new GameEngine(this);
        gameEngine.setEventListener(this);
        binding.bubbleGameView.setGameEngine(gameEngine);

        loadCurrentLevel();
    }

    private void loadCurrentLevel() {
        isGameOverOrWon = false;
        wasBackgrounded = false;
        binding.bubbleGameView.setBiomeLevel(currentLevelNumber);
        BubbleGameView.BiomeTheme theme = binding.bubbleGameView.getCurrentBiome();
        binding.tvLevelTitle.setText("LVL " + currentLevelNumber + " • " + (theme != null ? theme.title : ""));
        Level level = levelManager.getLevel(currentLevelNumber);
        gameEngine.loadLevel(level);
    }

    private void showPauseDialog() {
        if (isFinishing() || isDestroyed() || isGameOverOrWon) return;
        if (activePauseDialog != null && activePauseDialog.isShowing()) return;

        if (gameEngine != null) {
            gameEngine.pause();
        }

        activePauseDialog = new PauseDialog(this, new PauseDialog.PauseDialogListener() {
            @Override
            public void onResumeClicked() {
                activePauseDialog = null;
                if (gameEngine != null) {
                    gameEngine.resume();
                }
                enableImmersiveStickyMode();
            }

            @Override
            public void onRestartClicked() {
                activePauseDialog = null;
                loadCurrentLevel();
                enableImmersiveStickyMode();
            }

            @Override
            public void onExitClicked() {
                activePauseDialog = null;
                Intent intent = LevelSelectActivity.createIntent(GameActivity.this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        activePauseDialog.show();
    }

    @Override
    public void onScoreUpdated(int score, int stars, float starProgress) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            binding.tvScore.setText("SCORE: " + String.format("%,d", score));
            binding.hudStar1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            binding.hudStar2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            binding.hudStar3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        });
    }

    @Override
    public void onShotsUpdated(int shotsRemaining) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            binding.tvShotsCount.setText(String.valueOf(shotsRemaining));
        });
    }

    @Override
    public void onGameWon(int score, int stars) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            isGameOverOrWon = true;
            wasBackgrounded = false;

            if (activePauseDialog != null && activePauseDialog.isShowing()) {
                activePauseDialog.dismiss();
                activePauseDialog = null;
            }

            int previousHigh = prefs.getHighScoreForLevel(currentLevelNumber);
            int newHigh = Math.max(previousHigh, score);
            repository.completeLevel(currentLevelNumber, stars, score);

            activeVictoryDialog = new VictoryDialog(this, score, newHigh, stars, new VictoryDialog.VictoryDialogListener() {
                @Override
                public void onNextLevelClicked() {
                    activeVictoryDialog = null;
                    if (currentLevelNumber < levelManager.getTotalLevels()) {
                        currentLevelNumber++;
                        loadCurrentLevel();
                    } else {
                        finish();
                    }
                    enableImmersiveStickyMode();
                }

                @Override
                public void onReplayClicked() {
                    activeVictoryDialog = null;
                    loadCurrentLevel();
                    enableImmersiveStickyMode();
                }

                @Override
                public void onHomeClicked() {
                    activeVictoryDialog = null;
                    finish();
                }
            });
            activeVictoryDialog.show();
        });
    }

    @Override
    public void onGameLost(int score) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            isGameOverOrWon = true;
            wasBackgrounded = false;

            if (activePauseDialog != null && activePauseDialog.isShowing()) {
                activePauseDialog.dismiss();
                activePauseDialog = null;
            }

            activeGameOverDialog = new GameOverDialog(this, score, new GameOverDialog.GameOverDialogListener() {
                @Override
                public void onRetryClicked() {
                    activeGameOverDialog = null;
                    loadCurrentLevel();
                    enableImmersiveStickyMode();
                }

                @Override
                public void onHomeClicked() {
                    activeGameOverDialog = null;
                    finish();
                }
            });
            activeGameOverDialog.show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null && binding.bubbleGameView != null) {
            binding.bubbleGameView.pause();
        }
        if (gameEngine != null) {
            gameEngine.pause();
            if (!isGameOverOrWon) {
                wasBackgrounded = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && binding.bubbleGameView != null) {
            binding.bubbleGameView.resume();
        }

        // Only auto-show pause dialog if the app was actively sent to background during gameplay
        if (wasBackgrounded) {
            wasBackgrounded = false;
            if (!isGameOverOrWon && gameEngine != null) {
                if (activePauseDialog == null || !activePauseDialog.isShowing()) {
                    if ((activeVictoryDialog == null || !activeVictoryDialog.isShowing()) &&
                        (activeGameOverDialog == null || !activeGameOverDialog.isShowing())) {
                        showPauseDialog();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activePauseDialog != null && activePauseDialog.isShowing()) {
            activePauseDialog.dismiss();
            activePauseDialog = null;
        }
        if (activeVictoryDialog != null && activeVictoryDialog.isShowing()) {
            activeVictoryDialog.dismiss();
            activeVictoryDialog = null;
        }
        if (activeGameOverDialog != null && activeGameOverDialog.isShowing()) {
            activeGameOverDialog.dismiss();
            activeGameOverDialog = null;
        }
    }
}
