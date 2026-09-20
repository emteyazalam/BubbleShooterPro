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
import com.redcodersgroup.bubbleshooter.databinding.DialogStarChestBinding;

public class StarChestDialog extends Dialog {

    private final int currentStars;
    private final int targetStars;
    private final SoundManager soundManager;
    private DialogStarChestBinding binding;

    public StarChestDialog(@NonNull Context context, int currentStars, int targetStars) {
        super(context);
        this.currentStars = currentStars;
        this.targetStars = targetStars;
        this.soundManager = SoundManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogStarChestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        binding.tvChestProgress.setText(currentStars + " / " + targetStars);
        if (currentStars >= targetStars) {
            binding.tvChestSubtitle.setText("🎉 Target Reached!\nClaim your star reward chest!");
        } else {
            int needed = targetStars - currentStars;
            binding.tvChestSubtitle.setText("Collect " + needed + " more stars from levels\nto open!");
        }

        binding.btnChestOk.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
        });

        binding.btnCloseChest.setOnClickListener(v -> {
            soundManager.playClick();
            dismiss();
        });
    }
}
