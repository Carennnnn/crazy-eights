package com.example.comp4004f22a3101077008;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest{
    Card c = new Card("H","A");
    @Test
    void testGetSuit(){
        assertEquals("H",c.getSuit());
    }
    @Test
    void testGetRank(){
        assertEquals("A",c.getRank());
    }
}
