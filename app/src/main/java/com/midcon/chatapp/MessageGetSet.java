package com.midcon.chatapp;

import android.content.Context;

/**
 * Created by ASHISH SINGH on 10/02/2018.
 */

public class MessageGetSet {

    private String message;
    private String senderID;
    private String receiverID;
    private String image_thumb;
    long time;

    public MessageGetSet() {

    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String sender) {
        this.senderID = sender;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
