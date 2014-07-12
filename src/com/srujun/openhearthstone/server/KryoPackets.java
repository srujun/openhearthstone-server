package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.util.ArrayList;

public class KryoPackets {

    public static void registerPacketObjects(Server server) {
        Kryo kryo = server.getKryo();

        kryo.register(Credentials.class);
        kryo.register(Card.class);
        kryo.register(Deck.class);
        kryo.register(Deck[].class);
    }

    static public class Credentials {
        static public class UserExists {
        }

        public String username;
    }

    static public class Card {
        public int id;
        public String image;
        public int set;
        public String icon;
        public int type;
        public int faction;
        public int quality;
        public int cost;
        public int attack;
        public int health;
        public int elite;
        public int classs;
        public int race;
        public int durability;
        public int collectible;
        public String name;
        public String description;
        public int popularity;
    }

    static public class Deck {
        public ArrayList<Card> deck;
    }

    // Make KryoPackets a singleton
    private KryoPackets() {
    }
}
