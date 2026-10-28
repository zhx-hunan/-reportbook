package com.example.reportbook.model;

/**
 * 报告实体类，对应数据库中的一条记录。
 */
public class Report {

    private long id;
    private String title;
    private String category;
    private String content;
    private long createdAt;
    private long updatedAt;

    public Report() {
    }

    public Report(long id, String title, String category, String content, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
