package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.util.ArrayList;

public class KryoPackets {

    public static void registerPacketObjects(Server server) {
        Kryo kryo = server.getKryo();

        kryo.register(Credentials.class);
        kryo.register(Credentials.InvalidUsername.class);
        kryo.register(Credentials.UserExists.class);
        kryo.register(Credentials.UsernameNotFound.class);
        kryo.register(Credentials.LoggedIn.class);
    }

    static public class Credentials {
        static public class InvalidUsername {
        }

        static public class UserExists {
        }

        static public class UsernameNotFound {
        }

        static public class LoggedIn {
        }

        public boolean isNewUsername;
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
