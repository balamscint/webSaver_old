package com.wpdf.model;

public class PdfModel {

    private String name, path, time;


    public PdfModel(String name, String path, String time) {
        this.name = name;
        this.path = path;
        this.time = time;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getTime() {
        return time;
    }
}