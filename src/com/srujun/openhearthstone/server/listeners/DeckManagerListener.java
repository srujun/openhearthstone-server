package com.srujun.openhearthstone.server.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.srujun.openhearthstone.server.OHServer;
import com.srujun.openhearthstone.server.packets.DeckManagerPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeckManagerListener  extends Listener {

    public DeckManagerListener() {
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof DeckManagerPacket.GetDecks) {
            // Generate a deck query for the connected user.
            // BasicDBObject deckQuery = new BasicDBObject("username", connection.username);
            BasicDBObject deckQuery = new BasicDBObject("username", connection.toString());
            // Get the "decks" field from the user's document.
            BasicDBList decksList = (BasicDBList) OHServer.getCollection(OHServer.Collections.USERS).findOne(deckQuery).get("decks");
            connection.sendTCP(parseDecks(decksList));
        }

        if(object instanceof DeckManagerPacket.GetClasses) {
            // Generate a classes query for the connected user.
            BasicDBObject classesQuery = new BasicDBObject("collectible", 1);

            DBCursor classesCursor = OHServer.getCollection(OHServer.Collections.HEROES).find(classesQuery);
            connection.sendTCP(parseClasses(classesCursor));
        }

        if(object instanceof DeckManagerPacket.NewDeck) {
            DeckManagerPacket.NewDeck request = (DeckManagerPacket.NewDeck) object;

        }
    }

    private List<DeckManagerPacket.Deck> parseDecks(BasicDBList decksList) {
        // Initialize a temporary decks array.
        List<DeckManagerPacket.Deck> decks = new ArrayList<DeckManagerPacket.Deck>();

        if(decksList.isEmpty()) {
            return decks;
        }
        for(Object deckObj : decksList) {
            BasicDBObject deckDBObj = (BasicDBObject) deckObj;

            // Create temporary Deck object that will be added to the List.
            DeckManagerPacket.Deck deck = new DeckManagerPacket.Deck();
            deck.name = (String) deckDBObj.get("name");
            // Add the id and the hero class to the deck object.
            int id = (Integer) deckDBObj.get("heroId");
            deck.heroId = id;
            BasicDBObject idQuery = new BasicDBObject("id", id);
            deck.hero = (String) OHServer.getCollection(OHServer.Collections.HEROES).findOne(idQuery).get("className");

            // Load the cards list from the DB's deck object.
            BasicDBList cardsList = (BasicDBList) deckDBObj.get("cards");
            // Initialize the cards list in the temporary Deck object
            deck.cards = new ArrayList<Integer>(30);
            // Populate the temporary Deck's cards list from the DB's deck's cards
            for(Object cardObj : cardsList) {
                deck.cards.add((Integer) cardObj);
            }
            // Finally, add the temporary deck object to the deck.
            decks.add(deck);
        }
        return decks;
    }

    private List<DeckManagerPacket.Classs> parseClasses(DBCursor classesCursor) {
        List<DeckManagerPacket.Classs> classsList = new ArrayList<DeckManagerPacket.Classs>(9);

        while(classesCursor.hasNext()) {
            BasicDBObject classDBObj = (BasicDBObject) classesCursor.next();
            DeckManagerPacket.Classs classs = new DeckManagerPacket.Classs();
            classs.name = (String) classDBObj.get("className");
            classs.heroId = (Integer) classDBObj.get("id");
            classs.classId = (Integer) classDBObj.get("classs");
            classs.heroImage = (String) classDBObj.get("image");
            classsList.add(classs);
        }

        // Sort the Classs List
        Collections.sort(classsList, new Comparator<DeckManagerPacket.Classs>() {
            @Override
            public int compare(DeckManagerPacket.Classs a, DeckManagerPacket.Classs b) {
                int aIndex = Integer.parseInt(a.heroImage.substring(6));
                int bIndex = Integer.parseInt(b.heroImage.substring(6));
                return aIndex < bIndex ? -1 : (aIndex > bIndex ? 1 : 0);
            }
        });

        return classsList;
    }
}
