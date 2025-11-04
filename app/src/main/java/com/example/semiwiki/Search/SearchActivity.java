package com.example.semiwiki.Search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semiwiki.Board.BoardAdapter;
import com.example.semiwiki.Board.BoardItem;
import com.example.semiwiki.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etKeyword;
    private ImageView ivClear;
    private RecyclerView rvSearchResults;
    private View viewEmpty;

    private BoardAdapter adapter;

    private final List<BoardItem> originList = new ArrayList<>();
    private final List<BoardItem> shownList  = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etKeyword = findViewById(R.id.et_keyword);
        ivClear = findViewById(R.id.iv_clear);
        rvSearchResults = findViewById(R.id.rv_search_results);
        viewEmpty = findViewById(R.id.view_empty);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BoardAdapter();
        rvSearchResults.setAdapter(adapter);

        loadDummy();
        applyFilter("");

        ivClear.setOnClickListener(v -> etKeyword.setText(""));

        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilter(String.valueOf(s));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilter(etKeyword.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void applyFilter(String q) {
        shownList.clear();

        if (q == null || q.trim().isEmpty()) {
            shownList.addAll(originList);
        } else {
            String key = q.trim().toLowerCase();
            for (BoardItem it : originList) {
                if (it.getTitle().toLowerCase().contains(key)) {
                    shownList.add(it);
                }
            }
        }
        adapter.submitList(new ArrayList<>(shownList));

        boolean hasData = !shownList.isEmpty();
        rvSearchResults.setVisibility(hasData ? View.VISIBLE : View.GONE);
        viewEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }

    private void loadDummy() {
        List<String> cats = Arrays.asList("전공", "기숙사", "논란");
        for (int i = 0; i < 10; i++) {
            originList.add(new BoardItem("틀 현역 유럽파 축구 선수 +" + (i + 1), "wjddlfdnd", cats));
        }
    }
}
