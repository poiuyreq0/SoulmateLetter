package com.poiuyreq0.soulmateletter;

public class Letter {

    private String sender;
    private String text;

    public Letter() {

    }

    public Letter(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }
}
