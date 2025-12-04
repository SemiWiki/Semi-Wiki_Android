package com.example.semiwiki.Search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.semiwiki.R;

public class SearchActivity extends AppCompatActivity {
    private EditText etKeyword;
    private ImageView ivClear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etKeyword = findViewById(R.id.et_keyword);
        ivClear = findViewById(R.id.iv_clear);

        ivClear.setVisibility(android.view.View.GONE);

        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(s.length() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        ivClear.setOnClickListener(v->etKeyword.setText(""));

        etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                moveToBoardList();
                return true;
            }
            return false;
        });
    }

    private void moveToBoardList() {
        String keyword = etKeyword.getText().toString();
        Intent intent = new Intent(SearchActivity.this, com.example.semiwiki.Board.BoardActivity.class);
        intent.putExtra("keyword", keyword);
        startActivity(intent);
        finish();
    }
}