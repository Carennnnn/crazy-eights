package com.example.comp4004f22a3101077008;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Objects;

@Controller
public class GameController {
    GameLogic game;

    GameData gd;
    public GameController(GameLogic gl, GameData gx){
        this.game = gl;
        this.gd = gx;
    }
    int numPlayers = 0;
    @MessageMapping("/connect")
    @SendTo("/topic/message")
    public Message connect(ConnectionMessage message) throws Exception {
        numPlayers++;
        if(numPlayers==1){
            game.populateDeck(gd.getCards());
            game.shuffleDeck(gd.getCards());
            gd.setTopCard(game.startSetTopCard(gd.getCards()));
        }
        if(numPlayers>4){
            return new Message("game started","");
        }
        else{
            Thread.sleep(500);
            Player p = new Player(numPlayers);
            gd.addPlayer(p);
            game.startDealCards(gd.getCards(),gd.getPlayers(),numPlayers-1);
            return new Message("id",String.valueOf(p.getID()));
        }
    }

    @MessageMapping("/start")
    @SendTo("/topic/message")
    public StartMessage start(ConnectionMessage message) throws Exception{
        ArrayList<String> cards = new ArrayList<>();
        for(int i=0;i<numPlayers;i++){
            StringBuilder car = new StringBuilder();
            for(Card c : gd.getPlayers().get(i).cards){
                car.append(" ").append(c.getRank()).append(c.getSuit());
            }
            cards.add(car.toString());
        }
        return new StartMessage("Start",gd.getTopCard().getRank()+gd.getTopCard().getSuit(),"1", cards.get(0), "2",cards.get(1),"3",cards.get(2),"4",cards.get(3),String.valueOf(gd.getPlayers().get(0).getScore()),String.valueOf(gd.getPlayers().get(1).getScore()),String.valueOf(gd.getPlayers().get(2).getScore()),String.valueOf(gd.getPlayers().get(3).getScore()), gd.getDirection(), String.valueOf(gd.getCurrentPlayer()));
    }

    @MessageMapping("/playCard")
    @SendTo("/topic/message")
    public PlayMessage playCard (ConnectionMessage message) throws Exception{
        String [] msg = message.getMessage().split(" ");
        int id = Integer.parseInt(msg[0]);
        String [] car = msg[1].split("");
        int ci = gd.getPlayers().get(id-1).getCardIndex(car[0],car[1]);
        //  game.resetDraw(gd.getPlayers(),id);
        gd.setNumTurns(gd.getNumTurns()+1);
        if(car[0].equals("8")){
            if(gd.getNumTwoPlayed()>0){
                gd.setNumTwoPlayed(0);
                game.resetDraw(gd.getPlayers(),id);
                gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));
                StringBuilder card = new StringBuilder();
                for(Card c : gd.getPlayers().get(id-1).cards){
                    card.append(" ").append(c.getRank()).append(c.getSuit());
                }
                // add game over function
                return new PlayMessage("8Played",String.valueOf(id),card.toString(), gd.getTopCard().getRank()+gd.getTopCard().getSuit());
            }
            gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));
            game.resetDraw(gd.getPlayers(),id);
            if(gd.getPlayers().get(id-1).handSize()==0){
                return getPlayMessage();
            }
            else{
                setNextTurn();
                StringBuilder card = new StringBuilder();
                for(Card c : gd.getPlayers().get(id-1).cards){
                    card.append(" ").append(c.getRank()).append(c.getSuit());
                }
                // add game over function
                return new PlayMessage("8Played",String.valueOf(id),card.toString(), gd.getTopCard().getRank()+gd.getTopCard().getSuit());
            }
        }
        else{
            String res = game.playCard(gd.getPlayers().get(id-1), msg[1],gd.getTopCard());
            if(gd.getNumTurns()>1){
                if(gd.getTopCard().getRank().equals("2")){
                    if(gd.getPlayers().get(id-1).getTwoPlayed()==0){
                        game.resetDraw(gd.getPlayers(),id);
                        gd.getPlayers().get(id-1).setTwoPlayed(gd.getPlayers().get(id-1).getTwoPlayed()+1);
                        gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));

                        gd.setTotalTwoPlayed(0);
                        StringBuilder card = new StringBuilder();
                        for(Card c : gd.getPlayers().get(id-1).cards){
                            card.append(" ").append(c.getRank()).append(c.getSuit());
                        }
                        return new PlayMessage("1Played",String.valueOf(id),card.toString(),gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                    }
                }
                gd.setNumTwoPlayed(0);
            }
            switch(res){
                case "APlayed"->{
                    game.resetDraw(gd.getPlayers(),id);
                    gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));
                    if(gd.getPlayers().get(id-1).handSize()==0){
                        return getPlayMessage();
                    }
                    else{
                        if(gd.getDirection().equals("left")){
                            gd.setDirection("right");
                        }
                        else{
                            gd.setDirection("left");
                        }

                        StringBuilder card = new StringBuilder();
                        for(Card c : gd.getPlayers().get(id-1).cards){
                            card.append(" ").append(c.getRank()).append(c.getSuit());
                        }
                        if(gd.getNumTwoPlayed()>0){
                            gd.setTotalTwoPlayed(0);
                            gd.setNumTwoPlayed(0);
                        }
                        else{
                            gd.setTotalTwoPlayed(0);
                            setNextTurn();
                        }
                        return new PlayMessage("APlayed",String.valueOf(id),card.toString(),String.valueOf(gd.getCurrentPlayer()),gd.getDirection(),gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                    }
                }
                case "Played"->{
                    game.resetDraw(gd.getPlayers(),id);
                    gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));

                    if(gd.getPlayers().get(id-1).handSize()==0){
                        return getPlayMessage();
                    }
                    else{

                        if(gd.getNumTwoPlayed()>0){
                            gd.setTotalTwoPlayed(0);
                            gd.setNumTwoPlayed(0);
                        }
                        else{
                            gd.setTotalTwoPlayed(0);
                            setNextTurn();
                        }
                        StringBuilder card = new StringBuilder();
                        for(Card c : gd.getPlayers().get(id-1).cards){
                            card.append(" ").append(c.getRank()).append(c.getSuit());
                        }

                        return new PlayMessage("Played",String.valueOf(id), card.toString(),String.valueOf(gd.getCurrentPlayer()), gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                    }
                }
                case "2Played"->{
                    game.resetDraw(gd.getPlayers(),id);
                    gd.setNumTwoPlayed(gd.getNumTwoPlayed()+1);
                    gd.setTotalTwoPlayed(gd.getTotalTwoPlayed()+gd.getNumTwoPlayed());
                    gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));
                    if(gd.getPlayers().get(id-1).handSize()==0){
                        return getPlayMessage();
                    }
                    else{
                        setNextTurn();
                        StringBuilder card = new StringBuilder();
                        for(Card c : gd.getPlayers().get(id-1).cards){
                            card.append(" ").append(c.getRank()).append(c.getSuit());
                        }
                        int numCards = 0;
                        Card tc = new Card();
                        for(Card c : gd.getPlayers().get(gd.getCurrentPlayer()-1).cards){
                            if(gd.getTopCard().getRank().equals(c.getRank())||gd.getTopCard().getSuit().equals(c.getSuit())|| c.getRank().equals("8") || tc.getRank().equals(c.getRank()) || tc.getSuit().equals(c.getSuit())){
                                tc = c;
                                numCards++;
                            }
                        }
                        if(numCards<2){
                            int extraDrawID = gd.getCurrentPlayer();
                            gd.getPlayers().get(gd.getCurrentPlayer()-1).setTwoPlayed(3);
                            if(gd.getCards().size()>=2){
                                int numDraws = 0;
                                numDraws = 2 * gd.getTotalTwoPlayed();
                                for(int k=0;k<numDraws;k++){
                                    game.drawCard(gd.getCards(),gd.getPlayers().get(extraDrawID-1));
                                }
                                StringBuilder card2 = new StringBuilder();
                                for(Card c : gd.getPlayers().get(extraDrawID-1).cards){
                                    card2.append(" ").append(c.getRank()).append(c.getSuit());
                                }

                                gd.setNumTwoPlayed(0);
                                return new PlayMessage("2 Played 1", String.valueOf(id),card.toString(),String.valueOf(gd.getCurrentPlayer()),card2.toString(),gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                            }
                            else{

                                return getPlayMessage();
                            }
                        }
                        else{
                            gd.getPlayers().get(gd.getCurrentPlayer()-1).setTwoPlayed(0);
                            return new PlayMessage("Played",String.valueOf(id), card.toString(),String.valueOf(gd.getCurrentPlayer()), gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                        }

                    }
                }
                case "QPlayed" ->{
                    game.resetDraw(gd.getPlayers(),id);
                    gd.setTopCard(gd.getPlayers().get(id-1).cards.remove(ci));
                    if(gd.getPlayers().get(id-1).handSize()==0){
                        return getPlayMessage();
                    }
                    else{
                        if(gd.getNumTwoPlayed()>0){
                            gd.setTotalTwoPlayed(0);
                            gd.setNumTwoPlayed(0);
                        }
                        else{
                            gd.setTotalTwoPlayed(0);
                            if(gd.getDirection().equals("right")){
                                gd.setCurrentPlayer(gd.getCurrentPlayer()-2);
                                if(gd.getCurrentPlayer()==0){
                                    gd.setCurrentPlayer(4);
                                }
                                else if(gd.getCurrentPlayer()<0){
                                    gd.setCurrentPlayer(3);
                                }
                            }
                            else{
                                gd.setCurrentPlayer(gd.getCurrentPlayer()+2);
                                if(gd.getCurrentPlayer()>4){
                                    if(gd.getCurrentPlayer()==6){
                                        gd.setCurrentPlayer(2);
                                    }
                                    else{
                                        gd.setCurrentPlayer(1);
                                    }
                                }
                            }
                        }
                        StringBuilder card = new StringBuilder();
                        for(Card c : gd.getPlayers().get(id-1).cards){
                            card.append(" ").append(c.getRank()).append(c.getSuit());
                        }
                        return new PlayMessage("Played",String.valueOf(id),card.toString(),String.valueOf(gd.getCurrentPlayer()), gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                    }
                }
                default -> {
                    return new PlayMessage("NotPlayed",String.valueOf(id),"Invalid Entry", gd.getTopCard().getRank()+gd.getTopCard().getSuit());
                }
            }
        }

    }

    private PlayMessage getPlayMessage() {
        game.calculateScore(gd.getPlayers());
        if(checkRound()){
            //game over
            Player winner = new Player(0);
            winner.setScore(300);
            for(Player p: gd.getPlayers()){
                if(p.getScore()<winner.getScore()){
                    winner = p;
                }
            }
            return new PlayMessage("Game Over",String.valueOf(gd.getPlayers().get(0).getScore()),String.valueOf(gd.getPlayers().get(1).getScore()),String.valueOf(gd.getPlayers().get(2).getScore()),String.valueOf(gd.getPlayers().get(3).getScore()),String.valueOf(winner.getID()));
        }
        else{
            ArrayList<Card> tmp = new ArrayList<>();
            gd.setCards(tmp);
            game.populateDeck(gd.getCards());
            game.shuffleDeck(gd.getCards());
            gd.setTopCard(game.startSetTopCard(gd.getCards()));
            gd.setNumTurns(0);
            gd.setTotalTwoPlayed(0);
            gd.setNumTwoPlayed(0);
            gd.setDirection("left");
            for(Player p:gd.getPlayers()){
                p.resetCards();
                game.startDealCards(gd.getCards(),gd.getPlayers(),p.getID()-1);
            }
            setNextRound();
            gd.setCurrentPlayer(gd.getNextRound());
            return new PlayMessage("Round Over",String.valueOf(gd.getPlayers().get(0).getScore()),String.valueOf(gd.getPlayers().get(1).getScore()),String.valueOf(gd.getPlayers().get(2).getScore()),String.valueOf(gd.getPlayers().get(3).getScore()),String.valueOf(gd.getNextRound()));
        }
    }

    @MessageMapping("/drawCard")
    @SendTo("/topic/message")
    public DrawMessage drawCard(ConnectionMessage message) throws Exception{
        int id = Integer.parseInt(message.getMessage());
        boolean gameState = game.drawCard(gd.getCards(),gd.getPlayers().get(id-1));
        game.incrementDraw(gd.getPlayers(),id);
        if(gameState){
            if(gd.getPlayers().get(id-1).getNumDraws()>=3){
                game.resetDraw(gd.getPlayers(),id);
                Player p = gd.getPlayers().get(id-1);
                String card = p.getCard(p.handSize()-1).getRank()+ p.getCard(p.handSize()-1).getSuit();
                if(gd.getTopCard().getRank().equals(p.getCard(p.handSize()-1).getRank()) || gd.getTopCard().getSuit().equals(p.getCard(p.handSize()-1).getSuit()) || Objects.equals(p.getCard(p.handSize() - 1).getRank(), "8")) {
                    return new DrawMessage("noMoreDraw", message.getMessage(), card);
                }
                boolean playable = false;
                for(Card c : p.cards){
                    if(gd.getTopCard().getSuit().equals(c.getSuit())||gd.getTopCard().getRank().equals(c.getRank())||c.getRank().equals("8")){
                        playable = true;
                    }
                }
                if(playable){
                    return new DrawMessage("Playable", message.getMessage(), card);
                }
                else{
                    setNextTurn();
                    return new DrawMessage("Not Playable", message.getMessage(), card,String.valueOf(gd.getCurrentPlayer()));
                }
            }
            else{
                Player p = gd.getPlayers().get(id-1);
                String card = p.getCard(p.handSize()-1).getRank()+ p.getCard(p.handSize()-1).getSuit();
                if(gd.getTopCard().getRank().equals(p.getCard(p.handSize()-1).getRank()) || gd.getTopCard().getSuit().equals(p.getCard(p.handSize()-1).getSuit()) || Objects.equals(p.getCard(p.handSize() - 1).getRank(), "8")) {
                    return new DrawMessage("noMoreDraw", message.getMessage(), card);
                }
                else{
                    return new DrawMessage("draw", message.getMessage(), card);
                }
            }
        }
        else{
            setNextRound();
            game.resetDraw(gd.getPlayers(),id);
            gd.setCurrentPlayer(gd.getNextRound());
            gd.setTotalTwoPlayed(0);
            gd.setNumTwoPlayed(0);
            game.calculateScore(gd.getPlayers());
            gd.setDirection("left");

            if(gd.getPlayers().get(id-1).handSize()==0){
                Player winner = new Player(0);
                winner.setScore(300);
                for(Player p: gd.getPlayers()){
                    if(p.getScore()<winner.getScore()){
                        winner = p;
                    }
                }
                return new DrawMessage("Game Over","1",String.valueOf(gd.getPlayers().get(0).getScore()),"2",String.valueOf(gd.getPlayers().get(1).getScore()),"3",String.valueOf(gd.getPlayers().get(2).getScore()),"4",String.valueOf(gd.getPlayers().get(3).getScore()),String.valueOf(gd.getNextRound()),String.valueOf(winner.getID()));
            }
            else{
                return new DrawMessage("Round Over","1",String.valueOf(gd.getPlayers().get(0).getScore()),"2",String.valueOf(gd.getPlayers().get(1).getScore()),"3",String.valueOf(gd.getPlayers().get(2).getScore()),"4",String.valueOf(gd.getPlayers().get(3).getScore()),String.valueOf(gd.getNextRound()));
            }
        }
    }

    @MessageMapping("/changeSuit")
    @SendTo("/topic/message")
    public ChangeSuitMessage changeSuit(ConnectionMessage message) throws Exception{
        Card c = new Card(message.getMessage(),"");
        gd.setTopCard(c);
        return new ChangeSuitMessage("ChangeSuit",gd.getTopCard().getSuit(),String.valueOf(gd.getCurrentPlayer()));
    }

    public void setNextTurn() {
        if(gd.getDirection().equals("right")){
            gd.setCurrentPlayer(gd.getCurrentPlayer()-1);
            if(gd.getCurrentPlayer()<1){
                gd.setCurrentPlayer(4);
            }
        }
        else{
            gd.setCurrentPlayer(gd.getCurrentPlayer()+1);
            if(gd.getCurrentPlayer()>4){
                gd.setCurrentPlayer(1);
            }
        }
    }

    public void setNextRound(){
        System.out.println("next round: "+gd.getNextRound());
        gd.setNextRound(gd.getNextRound()+1);
        if(gd.getNextRound()>4){
            gd.setNextRound(1);
        }
    }

    public boolean checkRound(){
        for(Player p : gd.getPlayers()){
            if(p.getScore()>=100){
                return true;
            }
        }
        return false;
    }
}
