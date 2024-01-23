package com.example.comp4004f22a3101077008;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class PlayerTest{
    Player p = new Player(1);
    @Test
    void testGetID(){
        assertEquals(1,p.getID());
    }
    @Test
    void testAddCard(){
        Card c = new Card("H","A");
        p.addCard(c);
        assertEquals("H",p.getCard(0).getSuit());
    }
    @Test
    void testSetScore(){
        p.setScore(15);
        assertEquals(15,p.getScore());
    }
    @Test
    void testAddScore(){
        p.addScore(5);
        assertEquals(5,p.getScore());
    }
    @Test
    void testHandSize(){
        Card c = new Card("H","A");
        p.addCard(c);
        assertEquals(1,p.handSize());
    }
}
