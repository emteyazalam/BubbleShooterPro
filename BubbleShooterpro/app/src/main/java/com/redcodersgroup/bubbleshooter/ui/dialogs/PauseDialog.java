package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogPauseBinding;

public class PauseDialog extends Dialog {

    public interface PauseDialogListener {
        void onResumeClicked();
        void onRestartClicked();
        void onLevelsClicked();
        void onHomeClicked();
    }

    private final PauseDialogListener listener;
    private final boolean isEndlessMode;
    private final PreferencesManager prefs;
    private final SoundManager soundManager;
    private DialogPauseBinding binding;

    public PauseDialog(@NonNull Context context, PauseDialogListener listener) {
        this(context, false, listener);
    }

    public PauseDialog(@NonNull Context context, boolean isEndlessMode, PauseDialogListener listener) {
        super(context);
        this.isEndlessMode = isEndlessMode;
        this.listener = listener;
        this.prefs = new PreferencesManager(context);
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
        }

        if (isEndlessMode) {
            binding.btnLevels.setVisibility(android.view.View.GONE);
            // Remove extra top margin so restart button sits nicely below the top controls
            if (binding.btnRestart.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) binding.btnRestart.getLayoutParams();
                params.topMargin = 0;
                binding.btnRestart.setLayoutParams(params);
            }
        } else {
            binding.btnLevels.setVisibility(android.view.View.VISIBLE);
        }

        // Top Horizontal: Sound, Haptic, Home
        updateSoundButton(binding.btnToggleSound);
        binding.btnToggleSound.setOnClickListener(v -> {
            soundManager.playClick();
            boolean current = prefs.isSoundEnabled();
            prefs.setSoundEnabled(!current);
            updateSoundButton(binding.btnToggleSound);
        });

        updateHapticButton(binding.btnToggleHaptic);
        binding.btnToggleHaptic.setOnClickListener(v -> {
            soundManager.playClick();
            boolean current = prefs.isHapticEnabled();
            prefs.setHapticEnabled(!current);
            updateHapticButton(binding.btnToggleHaptic);
        });

        binding.btnHome.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onHomeClicked();
        });

        // Vertical Buttons (Top to Down: Levels, Restart, Resume)
        binding.btnLevels.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onLevelsClicked();
        });

        binding.btnRestart.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onRestartClicked();
        });

        binding.btnResume.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
            if (listener != null) listener.onResumeClicked();
        });
    }

    private void updateSoundButton(ImageButton btn) {
        if (prefs.isSoundEnabled()) {
            btn.setImageResource(R.drawable.btn_sound_green);
            btn.setAlpha(1.0f);
        } else {
            btn.setImageResource(R.drawable.btn_sound_gray);
            btn.setAlpha(0.65f);
        }
    }

    private void updateHapticButton(ImageButton btn) {
        if (prefs.isHapticEnabled()) {
            btn.setImageResource(R.drawable.btn_vibration_yellow);
            btn.setAlpha(1.0f);
        } else {
            btn.setImageResource(R.drawable.btn_vibration_gray);
            btn.setAlpha(0.65f);
        }
    }
}
