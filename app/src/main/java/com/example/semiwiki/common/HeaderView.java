package com.example.semiwiki.common;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semiwiki.R;
import com.google.android.material.navigation.NavigationView;

import android.net.Uri;


public class HeaderView extends FrameLayout {
    public interface Listener {
        void onSearchSubmit(String keyword);
        void onSearchCancel();
    }
    public interface DrawerActions {
        void onClickMyPosts();
        void onClickMyLikes();
        void onClickLogout();
        void onClickInquiry();
    }

    private ConstraintLayout headerContainer;
    private ImageView ivMenu;
    private ImageView ivLogo;
    private ImageView ivSearch;
    private View searchBar;
    private EditText etKeyword;
    private ImageView ivClear;
    private ImageView ivSearchInBar;
    private Listener listener;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private DrawerActions drawerActions;



    public HeaderView(Context context) { super(context); }
    public HeaderView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void bindWithoutXml(ConstraintLayout container, ImageView menu, ImageView logo, ImageView searchBtn) {
        this.headerContainer = container;
        this.ivMenu = menu;
        this.ivLogo = logo;
        this.ivSearch = searchBtn;

        ivMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });

        ivSearch.setOnClickListener(v -> enterSearchMode());
    }

    public void bindDrawer(DrawerLayout drawer, NavigationView nav, DrawerActions actions) {
        this.drawerLayout = drawer;
        this.navView = nav;
        this.drawerActions = actions;

        View header = navView.getHeaderView(0);
        if (header == null) header = navView.inflateHeaderView(R.layout.drawer_header_user);

        View rowMyPosts = header.findViewById(R.id.row_my_posts);
        View rowLiked = header.findViewById(R.id.row_liked_posts);
        View rowInquiry = header.findViewById(R.id.row_inquiry);
        View rowLogout = header.findViewById(R.id.layout_layout);

        if (rowMyPosts != null) rowMyPosts.setOnClickListener(v -> {
            if (drawerActions != null) drawerActions.onClickMyPosts();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        if (rowLiked != null) rowLiked.setOnClickListener(v -> {
            if (drawerActions != null) drawerActions.onClickMyLikes();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        if (rowInquiry != null) rowInquiry.setOnClickListener(v -> {
            if (drawerActions != null) drawerActions.onClickInquiry();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        if (rowLogout != null) rowLogout.setOnClickListener(v -> {
            if (drawerActions != null) drawerActions.onClickLogout();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

    }

    public void updateDrawerUser(String accountId, String postCountText) {
        if (navView == null) return;
        View header = navView.getHeaderView(0);
        if (header == null) return;

        TextView tvId = header.findViewById(R.id.tv_user_id);
        TextView tvCnt = header.findViewById(R.id.tv_post_count_value);
        if (tvId != null) tvId.setText("아이디: " + accountId);
        if (tvCnt != null) tvCnt.setText(postCountText);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void enterSearchMode() {
        if (headerContainer == null) return;

        ivLogo.setVisibility(View.GONE);
        ivSearch.setVisibility(View.GONE);
        ivMenu.setVisibility(View.GONE);

        if (searchBar == null) {
            searchBar = LayoutInflater.from(getContext())
                    .inflate(R.layout.common_search_bar, headerContainer, false);

            etKeyword = searchBar.findViewById(R.id.et_keyword);
            ivClear = searchBar.findViewById(R.id.iv_clear);
            ivSearchInBar = searchBar.findViewById(R.id.iv_searchbar_icon);

            ConstraintLayout.LayoutParams lp =
                    new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                    );
            searchBar.setId(View.generateViewId());
            headerContainer.addView(searchBar, lp);

            ConstraintSet cs = new ConstraintSet();
            cs.clone(headerContainer);
            cs.connect(searchBar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp(16));
            cs.connect(searchBar.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(16));
            cs.connect(searchBar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            cs.connect(searchBar.getId(), ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            cs.applyTo(headerContainer);

            ivClear.setOnClickListener(v -> {
                etKeyword.setText("");
                hideIme(v);
                exitSearchMode();
            });

            etKeyword.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    ivClear.setVisibility(s.length() > 0 ? VISIBLE : GONE);
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            etKeyword.setOnEditorActionListener((v, actionId, event) -> {
                boolean enter = (actionId == EditorInfo.IME_ACTION_SEARCH)
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN);
                if (enter) {
                    hideIme(v);
                    if (listener != null) listener.onSearchSubmit(etKeyword.getText().toString().trim());
                    return true;
                }
                return false;
            });

            ivSearchInBar.setOnClickListener(v -> {
                hideIme(v);
                if (listener != null) listener.onSearchSubmit(etKeyword.getText().toString().trim());
            });
        }

        searchBar.setVisibility(View.VISIBLE);
        etKeyword.requestFocus();
        showIme(etKeyword);
    }

    public void exitSearchMode() {
        if (searchBar != null) searchBar.setVisibility(View.GONE);
        ivLogo.setVisibility(View.VISIBLE);
        ivSearch.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.VISIBLE);
        if (etKeyword != null) etKeyword.setText("");
        if (listener != null) listener.onSearchCancel();
    }

    public boolean isInSearchMode() {
        return searchBar != null && searchBar.getVisibility() == View.VISIBLE;
    }

    private void showIme(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
    private void hideIme(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }
}
