package com.example.reportbook.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reportbook.R;
import com.example.reportbook.model.Report;
import com.example.reportbook.util.CategoryUtils;
import com.example.reportbook.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 报告列表适配器。
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnReportActionListener {
        void onReportClick(Report report);

        void onReportLongClick(Report report);
    }

    private final List<Report> reports = new ArrayList<>();
    private final OnReportActionListener listener;

    public ReportAdapter(OnReportActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Report> newReports) {
        reports.clear();
        if (newReports != null) {
            reports.addAll(newReports);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategory;
        private final TextView tvDate;
        private final TextView tvTitle;
        private final TextView tvPreview;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPreview = itemView.findViewById(R.id.tvPreview);
        }

        void bind(Report report, OnReportActionListener listener) {
            Context context = itemView.getContext();

            tvCategory.setText(report.getCategory());
            tvCategory.setTextColor(CategoryUtils.color(context, report.getCategory()));
            ViewCompat.setBackgroundTintList(tvCategory,
                    ColorStateList.valueOf(CategoryUtils.backgroundColor(context, report.getCategory())));

            tvDate.setText(DateUtils.formatRelative(context, report.getUpdatedAt()));
            tvTitle.setText(report.getTitle());

            String content = report.getContent() == null ? "" : report.getContent();
            tvPreview.setText(content.replace('\n', ' ').replace('\r', ' ').trim());

            itemView.setOnClickListener(v -> listener.onReportClick(report));
            itemView.setOnLongClickListener(v -> {
                listener.onReportLongClick(report);
                return true;
            });
        }
    }
}
