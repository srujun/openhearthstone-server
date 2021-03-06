package com.srujun.openhearthstone.server.packets;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KryoPackets {

    public static void registerPacketClasses(Server server) {
        Kryo kryo = server.getKryo();

        kryo.register(CredentialsPacket.class);
        kryo.register(CredentialsPacket.LoginResponse.class);
        kryo.register(CredentialsPacket.LoginResponse.Response.class);

        kryo.register(DeckManagerPacket.class);
        kryo.register(DeckManagerPacket.GetDecks.class);
        kryo.register(DeckManagerPacket.Deck.class);
        kryo.register(DeckManagerPacket.GetClasses.class);
        kryo.register(DeckManagerPacket.Classs.class);
        kryo.register(DeckManagerPacket.NewDeck.class);
        kryo.register(DeckManagerPacket.EditDeck.class);
        kryo.register(DeckManagerPacket.Card.class);
        kryo.register(List.class);
        kryo.register(ArrayList.class);
        kryo.register(Set.class);
        kryo.register(HashSet.class);
    }

    // Make KryoPackets a singleton
    private KryoPackets() {
    }
}
