package com.redcodersgroup.bubbleshooter.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.NonNull;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.databinding.DialogLowShotsWarningBinding;

/**
 * Auto-showing and auto-hiding warning dialog displayed when the player has less than 5 shots remaining.
 * Designed without buttons to match the game's aesthetic and smoothly inform the player without blocking gameplay.
 */
public class LowShotsWarningDialog extends Dialog {

    private static final long AUTO_HIDE_DELAY_MS = 1800L;

    private final int shotsRemaining;
    private final Handler autoDismissHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoDismissRunnable = this::dismissWithAnimation;
    private DialogLowShotsWarningBinding binding;
    private boolean isDismissing = false;

    public LowShotsWarningDialog(@NonNull Context context, int shotsRemaining) {
        super(context);
        this.shotsRemaining = shotsRemaining;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogLowShotsWarningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Allow tapping outside or on the card to dismiss immediately without waiting
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Display accurate remaining shots
        String countText = (shotsRemaining == 1)
                ? "ONLY 1 SHOT LEFT!"
                : "ONLY " + shotsRemaining + " SHOTS LEFT!";
        binding.tvWarningShotsCount.setText(countText);

        // Tap anywhere on dialog to dismiss immediately
        binding.getRoot().setOnClickListener(v -> dismissWithAnimation());

        // Play feedback
        SoundManager.getInstance(getContext()).playBounce();

        // Smooth bouncy pop-in animation
        binding.getRoot().setScaleX(0.72f);
        binding.getRoot().setScaleY(0.72f);
        binding.getRoot().setAlpha(0.0f);
        binding.getRoot().animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(220)
                .setInterpolator(new OvershootInterpolator(1.25f))
                .start();

        // Auto hide after delay
        autoDismissHandler.postDelayed(autoDismissRunnable, AUTO_HIDE_DELAY_MS);
    }

    public void dismissWithAnimation() {
        if (isDismissing) return;
        isDismissing = true;
        autoDismissHandler.removeCallbacks(autoDismissRunnable);

        if (binding != null && binding.getRoot() != null) {
            binding.getRoot().animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .alpha(0.0f)
                    .setDuration(160)
                    .withEndAction(LowShotsWarningDialog.super::dismiss)
                    .start();
        } else {
            super.dismiss();
        }
    }

    @Override
    public void dismiss() {
        dismissWithAnimation();
    }

    @Override
    protected void onStop() {
        autoDismissHandler.removeCallbacks(autoDismissRunnable);
        super.onStop();
    }
}
