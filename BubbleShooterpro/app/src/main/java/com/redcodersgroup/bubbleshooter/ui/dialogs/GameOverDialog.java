package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.databinding.DialogGameOverBinding;

public class GameOverDialog extends Dialog {

    public interface GameOverDialogListener {
        void onRetryClicked();
        void onHomeClicked();
    }

    private final GameOverDialogListener listener;
    private final int score;
    private final String reason;
    private final int highScore;
    private final boolean isEndless;
    private DialogGameOverBinding binding;

    public GameOverDialog(@NonNull Context context, int score, GameOverDialogListener listener) {
        this(context, score, "Out of shots! Don't give up!", 0, false, listener);
    }

    public GameOverDialog(@NonNull Context context, int score, String reason, GameOverDialogListener listener) {
        this(context, score, reason, 0, false, listener);
    }

    public GameOverDialog(@NonNull Context context, int score, String reason, int highScore, boolean isEndless, GameOverDialogListener listener) {
        super(context);
        this.score = score;
        this.reason = reason;
        this.highScore = highScore;
        this.isEndless = isEndless;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogGameOverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (binding.tvLoseSubtitle != null) {
            if (isEndless && score > highScore && highScore > 0) {
                binding.tvLoseSubtitle.setText("🎉 NEW BEST HIGH SCORE! 🎉");
            } else if (reason != null && !reason.isEmpty()) {
                binding.tvLoseSubtitle.setText(reason);
            }
        }

        if (isEndless) {
            int displayBest = Math.max(score, highScore);
            binding.tvLoseScore.setText("SCORE: " + String.format("%,d", score) + "\nBEST: " + String.format("%,d", displayBest));
        } else {
            binding.tvLoseScore.setText("FINAL SCORE: " + String.format("%,d", score));
        }

        binding.btnLoseRetry.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onRetryClicked();
        });

        binding.btnLoseHome.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onHomeClicked();
        });
    }
}
