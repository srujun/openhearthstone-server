package com.srujun.openhearthstone.server.packets;

public class DeckManager {
    static public class GetDecks {
        public String username;
    }

    static public class Deck {
        public String name;
        public int hero;
        public int[] cards;
    }
}
