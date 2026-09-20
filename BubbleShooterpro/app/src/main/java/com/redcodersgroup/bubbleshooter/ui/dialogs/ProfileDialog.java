package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogEditProfileBinding;

public class ProfileDialog extends Dialog {

    public interface ProfileDialogListener {
        void onProfileUpdated(String playerName);
    }

    private final String currentName;
    private final ProfileDialogListener listener;
    private final SoundManager soundManager;
    private DialogEditProfileBinding binding;

    public ProfileDialog(@NonNull Context context, String currentName, ProfileDialogListener listener) {
        super(context);
        this.currentName = (currentName != null && !currentName.isEmpty()) ? currentName : "Player";
        this.listener = listener;
        this.soundManager = SoundManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        binding.etPlayerName.setText(currentName);
        binding.etPlayerName.setSelection(currentName.length());

        binding.btnProfileConfirm.setOnClickListener(v -> {
            soundManager.playClick();
            String name = binding.etPlayerName.getText().toString().trim();
            if (name.isEmpty()) name = "Player";
            if (listener != null) listener.onProfileUpdated(name);
            dismiss();
        });

        binding.btnCloseProfile.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
        });
    }
}
