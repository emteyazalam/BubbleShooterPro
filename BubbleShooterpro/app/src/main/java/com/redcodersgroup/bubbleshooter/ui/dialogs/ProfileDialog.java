package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogEditProfileBinding;
import com.redcodersgroup.bubbleshooter.profile.AvatarManager;
import com.redcodersgroup.bubbleshooter.ui.adapter.AvatarAdapter;

public class ProfileDialog extends Dialog {

    public interface ProfileDialogListener {
        void onProfileUpdated(String playerName, String avatarId);
    }

    private final PreferencesManager prefs;
    private final ProfileDialogListener listener;
    private final SoundManager soundManager;
    private String selectedAvatarId;
    private DialogEditProfileBinding binding;

    public ProfileDialog(@NonNull Context context, ProfileDialogListener listener) {
        super(context);
        this.prefs = new PreferencesManager(context);
        this.listener = listener;
        this.soundManager = SoundManager.getInstance(context);
        this.selectedAvatarId = prefs.getPlayerAvatar();
    }

    public ProfileDialog(@NonNull Context context, String currentName, String currentAvatarId, ProfileDialogListener listener) {
        super(context);
        this.prefs = new PreferencesManager(context);
        this.listener = listener;
        this.soundManager = SoundManager.getInstance(context);
        this.selectedAvatarId = (currentAvatarId != null && !currentAvatarId.isEmpty())
                ? currentAvatarId : prefs.getPlayerAvatar();
        if (currentName != null && !currentName.isEmpty()) {
            prefs.setPlayerName(currentName);
        }
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

        // Initialize Player Name
        String currentName = prefs.getPlayerName();
        binding.etPlayerName.setText(currentName);
        binding.etPlayerName.setSelection(currentName.length());

        // Initialize Avatar Preview
        updateAvatarPreview(selectedAvatarId);

        // Setup 4-column Grid for 8 Predefined Avatars
        binding.rvAvatarChoices.setLayoutManager(new GridLayoutManager(getContext(), 4));
        AvatarAdapter adapter = new AvatarAdapter(
                AvatarManager.getPredefinedAvatars(),
                selectedAvatarId,
                avatar -> {
                    soundManager.playClick();
                    selectedAvatarId = avatar.id;
                    updateAvatarPreview(avatar.id);
                }
        );
        binding.rvAvatarChoices.setAdapter(adapter);

        // Confirm Button
        binding.btnProfileConfirm.setOnClickListener(v -> {
            soundManager.playClick();
            String name = binding.etPlayerName.getText().toString().trim();
            if (name.isEmpty()) {
                name = AvatarManager.DEFAULT_PLAYER_NAME;
            }

            prefs.setPlayerName(name);
            prefs.setPlayerAvatar(selectedAvatarId);

            if (listener != null) {
                listener.onProfileUpdated(name, selectedAvatarId);
            }
            dismiss();
        });

        // Close Button
        binding.btnCloseProfile.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
        });
    }

    private void updateAvatarPreview(String avatarId) {
        if (binding != null && binding.ivSelectedAvatarPreview != null) {
            binding.ivSelectedAvatarPreview.setImageResource(AvatarManager.getAvatarDrawable(avatarId));
        }
    }
}
