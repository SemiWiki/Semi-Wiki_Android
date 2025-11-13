package com.example.semiwiki;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.semiwiki.Login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getAccessToken();
        if (token == null || token.isEmpty()) {

            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }

    @Nullable
    private String getAccessToken() {
        String token = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE)
                .getString("access_token", null);


        if (token != null && token.trim().isEmpty()) {
            return null;
        }
        return token;
    }
}
