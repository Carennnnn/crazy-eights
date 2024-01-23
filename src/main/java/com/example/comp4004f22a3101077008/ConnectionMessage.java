package com.example.comp4004f22a3101077008;

public class ConnectionMessage {
    private String name;

    public ConnectionMessage() {
    }

    public ConnectionMessage(String name) {
        this.name = name;
    }

    public String getMessage() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
