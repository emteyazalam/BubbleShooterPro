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
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogPauseBinding;

public class PauseDialog extends Dialog {

    public interface PauseDialogListener {
        void onResumeClicked();
        void onRestartClicked();
        void onExitClicked();
    }

    private final PauseDialogListener listener;
    private final PreferencesManager prefs;
    private DialogPauseBinding binding;

    public PauseDialog(@NonNull Context context, PauseDialogListener listener) {
        super(context);
        this.listener = listener;
        this.prefs = new PreferencesManager(context);
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

        binding.btnResume.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onResumeClicked();
        });

        binding.btnRestart.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onRestartClicked();
        });

        binding.btnExit.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onExitClicked();
        });

        updateSoundButton(binding.btnToggleSound);
        binding.btnToggleSound.setOnClickListener(v -> {
            boolean current = prefs.isSoundEnabled();
            prefs.setSoundEnabled(!current);
            updateSoundButton(binding.btnToggleSound);
        });

        updateHapticButton(binding.btnToggleHaptic);
        binding.btnToggleHaptic.setOnClickListener(v -> {
            boolean current = prefs.isHapticEnabled();
            prefs.setHapticEnabled(!current);
            updateHapticButton(binding.btnToggleHaptic);
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
