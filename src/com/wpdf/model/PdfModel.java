package com.wpdf.model;

public class PdfModel {

    private String strFileName;
    private String strFileModifiedTime;
    private String strFileSize;

    public PdfModel(String strFileName, String strFileModifiedTime, String strFileSize) {
        this.strFileName = strFileName;
        this.strFileModifiedTime = strFileModifiedTime;
        this.strFileSize = strFileSize;
    }

    public String getStrFileSize() {
        return strFileSize;
    }

    public String getStrFileName() {
        return strFileName;
    }

    public String getStrFileModifiedTime() {
        return strFileModifiedTime;
    }
}