package com.wpdf.model;

/**
 * Created by Admin on 19-02-2016.
 */
public class NotificationModel {

    private String strMessage = "";
    private int iRead = -1;
    private int id;


    public NotificationModel(String strMessage, int iRead, int id) {
        this.strMessage = strMessage;
        this.iRead = iRead;
        this.id = id;
    }

    public String getStrMessage() {
        return strMessage;
    }

    public int getiRead() {
        return iRead;
    }

    public int getId() {
        return id;
    }
}
