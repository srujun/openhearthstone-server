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
            DeckManagerPacket.Deck newDeck = ((DeckManagerPacket.NewDeck) object).deck;

            // Parse the new Deck into a BasicDBObject to store in the DB.
            BasicDBObject newDeckDBObj = new BasicDBObject("name", newDeck.name)
                    .append("heroId", newDeck.classs.heroId)
                    .append("cards", new BasicDBList()); // Empty deck list
            // Generate a user query for the connected user.
            BasicDBObject userQuery = new BasicDBObject("username", connection.toString());
            // Create a copy of the user's current DB document.
            BasicDBObject userDbObj = (BasicDBObject) OHServer.getCollection(OHServer.Collections.USERS).findOne(userQuery);
            // Add the new deck object to the copied user's DB document.
            ((BasicDBList) userDbObj.get("decks")).add(newDeckDBObj);
            // Update the user's DB document with the new copy and it's added deck.
            OHServer.getCollection(OHServer.Collections.USERS).update(userQuery, userDbObj);

            // Create the response EditDeck object.
            DeckManagerPacket.EditDeck deckToEdit = new DeckManagerPacket.EditDeck();
            // Create the Deck object in deckToEdit.
            deckToEdit.deck = new DeckManagerPacket.Deck();
            deckToEdit.deck.name = newDeck.name;
            deckToEdit.deck.classs = newDeck.classs;
            deckToEdit.deck.cards = new ArrayList<DeckManagerPacket.Card>(30);

            // ADD CARD SET

            connection.sendTCP(deckToEdit);
        }

        if(object instanceof DeckManagerPacket.EditDeck) {
            DeckManagerPacket.EditDeck deckToEdit = (DeckManagerPacket.EditDeck) object;

            // Generate a user query.
            BasicDBObject userQuery = new BasicDBObject("username", connection.toString());
            // Get list of decks in user's DB.
            BasicDBList decksList = (BasicDBList) OHServer.getCollection(OHServer.Collections.USERS).findOne(userQuery).get("decks");

            // Create a DBObject to hold the corresponding object (based on the supplied deck name) from the user's DB.
            BasicDBObject deckToEditObj = new BasicDBObject();
            for(Object obj : decksList) {
                BasicDBObject deckObj = (BasicDBObject) obj;
                if(deckObj.get("name").equals(deckToEdit.deck.name)) {
                    deckToEditObj = deckObj;
                    break;
                }
            }

            // Create the Cards List in deckToEdit.
            BasicDBList cardObjList = (BasicDBList) deckToEditObj.get("cards");
            ArrayList<Integer> cardsIds = new ArrayList<Integer>(30);
            for(Object cardObj : cardObjList) {
                cardsIds.add((Integer) cardObj);
            }
            deckToEdit.deck.cards = parseCards(cardsIds);

            connection.sendTCP(deckToEdit);
        }
    }

    private List<DeckManagerPacket.Deck> parseDecks(BasicDBList decksList) {
        // Create a decks array that will be sent to the client.
        List<DeckManagerPacket.Deck> decks = new ArrayList<DeckManagerPacket.Deck>();

        if(decksList.isEmpty()) {
            return decks;
        }
        for(Object deckObj : decksList) {
            BasicDBObject deckDBObj = (BasicDBObject) deckObj;

            // Create temporary Deck object that will be added to the List.
            DeckManagerPacket.Deck deck = new DeckManagerPacket.Deck((String) deckDBObj.get("name"),
                    (Integer) deckDBObj.get("heroId"));
            // Create a query for the deck's className based on the heroId
            BasicDBObject idQuery = new BasicDBObject("id", deck.classs.heroId);
            BasicDBObject classs = (BasicDBObject) OHServer.getCollection(OHServer.Collections.HEROES).findOne(idQuery);
            deck.classs.name = (String) classs.get("className");
            deck.classs.classs = (Integer) classs.get("classs");
            deck.classs.heroName = (String) classs.get("name");
            deck.classs.heroImage = (String) classs.get("image");

            decks.add(deck);
        }
        return decks;
    }

    private List<DeckManagerPacket.Classs> parseClasses(DBCursor classesCursor) {
        List<DeckManagerPacket.Classs> classsList = new ArrayList<DeckManagerPacket.Classs>(9);

        while(classesCursor.hasNext()) {
            BasicDBObject classDBObj = (BasicDBObject) classesCursor.next();
            DeckManagerPacket.Classs classs = new DeckManagerPacket.Classs();
            classs.classs = (Integer) classDBObj.get("classs");
            classs.name = (String) classDBObj.get("className");

            classs.heroId = (Integer) classDBObj.get("id");
            BasicDBObject heroRequest = new BasicDBObject("id", classs.heroId);
            classs.heroName = (String) OHServer.getCollection(OHServer.Collections.HEROES).findOne(heroRequest).get("name");
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

    private List<DeckManagerPacket.Card> parseCards(List<Integer> cardIds) {
        List<DeckManagerPacket.Card> cardsList = new ArrayList<DeckManagerPacket.Card>(30);

        for(int cardId : cardIds) {
            BasicDBObject cardQuery = new BasicDBObject("id", cardId).append("collectible", 1);
            BasicDBObject cardObj = (BasicDBObject) OHServer.getCollection(OHServer.Collections.CARDS).findOne(cardQuery);

            DeckManagerPacket.Card card;
            switch((Integer) cardObj.get("type")) {
                case 4: // Minion
                    card = new DeckManagerPacket.Card(DeckManagerPacket.Card.CardType.MINION);
                    card.cost = (Integer) cardObj.get("cost");
                    card.attack = (Integer) cardObj.get("attack");
                    card.health = (Integer) cardObj.get("health");
                    break;
                case 5: // Spell
                    card = new DeckManagerPacket.Card(DeckManagerPacket.Card.CardType.SPELL);
                    card.cost = (Integer) cardObj.get("cost");
                    break;
                case 7: // Weapon
                    card = new DeckManagerPacket.Card(DeckManagerPacket.Card.CardType.WEAPON);
                    card.durability = (Integer) cardObj.get("durability");
                    break;
                default:
                    card = new DeckManagerPacket.Card();
            }
            card.id = cardId;
            card.image = (String) cardObj.get("image");
            card.quality = (Integer) cardObj.get("quality");
            card.name = (String) cardObj.get("name");
            card.description = (String) cardObj.get("description");
            card.classs = cardObj.get("class") == null ? -1 : (Integer) cardObj.get("class");
            card.race = cardObj.get("race") == null ? -1 : (Integer) cardObj.get("race");

            cardsList.add(card);
        }

        return cardsList;
    }
}
