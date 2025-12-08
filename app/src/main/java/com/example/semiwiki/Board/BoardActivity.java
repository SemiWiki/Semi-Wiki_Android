package com.example.semiwiki.Board;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.semiwiki.Drawer.MyLikesActivity;
import com.example.semiwiki.Drawer.MyPageDTO;
import com.example.semiwiki.Drawer.MyPostsActivity;
import com.example.semiwiki.Drawer.UserService;
import com.example.semiwiki.Login.AuthService;
import com.example.semiwiki.Login.LoginActivity;
import com.example.semiwiki.Login.RetrofitInstance;
import com.example.semiwiki.R;
import com.example.semiwiki.common.HeaderView;
import com.example.semiwiki.databinding.ActivityBoardBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BoardActivity extends AppCompatActivity {

    private ActivityBoardBinding binding;
    private BoardAdapter adapter;
    private HeaderView header;

    private static final String PREF = "semiwiki_prefs";
    private static final String KEY_AT = "access_token";
    private static final String KEY_ID = "account_id";

    private String currentKeyword = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String token = prefs.getString(KEY_AT, null);
        if (token == null || token.isEmpty()) {
            goLoginAndFinish();
            return;
        }

        binding = ActivityBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ConstraintLayout headerContainer = findViewById(R.id.constraint_layout_up);
        header = new HeaderView(this);
        header.bindWithoutXml(
                headerContainer,
                binding.ivMenu,
                binding.ivLogo,
                binding.ivSearch
        );
        header.bindDrawer(binding.drawerLayout, binding.navView, new HeaderView.DrawerActions() {
            @Override public void onClickMyPosts() { startActivity(new Intent(BoardActivity.this, MyPostsActivity.class)); }
            @Override public void onClickMyLikes() { startActivity(new Intent(BoardActivity.this, MyLikesActivity.class)); }
            @Override public void onClickLogout() {
                SharedPreferences p = getSharedPreferences(PREF, MODE_PRIVATE);
                String accountId = p.getString(KEY_ID, null);
                AuthService auth = RetrofitInstance.getAuthService();
                if (accountId != null) {
                    auth.logout(accountId).enqueue(new retrofit2.Callback<Void>() {
                        @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> resp) { }
                        @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) { }
                    });
                }
                p.edit().remove(KEY_AT).remove("refresh_token").remove(KEY_ID).apply();
                goLoginAndFinish();
            }
        });
        header.setListener(new HeaderView.Listener() {
            @Override public void onSearchSubmit(String keyword) { performSearch(keyword); }
            @Override public void onSearchCancel() { clearSearchAndReload(); }
        });


        binding.ivMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BoardAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(new DividerDecoration(this, 0xFF757575, 1f, 0f, 0f));

        adapter.setOnItemClickListener((item, position) -> {
            Intent i = new Intent(this, PostDetailActivity.class);
            i.putExtra(PostDetailActivity.EXTRA_BOARD_ID, item.getId());
            startActivity(i);
        });

        setupTabs();
        initDrawerHeaderTexts();
        fillHeaderFromApiAndUpdateDrawer();

        String initialKeyword = getIntent().getStringExtra("keyword");
        if (initialKeyword != null && !initialKeyword.trim().isEmpty()) {
            binding.ivSearch.post(() -> binding.ivSearch.performClick());
            performSearch(initialKeyword.trim());
        } else {
            loadBoardListFromApi(getCurrentOrderBy());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String token = prefs.getString(KEY_AT, null);
        if (token == null || token.isEmpty()) {
            goLoginAndFinish();
        }
    }

    @Override
    public void onBackPressed() {
        if (header != null && header.isInSearchMode()) {
            header.exitSearchMode();
            return;
        }
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void clearSearchAndReload() {
        currentKeyword = null;
        binding.tabNewest.setSelected(true);
        binding.tabLikes.setSelected(false);
        loadBoardListFromApi("recent");
    }



    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            binding.tabNewest.setSelected(false);
            binding.tabLikes.setSelected(false);
            v.setSelected(true);
            if (v.getId() == R.id.tab_newest) {
                loadBoardListFromApi("recent");
            } else {
                loadBoardListFromApi("like");
            }
        };
        binding.tabNewest.setOnClickListener(tabClick);
        binding.tabLikes.setOnClickListener(tabClick);
        binding.tabNewest.setSelected(true);
    }



    private void initDrawerHeaderTexts() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String accountId = prefs.getString(KEY_ID, "-");
        header.updateDrawerUser(accountId, "0");
    }

    private void fillHeaderFromApiAndUpdateDrawer() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String token = prefs.getString(KEY_AT, null);
        String accountId = prefs.getString(KEY_ID, null);
        if (token == null || accountId == null) { handleAuthError(); return; }

        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        UserService userService = retrofit.create(UserService.class);
        userService.getMyPage("Bearer " + token, accountId)
                .enqueue(new Callback<MyPageDTO>() {
                    @Override public void onResponse(Call<MyPageDTO> call, Response<MyPageDTO> resp) {
                        if (resp.code() == 401 || resp.code() == 403) { handleAuthError(); return; }
                        if (resp.isSuccessful() && resp.body() != null) {
                            header.updateDrawerUser(resp.body().getAccountId(),
                                    String.valueOf(resp.body().getNoticeBoardCount()));
                        }
                    }
                    @Override public void onFailure(Call<MyPageDTO> call, Throwable t) { }
                });
    }

        private void loadBoardListFromApi(String orderBy) {
            Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
            BoardService service = retrofit.create(BoardService.class);

            SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
            String token = prefs.getString(KEY_AT, null);
            if (token == null || token.isEmpty()) { handleAuthError(); return; }

            service.getBoardList("Bearer " + token, currentKeyword, null, orderBy, 0, 20)
                    .enqueue(new Callback<List<BoardListItemDTO>>() {
                    @Override
                    public void onResponse(Call<List<BoardListItemDTO>> call,
                                           Response<List<BoardListItemDTO>> response) {
                        if (response.code() == 401 || response.code() == 403) {
                            handleAuthError();
                            return;
                        }

                        boolean isSearching = currentKeyword != null && !currentKeyword.isEmpty();

                        if (response.code() == 204) {
                            adapter.submitList(new ArrayList<>());
                            if (isSearching) {
                                showEmpty();
                            } else {
                                showList();
                            }
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<BoardListItemDTO> body = response.body();

                            if (body.isEmpty()) {
                                adapter.submitList(new ArrayList<>());
                                if (isSearching) {
                                    showEmpty();
                                } else {
                                    showList();
                                }
                                return;
                            }
                            List<BoardItem> uiList = BoardMappers.toBoardItems(body);
                            adapter.submitList(uiList);
                            showList();
                        } else {
                            Log.e("BoardActivity", "list fail: " + response.code());
                            adapter.submitList(new ArrayList<>());
                            if (isSearching) {
                                showEmpty();
                            } else {
                                showList();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BoardListItemDTO>> call, Throwable t) {
                        boolean isSearching = currentKeyword != null && !currentKeyword.isEmpty();
                        adapter.submitList(new ArrayList<>());
                        if (isSearching) {
                            showEmpty();
                        } else {
                            showList();
                        }
                    }
                });

    }

    private String getCurrentOrderBy() {
        return binding.tabNewest.isSelected() ? "recent" : "like";
    }


    private void performSearch(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            clearSearchAndReload();
            return;
        }
        currentKeyword = trimmed;
        loadBoardListFromApi(getCurrentOrderBy());
    }


    private void showEmpty() {
        binding.viewEmpty.getRoot().setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.tabGroup.setVisibility(View.GONE);
        binding.linearLayoutCard.setVisibility(View.GONE);
        binding.tvBoardText.setVisibility(View.GONE);
    }

    private void showList() {
        binding.viewEmpty.getRoot().setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.tabGroup.setVisibility(View.VISIBLE);
        binding.linearLayoutCard.setVisibility(View.VISIBLE);
        binding.tvBoardText.setVisibility(View.VISIBLE);
    }


    private void handleAuthError() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        prefs.edit().remove(KEY_AT).remove("refresh_token").remove(KEY_ID).apply();
        goLoginAndFinish();
    }

    private void goLoginAndFinish() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
