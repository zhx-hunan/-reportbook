package com.example.reportbook;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reportbook.db.ReportDbHelper;
import com.example.reportbook.model.Report;
import com.example.reportbook.util.CategoryUtils;
import com.example.reportbook.util.EdgeToEdgeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 新建 / 编辑报告页：表单输入并写入 SQLite。
 */
public class EditReportActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "extra_report_id";

    private ReportDbHelper db;
    private Report editingReport;
    private long reportId = -1L;

    private TextInputLayout titleLayout;
    private TextInputLayout contentLayout;
    private TextInputEditText etTitle;
    private TextInputEditText etContent;
    private MaterialAutoCompleteTextView etCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.setup(this);
        setContentView(R.layout.activity_edit);

        EdgeToEdgeUtils.applyTopInset(findViewById(R.id.editAppBar));
        EdgeToEdgeUtils.applyImeAndNavBottomInset(findViewById(R.id.editRoot));

        db = ReportDbHelper.getInstance(this);
        reportId = getIntent().getLongExtra(EXTRA_REPORT_ID, -1L);

        titleLayout = findViewById(R.id.titleLayout);
        contentLayout = findViewById(R.id.contentLayout);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etCategory = findViewById(R.id.etCategory);

        MaterialToolbar toolbar = findViewById(R.id.editToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveReport());

        if (reportId > 0) {
            editingReport = db.getById(reportId);
            if (editingReport == null) {
                Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(R.string.edit_report);
            fillForm(editingReport);
        } else {
            toolbar.setTitle(R.string.new_report);
            etCategory.setText(CategoryUtils.CATEGORY_STUDY, false);
        }
    }

    private void fillForm(Report report) {
        etTitle.setText(report.getTitle());
        etTitle.setSelection(etTitle.getText() == null ? 0 : etTitle.getText().length());
        etCategory.setText(report.getCategory(), false);
        etContent.setText(report.getContent());
    }

    private void saveReport() {
        titleLayout.setError(null);
        contentLayout.setError(null);

        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        String content = etContent.getText() == null ? "" : etContent.getText().toString().trim();
        String category = etCategory.getText() == null ? "" : etCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(getString(R.string.title_empty));
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            contentLayout.setError(getString(R.string.content_empty));
            etContent.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            category = CategoryUtils.CATEGORY_STUDY;
        }

        long now = System.currentTimeMillis();
        if (editingReport == null) {
            Report report = new Report();
            report.setTitle(title);
            report.setCategory(category);
            report.setContent(content);
            report.setCreatedAt(now);
            report.setUpdatedAt(now);
            db.insert(report);
        } else {
            editingReport.setTitle(title);
            editingReport.setCategory(category);
            editingReport.setContent(content);
            editingReport.setUpdatedAt(now);
            db.update(editingReport);
        }

        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
