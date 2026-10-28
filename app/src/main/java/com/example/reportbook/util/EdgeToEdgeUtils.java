package com.example.reportbook.util;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * 适配 Android 15+ 强制沉浸式（edge-to-edge）的状态栏 / 导航栏工具。
 */
public final class EdgeToEdgeUtils {

    private EdgeToEdgeUtils() {
    }

    public static void setup(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        // 顶部是深色渐变 / 深色 Toolbar，使用白色状态栏图标
        controller.setAppearanceLightStatusBars(false);
        // 底部是浅色背景，使用深色导航栏图标
        controller.setAppearanceLightNavigationBars(true);
    }

    public static void applyTopInset(View view) {
        final int start = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int end = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(start + bars.left, top + bars.top, end + bars.right, bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    public static void applyBottomInset(View view) {
        final int start = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int end = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(start + bars.left, top, end + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * 底部同时考虑导航栏与输入法：键盘弹出时把内容顶到键盘上方。
     */
    public static void applyImeAndNavBottomInset(View view) {
        final int start = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int end = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(bars.bottom, ime.bottom);
            v.setPadding(start + bars.left, top, end + bars.right, bottom + bottomInset);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
