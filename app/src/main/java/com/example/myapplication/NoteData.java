package com.example.myapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteData implements Serializable {
    private String id;
    private String title;
    private String content;
    private String date;
    private List<String> imagePaths;
    private int order; // Новое поле для сортировки

    public NoteData() {
        this.imagePaths = new ArrayList<>();
        this.order = 0;
    }

    public NoteData(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.imagePaths = new ArrayList<>();
        this.order = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}