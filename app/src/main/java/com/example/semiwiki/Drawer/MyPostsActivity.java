package com.example.semiwiki.Drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.semiwiki.Board.BoardActivity;
import com.example.semiwiki.Board.BoardAdapter;
import com.example.semiwiki.Board.BoardItem;
import com.example.semiwiki.Board.BoardListItemDTO;
import com.example.semiwiki.Board.BoardMappers;
import com.example.semiwiki.Board.BoardService;
import com.example.semiwiki.Board.DividerDecoration;
import com.example.semiwiki.Board.PostDetailActivity;
import com.example.semiwiki.Login.AuthService;
import com.example.semiwiki.Login.LoginActivity;
import com.example.semiwiki.Login.RetrofitInstance;
import com.example.semiwiki.R;
import com.example.semiwiki.common.HeaderView;
import com.example.semiwiki.databinding.ActivityMyPostsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPostsActivity extends AppCompatActivity {

    private static final String TAG = "MyPosts";
    private static final String PREF = "semiwiki_prefs";
    private static final String KEY_AT = "access_token";
    private static final String KEY_ID = "account_id";

    private ActivityMyPostsBinding binding;
    private BoardAdapter adapter;
    private HeaderView header;

    private String currentOrder = "recent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        header = new HeaderView(this);
        if (binding.ivSearch != null) binding.ivSearch.setVisibility(View.VISIBLE);
        header.bindWithoutXml(binding.constraintLayoutUp, binding.ivMenu, binding.ivLogo, binding.ivSearch);
        header.bindDrawer(binding.drawerLayout, binding.navView, new HeaderView.DrawerActions() {
            @Override public void onClickMyPosts() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
            @Override public void onClickMyLikes() {
                startActivity(new Intent(MyPostsActivity.this, MyLikesActivity.class));
            }
            @Override public void onClickLogout() { doLogout(); }
        });

        header.setListener(new HeaderView.Listener() {
            @Override public void onSearchSubmit(String keyword) {
                String safeKeyword = (keyword == null) ? "" : keyword.trim();

                Intent intent = new Intent(MyPostsActivity.this, BoardActivity.class);
                intent.putExtra("keyword", safeKeyword);
                startActivity(intent);
            }
            @Override
            public void onSearchCancel() {
                if (binding.tabNewest != null && binding.tabNewest.isSelected()) {
                    currentOrder = "recent";
                } else if (binding.tabLikes != null && binding.tabLikes.isSelected()) {
                    currentOrder = "like";
                }
                loadUserPosts(currentOrder);
                showList();
            }
        });

        ImageView logo = findViewById(R.id.iv_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> {
                Intent i = new Intent(this, BoardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        fetchUserAndUpdateHeader();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BoardAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(
                new DividerDecoration(this, 0xFF757575, 1f, 0f, 0f)
        );
        adapter.setOnItemClickListener((item, position) -> {
            Intent i = new Intent(this, PostDetailActivity.class);
            i.putExtra(PostDetailActivity.EXTRA_BOARD_ID, item.getId());
            startActivity(i);
        });

        setupTabs();
        if (binding.tabNewest != null) binding.tabNewest.setSelected(true);
        loadUserPosts("recent");
    }

    private void setupTabs() {
        if (binding.tabNewest == null || binding.tabLikes == null) return;

        View.OnClickListener tabClick = v -> {
            binding.tabNewest.setSelected(false);
            binding.tabLikes.setSelected(false);
            v.setSelected(true);

            if (v.getId() == R.id.tab_newest) currentOrder = "recent";
            else currentOrder = "like";

            loadUserPosts(currentOrder);
        };
        binding.tabNewest.setOnClickListener(tabClick);
        binding.tabLikes.setOnClickListener(tabClick);
    }

    private void fetchUserAndUpdateHeader() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String token = prefs.getString(KEY_AT, null);
        String accountId = prefs.getString(KEY_ID, null);
        if (token == null || accountId == null) return;

        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        UserService userService = retrofit.create(UserService.class);

        userService.getMyPage("Bearer " + token, accountId)
                .enqueue(new Callback<MyPageDTO>() {
                    @Override
                    public void onResponse(Call<MyPageDTO> call, Response<MyPageDTO> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            header.updateDrawerUser(resp.body().getAccountId(),
                                    String.valueOf(resp.body().getNoticeBoardCount()));
                        } else if (resp.code() == 401 || resp.code() == 403) {
                            handleAuthError();
                        } else {
                            Log.w(TAG, "mypage 응답코드: " + resp.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<MyPageDTO> call, Throwable t) {
                        Log.e(TAG, "mypage 실패: " + t.getMessage());
                    }
                });
    }
    private void loadUserPosts(String orderBy) {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String token = prefs.getString(KEY_AT, null);
        String accountId = prefs.getString(KEY_ID, null);
        if (token == null || accountId == null) {
            Log.e(TAG, "token/accountId 누락");
            adapter.submitList(Collections.emptyList());
            showEmpty();
            return;
        }

        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        UserService service = retrofit.create(UserService.class);

        service.getUserPosts(
                "Bearer " + token,
                accountId,
                orderBy,
                0,
                20
        ).enqueue(new Callback<List<BoardListItemDTO>>() {
            @Override
            public void onResponse(Call<List<BoardListItemDTO>> call, Response<List<BoardListItemDTO>> resp) {
                if (resp.code() == 204) {
                    adapter.submitList(Collections.emptyList());
                    showEmpty();
                    return;
                }
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.submitList(BoardMappers.toBoardItems(resp.body()));
                    showList();
                } else if (resp.code() == 401 || resp.code() == 403) {
                    handleAuthError();
                } else {
                    Log.e(TAG, "목록 실패: " + resp.code());
                    Toast.makeText(MyPostsActivity.this, "목록을 불러오지 못했어요 (" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<List<BoardListItemDTO>> call, Throwable t) {
                Log.e(TAG, "네트워크 에러: " + t.getMessage(), t);
                Toast.makeText(MyPostsActivity.this, "네트워크 오류가 발생했어요", Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        binding.viewEmpty.getRoot().setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        if (binding.tabGroup != null) binding.tabGroup.setVisibility(View.GONE);
        if (binding.linearLayoutCard != null) binding.linearLayoutCard.setVisibility(View.GONE);
        if (binding.tvBoardText != null) binding.tvBoardText.setVisibility(View.GONE);
    }

    private void showList() {
        binding.viewEmpty.getRoot().setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        if (binding.tabGroup != null) binding.tabGroup.setVisibility(View.VISIBLE);
        if (binding.linearLayoutCard != null) binding.linearLayoutCard.setVisibility(View.VISIBLE);
        if (binding.tvBoardText != null) binding.tvBoardText.setVisibility(View.VISIBLE);
    }

    private void doLogout() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        String accountId = prefs.getString(KEY_ID, null);

        try {
            AuthService auth = RetrofitInstance.getAuthService();
            if (accountId != null) {
                auth.logout(accountId).enqueue(new retrofit2.Callback<Void>() {
                    @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) { Log.d(TAG, "logout resp=" + r.code()); }
                    @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) { Log.w(TAG, "logout fail: " + t.getMessage()); }
                });
            }
        } catch (Exception ignore) {}

        prefs.edit().remove(KEY_AT).remove("refresh_token").remove(KEY_ID).apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void handleAuthError() {
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        prefs.edit().remove(KEY_AT).remove("refresh_token").remove(KEY_ID).apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
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
}
