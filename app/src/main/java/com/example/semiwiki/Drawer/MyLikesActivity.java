package com.example.semiwiki.Drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.semiwiki.Board.BoardAdapter;
import com.example.semiwiki.Board.BoardActivity;
import com.example.semiwiki.Board.BoardListItemDTO;
import com.example.semiwiki.Board.BoardMappers;
import com.example.semiwiki.Board.DividerDecoration;
import com.example.semiwiki.Board.PostDetailActivity;
import com.example.semiwiki.Login.LoginActivity;
import com.example.semiwiki.Login.RetrofitInstance;
import com.example.semiwiki.R;
import com.example.semiwiki.databinding.ActivityMyLikesBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.content.SharedPreferences;
import android.content.Intent;
import com.example.semiwiki.Login.LoginActivity;
import com.example.semiwiki.Login.AuthService;
import com.example.semiwiki.Login.RetrofitInstance;

public class MyLikesActivity extends AppCompatActivity {

    private static final String TAG = "MyLikes";

    private ActivityMyLikesBinding binding;
    private BoardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyLikesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView logo = findViewById(R.id.iv_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> {
                Intent i = new Intent(this, BoardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        binding.ivMenu.setOnClickListener(v ->
                binding.drawerLayout.openDrawer(GravityCompat.START)
        );

        setupUserDrawerHeader();

        View tabGroup = findViewById(R.id.tab_group);
        if (tabGroup != null) tabGroup.setVisibility(View.GONE);

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

        loadMyLikedPosts();
    }

    private void setupUserDrawerHeader() {
        View header = binding.navView.getHeaderView(0);
        if (header == null) header = binding.navView.inflateHeaderView(R.layout.drawer_header_user);

        TextView tvUserId = header.findViewById(R.id.tv_user_id);
        TextView tvPostCountValue = header.findViewById(R.id.tv_post_count_value);
        View rowMyPosts    = header.findViewById(R.id.row_my_posts);
        View rowLikedPosts = header.findViewById(R.id.row_liked_posts);
        View rowLogout     = header.findViewById(R.id.layout_layout);

        fillHeaderFromApi(tvUserId, tvPostCountValue);

        rowMyPosts.setOnClickListener(v -> {
            startActivity(new Intent(this, MyPostsActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
        rowLikedPosts.setOnClickListener(v ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)
        );
        rowLogout.setOnClickListener(v -> {
            doLogout();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void doLogout() {
        SharedPreferences prefs = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
        String accountId = prefs.getString("account_id", null);

        try {
            AuthService auth = RetrofitInstance.getAuthService();
            if (accountId != null) {
                auth.logout(accountId).enqueue(new retrofit2.Callback<Void>() {
                    @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                        Log.d("MyLikesActivity", "logout resp=" + r.code());
                    }
                    @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) {
                        Log.w("MyLikesActivity", "logout fail: " + t.getMessage());
                    }
                });
            }
        } catch (Exception ignore) {}

        prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .remove("account_id")
                .apply();

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void fillHeaderFromApi(TextView tvUserId, TextView tvPostCountValue) {
        SharedPreferences prefs = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
        String token     = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);
        if (token == null || accountId == null) return;

        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        UserService userService = retrofit.create(UserService.class);

        userService.getMyPage("Bearer " + token, accountId)
                .enqueue(new Callback<MyPageDTO>() {
                    @Override public void onResponse(Call<MyPageDTO> call, Response<MyPageDTO> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            tvUserId.setText("아이디: " + resp.body().getAccountId());
                            tvPostCountValue.setText(String.valueOf(resp.body().getNoticeBoardCount()));
                        } else if (resp.code() == 401 || resp.code() == 403) {
                            handleAuthError();
                        } else {
                            Log.w(TAG, "mypage 응답코드: " + resp.code());
                        }
                    }
                    @Override public void onFailure(Call<MyPageDTO> call, Throwable t) {
                        Log.e(TAG, "mypage 실패: " + t.getMessage());
                    }
                });
    }

    private void loadMyLikedPosts() {
        SharedPreferences prefs = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
        String token     = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);
        if (token == null || accountId == null) {
            Log.e(TAG, "token/accountId 누락");
            adapter.submitList(Collections.emptyList());
            return;
        }

        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        UserService userService = retrofit.create(UserService.class);

        userService.getUserLikedPosts(
                "Bearer " + token,
                accountId,
                0, // offset
                20  // limit

        ).enqueue(new Callback<List<BoardListItemDTO>>() {
            @Override
            public void onResponse(Call<List<BoardListItemDTO>> call,
                                   Response<List<BoardListItemDTO>> resp) {
                if (resp.code() == 401 || resp.code() == 403) {
                    handleAuthError();
                    return;
                }
                if (resp.code() == 404) {
                    Toast.makeText(MyLikesActivity.this, "해당 사용자를 찾을 수 없어요.", Toast.LENGTH_SHORT).show();
                    adapter.submitList(Collections.emptyList());
                    return;
                }
                if (resp.code() == 204) {
                    adapter.submitList(Collections.emptyList());
                    return;
                }
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.submitList(BoardMappers.toBoardItems(resp.body()));
                } else {
                    Log.e(TAG, "liked 목록 실패: " + resp.code());
                    Toast.makeText(MyLikesActivity.this,
                            "목록을 불러오지 못했어요 (" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                    adapter.submitList(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<BoardListItemDTO>> call, Throwable t) {
                Log.e(TAG, "네트워크 에러: " + t.getMessage(), t);
                Toast.makeText(MyLikesActivity.this,
                        "네트워크 오류가 발생했어요", Toast.LENGTH_SHORT).show();
                adapter.submitList(Collections.emptyList());
            }
        });
    }

    private void handleAuthError() {
        Toast.makeText(this, "로그인이 만료되었어요. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
        SharedPreferences prefs = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
        prefs.edit().remove("access_token").apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
}
