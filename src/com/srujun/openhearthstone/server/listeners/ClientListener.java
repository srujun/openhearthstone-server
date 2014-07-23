package com.srujun.openhearthstone.server.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.srujun.openhearthstone.server.OHServer;
import com.srujun.openhearthstone.server.packets.Credentials;
import com.srujun.openhearthstone.server.packets.DeckManager;

public class ClientListener extends Listener {

    public ClientListener() {
    }

    @Override
    public void received(Connection connection, Object object) {
        // OHConnection connection = (OHConnection) c;

        if(object instanceof Credentials) {
            Credentials cred = (Credentials) object;

            if(cred.isNewUsername) {
                // If new user.
                // TODO: Check for username validity

                // Find new user in database to catch duplicates.
                BasicDBObject newUserQuery = new BasicDBObject("username", cred.username);
                if(OHServer.getCollection(OHServer.Collections.USERS).find(newUserQuery).count() == 0) {
                    // Add new user to database.
                    OHServer.getCollection(OHServer.Collections.USERS).insert(newUserQuery);
                    // Tell client that user is logged in.
                    //connection.username = cred.username;
                    connection.sendTCP(new Credentials.LoggedIn());
                    return;
                } else {
                    // This username exists! Ask client for new username.
                    connection.sendTCP(new Credentials.UserExists());
                    return;
                }
            } else {
                // If existing user.
                BasicDBObject query = new BasicDBObject("username", cred.username);
                if(OHServer.getCollection(OHServer.Collections.USERS).find(query).count() == 0) {
                    // Username not found in database!
                    connection.sendTCP(new Credentials.UsernameNotFound());
                    return;
                } else {
                    // Tell client that user is logged in.
                    //connection.username = cred.username;
                    connection.sendTCP(new Credentials.LoggedIn());
                    return;
                }
            }
        }

        if(object instanceof DeckManager.GetDecks) {
            DeckManager.GetDecks request = (DeckManager.GetDecks) object;

            // Generate a deck query for the connected user.
            // BasicDBObject deckQuery = new BasicDBObject("username", connection.username);
            BasicDBObject deckQuery = new BasicDBObject("username", request.username);
            // Get the "decks" field from the user's document.
            BasicDBList decksList = (BasicDBList) OHServer.getCollection(OHServer.Collections.USERS).findOne(deckQuery).get("decks");
            connection.sendTCP(parseDecks(decksList));
        }
    }

    private DeckManager.Deck[] parseDecks(BasicDBList decksList) {
        // Initialize a temporary decks array to given size.
        DeckManager.Deck[] decks = new DeckManager.Deck[decksList.size()];
        // For every card in the given DBList.
        for(int i = 0; i < decksList.size(); i++) {
            BasicDBObject deck = (BasicDBObject) decksList.get(Integer.toString(i));
            decks[i] = new DeckManager.Deck();
            decks[i].name = (String) deck.get("name");
            decks[i].hero = (Integer) deck.get("hero");

            BasicDBList cards = (BasicDBList) deck.get("cards");
            for(int j = 0; i < cards.size(); j++) {
                decks[i].cards[j] = (Integer) cards.get(j);
            }
        }
        return decks;
    }
}
