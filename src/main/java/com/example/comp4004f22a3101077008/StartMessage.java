package com.example.comp4004f22a3101077008;

public class StartMessage {
    private String content;
    private String topCard;
    private String card1;
    private String card2;
    private String card3;
    private String card4;
    private String id1;
    private String score1;
    private String id2;
    private String score2;
    private String id3;
    private String score3;
    private String id4;
    private String score4;

    private String direction;

    private String currentPlayer;
    public StartMessage() {
    }

    public StartMessage(String content, String topCard, String id1,String card1,String id2,String card2,String id3, String card3, String id4, String card4, String score1, String score2, String score3, String score4, String direction, String currentPlayer) {
        this.content = content;
        this.topCard = topCard;
        this.id1 = id1;
        this.card1 = card1;
        this.id2 = id2;
        this.card2 = card2;
        this.id3 = id3;
        this.card3 = card3;
        this.id4 = id4;
        this.card4 = card4;
        this.score1 = score1;
        this.score2 = score2;
        this.score3 = score3;
        this.score4 = score4;
        this.direction = direction;
        this.currentPlayer = currentPlayer;
    }

    public String getContent() {
        return content;
    }
    public String getTopCard(){return topCard;}
    public String getCard1(){return card1;}
    public String getCard2(){return card2;}
    public String getCard3(){return card3;}
    public String getCard4(){return card4;}
    public String getId1(){return id1;}
    public String getScore1(){return score1;}
    public String getId2(){return id2;}
    public String getScore2(){return score2;}
    public String getId3(){return id3;}
    public String getScore3(){return score3;}
    public String getId4(){return id4;}
    public String getScore4(){return score4;}
    public String getDirection(){return direction;}
    public String getCurrentPlayer(){return currentPlayer;}
}
