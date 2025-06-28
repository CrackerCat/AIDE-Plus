package io.github.zeroaicy.aide.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aide.ui.rewrite.R;

public class BreadcrumbView extends FrameLayout {

    private final RecyclerView recyclerView;
    private final java.util.List<BreadcrumbItem> items = new java.util.ArrayList<>();

    public BreadcrumbView(Context context) {
        this(context, null);
    }

    public BreadcrumbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BreadcrumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        addView(recyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setPath(java.util.List<BreadcrumbItem> path, OnBreadcrumbClickListener onClick) {
        items.clear();
        items.addAll(path);
        BreadcrumbView.BreadcrumbAdapter adapter = new BreadcrumbAdapter(items, onClick);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(items.size() - 1);
    }

    public void setFullPath(String fullPath, OnBreadcrumbClickListener onClick) {
        String cleanedPath = fullPath.trim().replaceAll("^/+|/+$", "");

        String[] segments = cleanedPath.isEmpty() ? new String[0] : cleanedPath.split("/");
        java.util.List<BreadcrumbItem> breadcrumbList = new java.util.ArrayList<>();
        String currentPath = "";

        for (String segment : segments) {
            currentPath += "/" + segment;
            breadcrumbList.add(new BreadcrumbItem(segment, currentPath));
        }

        setPath(breadcrumbList, onClick);
    }

    public interface OnBreadcrumbClickListener {
        void onClick(BreadcrumbItem item, int position);
    }

    private static class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.ViewHolder> {
        private final java.util.List<BreadcrumbItem> items;
        private final OnBreadcrumbClickListener onClick;

        BreadcrumbAdapter(java.util.List<BreadcrumbItem> items, OnBreadcrumbClickListener onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_breadcrumb, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BreadcrumbItem item = items.get(position);
            holder.text.setText(item.getName());
            holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);

            holder.text.setOnClickListener(v -> {
                if (onClick != null) {
                    onClick.onClick(item, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            View divider;

            ViewHolder(View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.text);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }

    public static class BreadcrumbItem {
        private final String name;
        private final String fullPath;

        public BreadcrumbItem(String name, String fullPath) {
            this.name = name;
            this.fullPath = fullPath;
        }

        public String getName() {
            return name;
        }

        public String getFullPath() {
            return fullPath;
        }
    }
}