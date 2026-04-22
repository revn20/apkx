package com.fbsharebx.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applySavedTheme(this);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splashLogo);
        logo.setAlpha(0f);
        logo.setScaleX(0.7f);
        logo.setScaleY(0.7f);
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 5000);
    }
}
