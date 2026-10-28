package com.example.reportbook.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.reportbook.R;

/**
 * 分类常量与配色工具。
 */
public final class CategoryUtils {

    public static final String CATEGORY_STUDY = "学习";
    public static final String CATEGORY_WORK = "工作";
    public static final String CATEGORY_LIFE = "生活";
    public static final String CATEGORY_OTHER = "其他";

    private CategoryUtils() {
    }

    public static int color(Context context, String category) {
        if (CATEGORY_STUDY.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_study);
        }
        if (CATEGORY_WORK.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_work);
        }
        if (CATEGORY_LIFE.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_life);
        }
        return ContextCompat.getColor(context, R.color.cat_other);
    }

    public static int backgroundColor(Context context, String category) {
        if (CATEGORY_STUDY.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_study_bg);
        }
        if (CATEGORY_WORK.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_work_bg);
        }
        if (CATEGORY_LIFE.equals(category)) {
            return ContextCompat.getColor(context, R.color.cat_life_bg);
        }
        return ContextCompat.getColor(context, R.color.cat_other_bg);
    }
}
