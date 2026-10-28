package com.example.reportbook;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.reportbook.db.ReportDbHelper;
import com.example.reportbook.model.Report;
import com.example.reportbook.util.CategoryUtils;
import com.example.reportbook.util.DateUtils;
import com.example.reportbook.util.EdgeToEdgeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 报告详情页：查看全文，支持编辑、分享与删除。
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "extra_report_id";

    private ReportDbHelper db;
    private Report report;
    private long reportId;

    private TextView tvCategory;
    private TextView tvTitle;
    private TextView tvMeta;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.setup(this);
        setContentView(R.layout.activity_detail);

        EdgeToEdgeUtils.applyTopInset(findViewById(R.id.detailAppBar));
        EdgeToEdgeUtils.applyBottomInset(findViewById(R.id.detailRoot));

        db = ReportDbHelper.getInstance(this);
        reportId = getIntent().getLongExtra(EXTRA_REPORT_ID, -1L);

        tvCategory = findViewById(R.id.tvCategory);
        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvContent = findViewById(R.id.tvContent);

        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                shareReport();
                return true;
            }
            if (itemId == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        MaterialButton btnEdit = findViewById(R.id.btnEdit);
        MaterialButton btnShare = findViewById(R.id.btnShare);
        btnEdit.setOnClickListener(v -> openEditor());
        btnShare.setOnClickListener(v -> shareReport());

        if (reportId <= 0) {
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reportId <= 0) {
            return;
        }
        report = db.getById(reportId);
        if (report == null) {
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindReport(report);
    }

    private void bindReport(Report data) {
        tvTitle.setText(data.getTitle());
        tvContent.setText(data.getContent());
        tvCategory.setText(data.getCategory());
        tvCategory.setTextColor(CategoryUtils.color(this, data.getCategory()));
        ViewCompat.setBackgroundTintList(tvCategory,
                ColorStateList.valueOf(CategoryUtils.backgroundColor(this, data.getCategory())));

        String updated = getString(R.string.detail_updated, DateUtils.formatFull(data.getUpdatedAt()));
        String created = getString(R.string.detail_created, DateUtils.formatFull(data.getCreatedAt()));
        tvMeta.setText(updated + "  ·  " + created);
    }

    private void openEditor() {
        if (report == null) {
            return;
        }
        Intent intent = new Intent(this, EditReportActivity.class);
        intent.putExtra(EditReportActivity.EXTRA_REPORT_ID, reportId);
        startActivity(intent);
    }

    private void shareReport() {
        if (report == null) {
            return;
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, report.getTitle());
        send.putExtra(Intent.EXTRA_TEXT,
                report.getTitle() + "\n\n" + report.getContent() + "\n\n"
                        + getString(R.string.share_footer));
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    private void confirmDelete() {
        if (report == null) {
            return;
        }
        String title = report.getTitle();
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm, (d, which) -> {
                    db.delete(reportId);
                    Toast.makeText(this, getString(R.string.delete_done, title),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.error));
    }
}
