package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogPauseBinding;

public class PauseDialog extends Dialog {

    public interface PauseDialogListener {
        void onResumeClicked();
        void onRestartClicked();
        void onHomeClicked();
    }

    private final PauseDialogListener listener;
    private final boolean isEndlessMode;
    private final SoundManager soundManager;
    private DialogPauseBinding binding;

    public PauseDialog(@NonNull Context context, PauseDialogListener listener) {
        this(context, false, listener);
    }

    public PauseDialog(@NonNull Context context, boolean isEndlessMode, PauseDialogListener listener) {
        super(context);
        this.isEndlessMode = isEndlessMode;
        this.listener = listener;
        this.soundManager = SoundManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogPauseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        binding.btnClosePause.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onResumeClicked();
        });

        binding.btnRestart.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onRestartClicked();
        });

        binding.btnHome.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onHomeClicked();
        });

        binding.btnSettings.setOnClickListener(v -> {
            soundManager.playClick();
            new SettingsDialog(getContext()).show();
        });

        binding.btnResume.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onResumeClicked();
        });
    }
}
