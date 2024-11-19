package com.example.doan.data.model;

import java.io.Serializable;

public class FileVideo implements Serializable {
    String path;
    int id;

    public FileVideo(String path, int id) {
        this.path = path;
        this.id = id;
    }

    public FileVideo() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id
        ;
    }

}
