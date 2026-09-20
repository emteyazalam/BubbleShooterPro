package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.audio.MusicManager;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogSettingsBinding;

public class SettingsDialog extends Dialog {

    private final PreferencesManager prefs;
    private final SoundManager soundManager;
    private DialogSettingsBinding binding;

    public SettingsDialog(@NonNull Context context) {
        super(context);
        this.prefs = new PreferencesManager(context);
        this.soundManager = SoundManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        updateSoundUI();
        updateMusicUI();
        updateHapticUI();

        binding.layoutSoundToggle.setOnClickListener(v -> toggleSound());
        binding.btnSettingSound.setOnClickListener(v -> toggleSound());

        binding.layoutMusicToggle.setOnClickListener(v -> toggleMusic());
        binding.btnSettingMusic.setOnClickListener(v -> toggleMusic());

        binding.layoutHapticToggle.setOnClickListener(v -> toggleHaptics());
        binding.btnSettingHaptic.setOnClickListener(v -> toggleHaptics());

        binding.btnCloseSettings.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
        });
    }

    private void toggleSound() {
        boolean current = prefs.isSoundEnabled();
        prefs.setSoundEnabled(!current);
        soundManager.playClick();
        updateSoundUI();
    }

    private void toggleMusic() {
        boolean current = prefs.isMusicEnabled();
        boolean newVal = !current;
        prefs.setMusicEnabled(newVal);
        MusicManager.getInstance(getContext()).setMusicEnabled(newVal);
        soundManager.playClick();
        updateMusicUI();
    }

    private void toggleHaptics() {
        boolean current = prefs.isHapticEnabled();
        prefs.setHapticEnabled(!current);
        soundManager.playClick();
        updateHapticUI();
    }

    private void updateSoundUI() {
        if (prefs.isSoundEnabled()) {
            binding.btnSettingSound.setImageResource(R.drawable.btn_sound_green);
            binding.btnSettingSound.setAlpha(1.0f);
        } else {
            binding.btnSettingSound.setImageResource(R.drawable.btn_sound_gray);
            binding.btnSettingSound.setAlpha(0.65f);
        }
    }

    private void updateMusicUI() {
        if (prefs.isMusicEnabled()) {
            binding.btnSettingMusic.setImageResource(R.drawable.btn_music_green);
            binding.btnSettingMusic.setAlpha(1.0f);
        } else {
            binding.btnSettingMusic.setImageResource(R.drawable.btn_music_gray);
            binding.btnSettingMusic.setAlpha(0.65f);
        }
    }

    private void updateHapticUI() {
        if (prefs.isHapticEnabled()) {
            binding.btnSettingHaptic.setImageResource(R.drawable.btn_vibration_yellow);
            binding.btnSettingHaptic.setAlpha(1.0f);
        } else {
            binding.btnSettingHaptic.setImageResource(R.drawable.btn_vibration_gray);
            binding.btnSettingHaptic.setAlpha(0.65f);
        }
    }
}
