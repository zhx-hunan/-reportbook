package com.example.reportbook;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reportbook.util.EdgeToEdgeUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * 关于页：展示应用介绍、技术要点与使用说明。
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.setup(this);
        setContentView(R.layout.activity_about);

        EdgeToEdgeUtils.applyTopInset(findViewById(R.id.aboutAppBar));
        EdgeToEdgeUtils.applyBottomInset(findViewById(R.id.aboutRoot));

        MaterialToolbar toolbar = findViewById(R.id.aboutToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvVersion = findViewById(R.id.tvVersion);
        String versionName = "1.0";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (info.versionName != null) {
                versionName = info.versionName;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            // 使用默认版本号
        }
        tvVersion.setText(getString(R.string.about_version, versionName));
    }
}
