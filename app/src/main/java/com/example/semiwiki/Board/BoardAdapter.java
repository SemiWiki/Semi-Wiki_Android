package com.example.semiwiki.Board;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semiwiki.R;

import java.util.ArrayList;
import java.util.List;

import android.view.Gravity;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull BoardItem item, int position);
    }

    private final List<BoardItem> items = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public BoardAdapter() {}

    public BoardAdapter(List<BoardItem> initial) {
        if (initial != null) items.addAll(initial);
    }

    public void submitList(List<BoardItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.itemClickListener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_board, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BoardItem item = items.get(position);

        h.tvTitle.setText(nullToEmpty(item.getTitle()));
        h.tvTitle.setMaxLines(1);
        h.tvTitle.setEllipsize(TextUtils.TruncateAt.END);

        h.tvEditor.setText(nullToEmpty(item.getEditor()));
        h.tvEditor.setMaxLines(1);
        h.tvEditor.setEllipsize(TextUtils.TruncateAt.END);

        h.layoutChips.removeAllViews();
        List<String> cats = item.getCategories();
        if (cats != null && !cats.isEmpty()) {
            int show = Math.min(2, cats.size());
            for (int i = 0; i < show; i++) {
                h.layoutChips.addView(makeChip(h.layoutChips.getContext(), cats.get(i)));
            }
            int remain = cats.size() - show;
            if (remain > 0) {
                h.layoutChips.addView(makeChip(h.layoutChips.getContext(), "+" + remain));
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item, h.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        LinearLayout layoutChips;
        TextView tvEditor;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            layoutChips = itemView.findViewById(R.id.linear_layout_category);
            tvEditor = itemView.findViewById(R.id.tv_editor);
        }
    }


    private static View makeChip(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);

        tv.setTextSize(9);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setIncludeFontPadding(false);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_chip));

        int height = dp(ctx, 20);
        int padH = dp(ctx, 4);
        tv.setPadding(padH, 0, padH, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                height
        );
        lp.setMargins(dp(ctx, 2), 0, dp(ctx, 2), 0);
        tv.setLayoutParams(lp);

        return tv;
    }


    private static int dp(Context c, int v) {
        return Math.round(c.getResources().getDisplayMetrics().density * v);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}