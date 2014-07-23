package com.srujun.openhearthstone.server.packets;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

public class KryoPackets {

    public static void registerPacketClasses(Server server) {
        Kryo kryo = server.getKryo();

        kryo.register(Credentials.class);
        kryo.register(Credentials.InvalidUsername.class);
        kryo.register(Credentials.UserExists.class);
        kryo.register(Credentials.UsernameNotFound.class);
        kryo.register(Credentials.LoggedIn.class);

        kryo.register(DeckManager.class);
        kryo.register(DeckManager.GetDecks.class);
        kryo.register(DeckManager.Deck[].class);
    }

    // Make KryoPackets a singleton
    private KryoPackets() {
    }
}
