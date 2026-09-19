package com.redcodersgroup.bubbleshooter.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Base activity providing true edge-to-edge immersive sticky full-screen mode.
 * The status bar and navigation bar are hidden with zero blank reserved space,
 * expanding content to fill the full screen and allowing transient swipe from edges.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow content to draw full-bleed under notch/cutout with zero reserved blank space
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Disable fitsSystemWindows reservation so decor view fills entire screen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        enableImmersiveStickyMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveStickyMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveStickyMode();
        }
    }

    protected void enableImmersiveStickyMode() {
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            // Show transient bars on swipe from edges without resizing the layout
            insetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            // Hide both status bar and navigation bar completely
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }
}
