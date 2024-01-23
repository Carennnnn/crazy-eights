package com.example.comp4004f22a3101077008;

import org.springframework.stereotype.Component;

import java.util.ArrayList;

public class Player {
    private final int playerID;
    private String username;
    ArrayList<Card> cards;
    private int score;
    private int numDraws;
    private int twoPlayed;
    public Player(int id){
        this.playerID = id;
        this.cards = new ArrayList<>();
        this.score = 0;
        this.numDraws = 0;
        this.twoPlayed = 0;
    }
    public Player(int id,String un){
        this.playerID = id;
        this.cards = new ArrayList<>();
        this.score = 0;
        this.username = un;
        this.numDraws = 0;
        this.twoPlayed = 0;
    }
    public int getID(){
        return this.playerID;
    }
    public void addCard(Card c){
        this.cards.add(c);
    }
    public Card getCard(int i){
        return this.cards.get(i);
    }
    public void setScore(int num){
        this.score = num;
    }
    public int getScore(){
        return this.score;
    }
    public void addScore(int num){
        this.score += num;
    }

    public String getUsername(){
        return this.username;
    }
    public void setUsername(String un){
        this.username = un;
    }

    public int handSize() {
        return this.cards.size();
    }

    public int getCardIndex(String r, String s){
        int i=0;
        for(int j=0;j<cards.size();j++){
            if(cards.get(j).getSuit().equals(s)&&cards.get(j).getRank().equals(r)){
                i = j;
            }
        }
        return i;
    }
    public void setCards(ArrayList<Card> c){
        this.cards = c;
    }
    public void resetCards(){
        this.cards = new ArrayList<>();
    }
    public int getNumDraws() {
        return numDraws;
    }

    public void setNumDraws(int numDraws) {
        this.numDraws = numDraws;
    }

    public int getTwoPlayed() {
        return twoPlayed;
    }

    public void setTwoPlayed(int twoPlayed) {
        this.twoPlayed = twoPlayed;
    }
}
