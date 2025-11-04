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
        setContentView(R.layout.activity_main);

        // TODO: 메인 화면 초기화 작업 여기서 진행
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
