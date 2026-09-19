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
    private DialogGameOverBinding binding;

    public GameOverDialog(@NonNull Context context, int score, GameOverDialogListener listener) {
        super(context);
        this.score = score;
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

        binding.tvLoseScore.setText("FINAL SCORE: " + String.format("%,d", score));

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
