package com.example.semiwiki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.semiwiki.Board.BoardActivity;
import com.example.semiwiki.Login.LoginActivity;
import com.example.semiwiki.Login.RetrofitInstance;
import com.example.semiwiki.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences pref = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
            String token = pref.getString("access_token", null);

            if (token != null && !token.isEmpty()) {
                RetrofitInstance.setAccessToken(token);
                startActivity(new Intent(this, BoardActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
        }, 800);
    }
}
