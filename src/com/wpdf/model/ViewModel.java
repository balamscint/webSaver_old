package com.wpdf.model;

public class ViewModel {

    private String name, path, time;


    public ViewModel(String name, String path, String time) {
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getTime() {
        return time;
    }

    public void setSaved(String time) {
        this.time = time;
    }
}