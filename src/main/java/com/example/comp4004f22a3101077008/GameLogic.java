package com.example.comp4004f22a3101077008;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
@Component
public class GameLogic{

    public Card startGame(ArrayList<Card> cards,ArrayList<Player> players){
        for(Player p: players){
            p.cards.clear();
        }
        Card topCard = cards.remove(0);
        while(topCard.getRank().equals("8")){
            Card tmp = topCard;
            cards.add(tmp);
            topCard = cards.remove(0);
        }
        dealCards(cards,players);
        return topCard;
    }

    public void startDealCards(ArrayList<Card> cards, ArrayList<Player> players, int id){
        players.get(id).cards.clear();
        for(int i=0;i<5;i++){
            players.get(id).addCard(cards.remove(0));
        }
    }
    public Card startSetTopCard(ArrayList<Card> cards){
        Card topCard = cards.remove(0);
        while(topCard.getRank().equals("8")){
            Card tmp = topCard;
            cards.add(tmp);
            topCard = cards.remove(0);
        }
        return topCard;
    }
    public void populateDeck(ArrayList<Card> cards) {
        String [] suit = {"S","C","D","H"};
        String [] rank = {"A","2","3","4","5","6","7","8","9","T","J","Q","K"};
        for (String s : suit) {
            for (String value : rank) {
                Card c = new Card(s, value);
                cards.add(c);
            }
        }
    }

    public void shuffleDeck(ArrayList<Card> cards) {
        Collections.shuffle(cards);
    }

    public boolean dealCards(ArrayList<Card> cards, ArrayList<Player> players) {
        if(cards.size() <(players.size()*5)){
            return false;
        }
        else{
            for(Player p : players){
                for(int i=0;i<5;i++){
                    p.addCard(cards.remove(0));
                }
            }
            return true;
        }
    }
    public boolean drawCard(ArrayList<Card> cards, Player p){
        if(cards.size()==0){
            return false;
        }
        else{
            p.addCard(cards.remove(0));
            return true;
        }
    }
    public boolean checkQueen(String r){
        return r.equals("Q");
    }
    public boolean checkAces(String r){
        return r.equals("A");
    }
    public boolean check8(String r){
        return r.equals("8");
    }
    public boolean check2(String r){
        return r.equals("2");
    }

    public String playCard(Player p, String c, Card topCard) {
        String [] m = c.split("");
        String suit = m[1];
        String rank = m[0];
        String res = "";
        if(check8(rank)){
            if(topCard.getRank().equals(rank)||topCard.getSuit().equals(suit)){
                for(int i=0;i<p.handSize();i++){
                    if(p.getCard(i).getSuit().equals(suit)&&p.getCard(i).getRank().equals(rank)){
                        res = "8Played";
                    }
                }
            }
            else{
                res = "notplayed";
            }
        }
        else if(checkAces(rank)){
            if(topCard.getRank().equals(rank)||topCard.getSuit().equals(suit)){
                for(int i=0;i<p.handSize();i++){
                    if(p.getCard(i).getSuit().equals(suit)&&p.getCard(i).getRank().equals(rank)){
                        res = "APlayed";
                    }
                }
            }
            else{
                res = "notplayed";
            }
        }
        else if(checkQueen(rank)){
            if(topCard.getRank().equals(rank)||topCard.getSuit().equals(suit)){
                for(int i=0;i<p.handSize();i++){
                    if(p.getCard(i).getSuit().equals(suit)&&p.getCard(i).getRank().equals(rank)){
                        res = "QPlayed";
                    }
                }
            }
            else{
                res = "notplayed";
            }
        }
        else if(check2(rank)){
            if(topCard.getRank().equals(rank)||topCard.getSuit().equals(suit)){
                for(int i=0;i<p.handSize();i++){
                    if(p.getCard(i).getSuit().equals(suit)&&p.getCard(i).getRank().equals(rank)){
                        res = "2Played";
                    }
                }
            }
            else{
                res = "notplayed";
            }
        }
        else{
            if(topCard.getRank().equals(rank)||topCard.getSuit().equals(suit)){
                for(int i=0;i<p.handSize();i++){
                    if(p.getCard(i).getSuit().equals(suit)&&p.getCard(i).getRank().equals(rank)){
                        res = "Played";
                    }
                }
            }
            else{
                res = "notplayed";
            }
        }
        return res;
    }

    public void calculateScore(ArrayList<Player> players){
        for(Player p : players){
            if(p.handSize()==0){
                p.setScore(p.getScore());
            }
            else{
                int score = 0;
                for(Card c : p.cards){
                    if(c.getRank().equals("Q")||c.getRank().equals("K")||c.getRank().equals("J")||c.getRank().equals("T")){
                        score += 10;
                    }
                    else if(c.getRank().equals("8")){
                        score += 50;
                    }
                    else{
                        switch (c.getRank()) {
                            case "A" -> score += 1;
                            case "2" -> score += 2;
                            case "3" -> score += 3;
                            case "4" -> score += 4;
                            case "5" -> score += 5;
                            case "6" -> score += 6;
                            case "7" -> score += 7;
                            case "9" -> score += 9;
                        }
                    }
                }
                p.setScore(score);
            }
        }
    }

    public void incrementDraw(ArrayList<Player> players, int id){
        players.get(id-1).setNumDraws(players.get(id-1).getNumDraws()+1);
    }
    public void resetDraw(ArrayList<Player> players, int id){
        players.get(id-1).setNumDraws(0);
    }
}
