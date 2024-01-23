package com.example.comp4004f22a3101077008;

public class Message {
    private String content;
    private String id;
    public Message() {
    }

    public Message(String content, String id) {
        this.content = content;
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public String getId (){return id;}
}
