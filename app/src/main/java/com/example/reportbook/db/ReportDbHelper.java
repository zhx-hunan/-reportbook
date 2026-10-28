package com.example.reportbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.reportbook.model.Report;
import com.example.reportbook.util.CategoryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 数据库帮助类：负责建表、示例数据以及报告表的增删改查。
 */
public class ReportDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "report_book.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_REPORTS = "reports";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CATEGORY = "category";
    public static final String COL_CONTENT = "content";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    private static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;

    private static ReportDbHelper instance;

    private ReportDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized ReportDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ReportDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_REPORTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT NOT NULL, "
                + COL_CREATED_AT + " INTEGER NOT NULL, "
                + COL_UPDATED_AT + " INTEGER NOT NULL)");
        seedSampleReports(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    public long insert(Report report) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_REPORTS, null, toValues(report));
        report.setId(id);
        return id;
    }

    public int update(Report report) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_REPORTS, toValues(report),
                COL_ID + " = ?", new String[]{String.valueOf(report.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_REPORTS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Report getById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_REPORTS, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    /**
     * 按关键词（标题 / 正文）和分类查询，按更新时间倒序。
     * 关键词为空、分类为 null 时表示不过滤。
     */
    public List<Report> query(String keyword, String category) {
        StringBuilder selection = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            selection.append(COL_CATEGORY).append(" = ?");
            args.add(category);
        }

        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        if (!trimmedKeyword.isEmpty()) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(").append(COL_TITLE).append(" LIKE ? OR ")
                    .append(COL_CONTENT).append(" LIKE ?)");
            String like = "%" + trimmedKeyword + "%";
            args.add(like);
            args.add(like);
        }

        String where = selection.length() == 0 ? null : selection.toString();
        String[] whereArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        SQLiteDatabase db = getReadableDatabase();
        List<Report> reports = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_REPORTS, null, where, whereArgs,
                null, null, COL_UPDATED_AT + " DESC")) {
            while (cursor.moveToNext()) {
                reports.add(fromCursor(cursor));
            }
        }
        return reports;
    }

    public int count() {
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_REPORTS);
    }

    public long latestUpdatedAt() {
        return DatabaseUtils.longForQuery(getReadableDatabase(),
                "SELECT MAX(" + COL_UPDATED_AT + ") FROM " + TABLE_REPORTS, null);
    }

    private ContentValues toValues(Report report) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, report.getTitle());
        values.put(COL_CATEGORY, report.getCategory());
        values.put(COL_CONTENT, report.getContent());
        values.put(COL_CREATED_AT, report.getCreatedAt());
        values.put(COL_UPDATED_AT, report.getUpdatedAt());
        return values;
    }

    private Report fromCursor(Cursor cursor) {
        return new Report(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
        );
    }

    /**
     * 首次创建数据库时写入几篇示例报告，方便直接演示。
     */
    private void seedSampleReports(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        insertSample(db,
                "Android 阶段作业开发计划",
                CategoryUtils.CATEGORY_STUDY,
                "一、作业目标\n"
                        + "完成一个「报告记录」Android 应用，综合运用 Activity 跳转、RecyclerView、SQLite 与 SharedPreferences。\n\n"
                        + "二、功能拆解\n"
                        + "1. 登录页：校验输入，并用 SharedPreferences 记住用户名；\n"
                        + "2. 主页：展示报告列表，支持关键词搜索和分类筛选；\n"
                        + "3. 新建 / 编辑页：填写标题、分类与正文，保存到 SQLite；\n"
                        + "4. 详情页：查看全文，支持编辑、分享和删除。\n\n"
                        + "三、时间安排\n"
                        + "第 1 天：搭建项目与页面布局；\n"
                        + "第 2 天：完成数据库与列表逻辑；\n"
                        + "第 3 天：联调页面跳转并整理实验报告。\n\n"
                        + "四、验收标准\n"
                        + "页面跳转顺畅，数据在应用重启后不丢失，列表、搜索与筛选结果正确。",
                now - 2 * DAY,
                now - 2 * HOUR);

        insertSample(db,
                "本周学习小组进度汇报",
                CategoryUtils.CATEGORY_WORK,
                "本周小组围绕期末项目完成了三件事：\n"
                        + "1. 明确需求边界，删减了两个非核心功能；\n"
                        + "2. 完成登录、列表、详情三个页面的原型；\n"
                        + "3. 统一了颜色、圆角与字号规范。\n\n"
                        + "遇到的问题：\n"
                        + "详情页返回列表后数据没有刷新。解决方案是在 onResume() 中重新查询数据库。\n\n"
                        + "下周计划：\n"
                        + "补齐新建与编辑页面，并进行一次完整的流程测试。",
                now - 4 * DAY,
                now - 1 * DAY);

        insertSample(db,
                "周末读书笔记：把复杂的事情写简单",
                CategoryUtils.CATEGORY_LIFE,
                "这周读完了《Android 移动开发基础案例教程》的前六章。\n\n"
                        + "印象最深的一句话是：先让页面跑起来，再逐步替换成更好的实现。\n\n"
                        + "以前总想一次写出完美的代码，结果常常卡在细节上。这次按照书里的案例顺序练习："
                        + "先写布局，再绑定控件，最后接入数据库，反而顺利很多。\n\n"
                        + "下周准备把书中的 SQLite 案例独立敲一遍，不看源码。",
                now - 6 * DAY,
                now - 3 * DAY);

        insertSample(db,
                "SQLite 增删改查学习小结",
                CategoryUtils.CATEGORY_STUDY,
                "一、核心类\n"
                        + "SQLiteOpenHelper 负责数据库的创建与版本管理，getWritableDatabase() 用于获取可写数据库对象。\n\n"
                        + "二、常用方法\n"
                        + "insert() 新增数据；update() 修改数据；delete() 删除数据；query() 查询数据。\n\n"
                        + "三、注意事项\n"
                        + "1. Cursor 使用完必须 close()，避免资源泄漏；\n"
                        + "2. 查询条件尽量使用参数占位符，避免拼接 SQL；\n"
                        + "3. 时间统一存为毫秒时间戳，展示时再格式化。\n\n"
                        + "四、实践\n"
                        + "在「墨记报告」中完成了报告的新增、编辑、删除与搜索，数据在应用重启后仍然保留。",
                now - 9 * DAY,
                now - 7 * DAY);
    }

    private void insertSample(SQLiteDatabase db, String title, String category,
                              String content, long createdAt, long updatedAt) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_CATEGORY, category);
        values.put(COL_CONTENT, content);
        values.put(COL_CREATED_AT, createdAt);
        values.put(COL_UPDATED_AT, updatedAt);
        db.insert(TABLE_REPORTS, null, values);
    }
}
