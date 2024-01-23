package com.example.comp4004f22a3101077008;

public class PlayMessage {
    private String content;
    private String id;
    private String card;
    private String turn;
    private String direction;
    private String topCard;
    private String score1;
    private String score2;
    private String score3;
    private String score4;
    private String winner;
    private String nextRound;
    private String nextCard;
    private String extraDrawID;
    public PlayMessage() {
    }
    public PlayMessage(String content, String id, String card, String topCard){
        this.content = content;
        this.id = id;
        this.card = card;
        this.topCard = topCard;
    }
    public PlayMessage(String content, String id, String card, String turn,String direction, String topCard) {
        if(content.equals("Game Over")){
            this.content = content;
            this.score1 = id;
            this.score2 = card;
            this.score3 = turn;
            this.score4 = direction;
            this.winner = topCard;
        }
        else if(content.equals("2 Played 1")){
            this.content = content;
            this.id = id;
            this.card = card;
            this.turn = turn;
            this.nextCard = direction;
            this.topCard = topCard;
        }
        else if(content.equals("Round Over")){
            this.content = content;
            this.score1 = id;
            this.score2 = card;
            this.score3 = turn;
            this.score4 = direction;
            this.nextRound = topCard;
        }
        else{
            this.content = content;
            this.id = id;
            this.card = card;
            this.turn = turn;
            this.direction = direction;
            this.topCard = topCard;
        }
    }
    public PlayMessage(String content, String id, String card, String turn,String direction, String topCard,String extraID){
        this.content = content;
        this.id = id;
        this.card = card;
        this.turn = turn;
        this.nextCard = direction;
        this.topCard = topCard;
        this.extraDrawID = extraID;
    }
    public PlayMessage(String content, String id, String card, String turn, String topCard){
        this.content = content;
        this.id = id;
        this.card = card;
        this.turn = turn;
        this.topCard = topCard;
    }

    public String getContent() {
        return content;
    }
    public String getId (){return id;}
    public String getCard(){return card;}
    public String getTurn(){return turn;}
    public String getDirection(){return direction;}
    public String getTopCard(){return topCard;}

    public String getScore1() {
        return score1;
    }

    public String getScore2() {
        return score2;
    }

    public String getScore3() {
        return score3;
    }

    public String getScore4() {
        return score4;
    }

    public String getWinner() {
        return winner;
    }

    public String getNextRound() {
        return nextRound;
    }

    public String getNextCard() {
        return nextCard;
    }

    public String getExtraDrawID() {
        return extraDrawID;
    }

    public void setExtraDrawID(String extraDrawID) {
        this.extraDrawID = extraDrawID;
    }
}
