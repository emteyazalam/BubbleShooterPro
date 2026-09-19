package com.redcodersgroup.bubbleshooter.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import com.redcodersgroup.bubbleshooter.MainActivity;
import com.redcodersgroup.bubbleshooter.databinding.ActivitySplashBinding;
import com.redcodersgroup.bubbleshooter.ui.BaseActivity;

public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.splashAnimationView.setOnSplashFinishedListener(() -> {
            if (!isNavigated) {
                isNavigated = true;
                runOnUiThread(this::navigateToHome);
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
