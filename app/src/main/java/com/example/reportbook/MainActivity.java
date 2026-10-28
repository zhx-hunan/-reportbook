package com.example.reportbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reportbook.adapter.ReportAdapter;
import com.example.reportbook.db.ReportDbHelper;
import com.example.reportbook.model.Report;
import com.example.reportbook.util.CategoryUtils;
import com.example.reportbook.util.DateUtils;
import com.example.reportbook.util.EdgeToEdgeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * 主页：展示报告列表，支持搜索、分类筛选与长按删除。
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";

    private ReportDbHelper db;
    private ReportAdapter adapter;
    private String username;

    private View mainRoot;
    private TextView tvAvatar;
    private TextView tvGreeting;
    private TextView tvStats;
    private TextView tvResultCount;
    private TextView tvEmptyTitle;
    private TextView tvEmptyDesc;
    private View emptyState;
    private RecyclerView rvReports;
    private TextInputEditText etSearch;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.setup(this);
        setContentView(R.layout.activity_main);

        EdgeToEdgeUtils.applyTopInset(findViewById(R.id.mainAppBar));
        EdgeToEdgeUtils.applyBottomInset(findViewById(R.id.mainRoot));

        db = ReportDbHelper.getInstance(this);
        username = resolveUsername();

        bindViews();
        setupToolbar();
        setupHeader();
        setupSearch();
        setupChips();
        setupRecyclerView();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadReports();
    }

    private String resolveUsername() {
        String name = getIntent().getStringExtra(EXTRA_USERNAME);
        if (TextUtils.isEmpty(name)) {
            name = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                    .getString(LoginActivity.KEY_USERNAME, "");
        }
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.default_username);
        }
        return name;
    }

    private void bindViews() {
        mainRoot = findViewById(R.id.mainRoot);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStats = findViewById(R.id.tvStats);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        emptyState = findViewById(R.id.emptyState);
        rvReports = findViewById(R.id.rvReports);
        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert));
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            if (itemId == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }
            return false;
        });
    }

    private void setupHeader() {
        String trimmed = username.trim();
        tvAvatar.setText(trimmed.isEmpty()
                ? getString(R.string.default_username)
                : trimmed.substring(0, 1).toUpperCase(Locale.getDefault()));
        tvGreeting.setText(getString(R.string.main_greeting, DateUtils.greeting(this), username));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                reloadReports();
            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void setupChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> reloadReports());
    }

    private void setupRecyclerView() {
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setHasFixedSize(true);
        adapter = new ReportAdapter(new ReportAdapter.OnReportActionListener() {
            @Override
            public void onReportClick(Report report) {
                hideKeyboard();
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_REPORT_ID, report.getId());
                startActivity(intent);
            }

            @Override
            public void onReportLongClick(Report report) {
                showDeleteDialog(report);
            }
        });
        rvReports.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, EditReportActivity.class)));
    }

    private void reloadReports() {
        if (adapter == null || db == null) {
            return;
        }
        String keyword = etSearch.getText() == null ? "" : etSearch.getText().toString();
        String category = getSelectedCategory();
        List<Report> reports = db.query(keyword, category);
        adapter.submitList(reports);

        boolean filtering = !TextUtils.isEmpty(keyword.trim()) || category != null;
        tvResultCount.setText(filtering
                ? getString(R.string.result_count_filtered, reports.size())
                : getString(R.string.result_count, reports.size()));

        if (reports.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
            tvEmptyTitle.setText(filtering ? R.string.empty_search_title : R.string.empty_title);
            tvEmptyDesc.setText(filtering ? R.string.empty_search_desc : R.string.empty_desc);
        } else {
            emptyState.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
        }

        updateStats();
    }

    private void updateStats() {
        int total = db.count();
        if (total == 0) {
            tvStats.setText(R.string.main_stats_empty);
        } else {
            tvStats.setText(getString(R.string.main_stats, total,
                    DateUtils.formatRelative(this, db.latestUpdatedAt())));
        }
    }

    private String getSelectedCategory() {
        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId == R.id.chipStudy) {
            return CategoryUtils.CATEGORY_STUDY;
        }
        if (checkedId == R.id.chipWork) {
            return CategoryUtils.CATEGORY_WORK;
        }
        if (checkedId == R.id.chipLife) {
            return CategoryUtils.CATEGORY_LIFE;
        }
        if (checkedId == R.id.chipOther) {
            return CategoryUtils.CATEGORY_OTHER;
        }
        return null;
    }

    private void showDeleteDialog(Report report) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, report.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm, (d, which) -> {
                    db.delete(report.getId());
                    reloadReports();
                    Snackbar.make(mainRoot, getString(R.string.delete_done, report.getTitle()),
                            Snackbar.LENGTH_SHORT).show();
                })
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.error));
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout_confirm, (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(this, R.string.logout_done, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        etSearch.clearFocus();
    }
}
