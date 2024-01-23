package com.example.comp4004f22a3101077008;

public class ChangeSuitMessage {
    private String content;
    private String suit;
    private String turn;
    public ChangeSuitMessage() {
    }

    public ChangeSuitMessage(String content, String suit, String turn) {
        this.content = content;
        this.suit = suit;
        this.turn = turn;
    }

    public String getContent() {
        return content;
    }
    public String getSuit (){return suit;}
    public String getTurn(){return turn;}
}
