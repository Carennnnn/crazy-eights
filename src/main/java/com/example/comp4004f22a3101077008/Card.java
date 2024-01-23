package com.example.comp4004f22a3101077008;

public class Card {
    private String suit;
    private String rank;
    public Card(){
        this.suit = "";
        this.rank = "";
    }
    public Card (String s, String r){
        this.suit = s;
        this.rank = r;
    }
    public String getSuit(){
        return this.suit;
    }
    public String getRank(){
        return this.rank;
    }
    
}
