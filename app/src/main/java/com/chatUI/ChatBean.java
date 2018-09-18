package com.chatUI;

/**
 * Created by zhaichenyang on 2018/9/13.
 */

public class ChatBean {

    private String message;

    private String source;

    public ChatBean(String message, String source) {
        this.message = message;
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
