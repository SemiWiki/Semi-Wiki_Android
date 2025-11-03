package com.example.semiwiki.Board;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semiwiki.R;
import com.example.semiwiki.Login.RetrofitInstance;
import com.example.semiwiki.Drawer.LikeService;

import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "board_id";

    private TextView tvTitle, tvLikeCount;
    private ImageView ivLike;

    private TextView tvCat1, tvCat2, tvCat3;
    private LinearLayout catWrap, tocContainer, contentContainer;
<<<<<<< HEAD
<<<<<<< HEAD

    private ImageView ivLogo, ivMenu;
=======
    private ImageView ivLogo, ivMenu, ivSearch;
>>>>>>> origin/main
=======

    private ImageView ivLogo, ivMenu;
>>>>>>> origin/feat/65-search-icon-function
    private DrawerLayout drawerLayout;

    private long boardId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        boardId = getIntent().getLongExtra(EXTRA_BOARD_ID, -1);
        if (boardId <= 0) { finish(); return; }

        SharedPreferences prefs = getSharedPreferences("semiwiki_prefs", MODE_PRIVATE);
        token = prefs.getString("access_token", null);

        bindViews();
        wireTopBar();

        ivLike.setClickable(false);
        ivLike.setFocusable(false);
        ivLike.setOnClickListener(null);

        loadDetail();
        loadLikeCount();
        loadIsLiked();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tv_post_title);
        ivLike = findViewById(R.id.iv_likes);
        tvLikeCount = findViewById(R.id.tv_likes);

        tvCat1 = findViewById(R.id.tv_category_01);
        tvCat2 = findViewById(R.id.tv_category_02);
        tvCat3 = findViewById(R.id.tv_category_03);
        catWrap = findViewById(R.id.linear_layout_category_three);

        tocContainer = findViewById(R.id.linear_layout_toc);
        contentContainer = findViewById(R.id.linear_layout_post_card);

        ivLogo = findViewById(R.id.iv_logo_semiwiki);

        drawerLayout = findViewById(R.id.drawerLayout);
    }

    private void wireTopBar() {
        if (ivLogo != null) {
            ivLogo.setOnClickListener(v -> {
                Intent intent = new Intent(PostDetailActivity.this, BoardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        }

    private void loadDetail() {
        if (token == null) return;

        Retrofit rt = RetrofitInstance.getRetrofitInstance();
        BoardService svc = rt.create(BoardService.class);

        svc.getBoardDetail("Bearer " + token, boardId)
                .enqueue(new Callback<BoardDetailDTO>() {
                    @Override
                    public void onResponse(Call<BoardDetailDTO> call, Response<BoardDetailDTO> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Log.e("Detail", "fail " + resp.code());
                            return;
                        }
                        BoardDetailDTO d = resp.body();
                        tvTitle.setText(d.getTitle());
                        renderCategories(d.getCategories());
                        renderHeaders(d.getNoticeBoardHeaders());
                    }

                    @Override
                    public void onFailure(Call<BoardDetailDTO> call, Throwable t) {
                        Log.e("Detail", "net", t);
                    }
                });
    }

    private void loadLikeCount() {
        if (token == null) return;

        LikeService like = RetrofitInstance.getRetrofitInstance().create(LikeService.class);
        like.getLikeCount("Bearer " + token, boardId)
                .enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> c, Response<Integer> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            String pretty = NumberFormat.getInstance().format(r.body());
                            tvLikeCount.setText(pretty);
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> c, Throwable t) { /* no-op */ }
                });
    }

    private void loadIsLiked() {
        if (token == null) return;

        LikeService like = RetrofitInstance.getRetrofitInstance().create(LikeService.class);
        like.isLikedByMe("Bearer " + token, boardId)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> c, Response<Boolean> r) {
                        boolean liked = r.isSuccessful() && Boolean.TRUE.equals(r.body());
                        ivLike.setImageResource(liked
                                ? R.drawable.ic_fill_likes
                                : R.drawable.ic_likes);
                    }

                    @Override
                    public void onFailure(Call<Boolean> c, Throwable t) { /* no-op */ }
                });
    }

    private void renderCategories(List<String> cats) {
        TextView[] chips = { tvCat1, tvCat2, tvCat3 };
        for (TextView v : chips) v.setVisibility(View.GONE);

        if (cats == null || cats.isEmpty()) {
            catWrap.setVisibility(View.GONE);
            return;
        }

        catWrap.setVisibility(View.VISIBLE);
        for (int i = 0; i < cats.size() && i < chips.length; i++) {
            chips[i].setText(cats.get(i));
            chips[i].setVisibility(View.VISIBLE);
        }
    }

    private void renderHeaders(List<BoardDetailDTO.HeaderDTO> roots) {
        tocContainer.removeAllViews();
        contentContainer.removeAllViews();
        if (roots == null || roots.isEmpty()) return;

        Deque<BoardDetailDTO.HeaderDTO> st = new ArrayDeque<>();
        for (int i = roots.size() - 1; i >= 0; i--) st.push(roots.get(i));

        while (!st.isEmpty()) {
            BoardDetailDTO.HeaderDTO h = st.pop();

            TextView toc = new TextView(this);
            toc.setText(h.getHeaderNumber() + ". " + h.getTitle());
            toc.setTextSize(14f);
            toc.setTextColor(0xFF252525);
            toc.setPadding(dp(8) * (Math.max(1, h.getLevel()) - 1), dp(2), 0, dp(2));
            final String key = "sec:" + h.getHeaderNumber();
            toc.setOnClickListener(v -> scrollToSection(key));
            tocContainer.addView(toc);

            LinearLayout sec = new LinearLayout(this);
            sec.setOrientation(LinearLayout.VERTICAL);
            sec.setTag(key);
            sec.setPadding(dp(14), dp(12), dp(14), dp(12));

            LinearLayout titleRow = new LinearLayout(this);
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            TextView num = tBold(h.getHeaderNumber(), 0xFFFF7A00, 18);
            TextView title = tBold("  " + h.getTitle(), 0xFF252525, 18);
            titleRow.addView(num);
            titleRow.addView(title);
            sec.addView(titleRow);

            View line = new View(this);
            line.setBackgroundColor(0xFF252525);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            lp.topMargin = dp(4);
            sec.addView(line, lp);

            TextView body = tBold(h.getContents() == null ? "" : h.getContents(), 0xFF252525, 12);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.topMargin = dp(10);
            sec.addView(body, bp);

            contentContainer.addView(sec);

            List<BoardDetailDTO.HeaderDTO> children = h.getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) st.push(children.get(i));
            }
        }
    }

    private TextView tBold(String s, int color, float sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void scrollToSection(String tag) {
        View target = null;
        for (int i = 0; i < contentContainer.getChildCount(); i++) {
            View v = contentContainer.getChildAt(i);
            if (tag.equals(v.getTag())) { target = v; break; }
        }
        if (target == null) return;

        View parent = (View) findViewById(R.id.linear_layout_post).getParent();
        if (parent instanceof ScrollView) {
            ((ScrollView) parent).smoothScrollTo(0, target.getTop());
        } else {
            parent.scrollTo(0, target.getTop());
        }
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}
