package com.srujun.openhearthstone.server;

import com.esotericsoftware.minlog.Log;
import com.mongodb.*;
import com.mongodb.util.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardDatabaseManager {
    private final String HEARTHHEAD_CARDS_COLLECTIBLE = "http://www.hearthhead.com/cards";
    private final String HEARTHHEAD_CARDS_UNCOLLECTIBLE = "http://www.hearthhead.com/cards=?filter=uc=on";

    private int cardsUpdated;
    private int cardsAdded;
    private int cardsDeleted;

    public CardDatabaseManager() {

    }

    public void deleteDatabase() {
        // Reset counter.
        cardsDeleted = 0;

        DBCursor cardsCursor = OHServer.getCollection(OHServer.Collections.CARDS).find();

        for(DBObject card : cardsCursor) {
            OHServer.getCollection(OHServer.Collections.CARDS).remove(card);
            cardsDeleted++;
        }

        Log.info("DB", "[DB] Deleted " + cardsDeleted + " cards.");
    }

    public void updateDatabase() {
        // Reset counters.
        cardsUpdated = cardsAdded = 0;

        // Update collectible cards
        updateCards(this.HEARTHHEAD_CARDS_COLLECTIBLE);
        // Update uncollectible cards
        updateCards(this.HEARTHHEAD_CARDS_UNCOLLECTIBLE);

        Log.info("DB", "Cards updated: " + cardsUpdated);
        Log.info("DB", "Cards added: " + cardsAdded);
    }

    private void updateCards(String url) {
        DBCollection cardsCol = OHServer.getCollection(OHServer.Collections.CARDS);

        // Get cards from Hearthhead as a List
        BasicDBList newCards = getCardsAsList(url);

        for(Object cardObj : newCards) {
            BasicDBObject newCard = (BasicDBObject) cardObj;

            // Find a card in the DB that exactly matches the new card.
            BasicDBObject exactCard = (BasicDBObject)cardsCol.findOne(newCard);
            if(exactCard == null) {
                // The exact card doesn't exist in the DB, so...
                // Check if the new card's ID exists in the DB.
                BasicDBObject query = new BasicDBObject("id", newCard.get("id"));
                BasicDBObject similarCard = (BasicDBObject) cardsCol.findOne(query);

                if(similarCard == null) {
                    // Card isn't in DB. Let's add it!
                    Log.info("DB", "New card: " + newCard.get("id") + "-" + newCard.get("name"));
                    cardsCol.insert(newCard);
                    cardsAdded++;
                } else {
                    // Card with similar id exists.
                    // So card needs to be updated!
                    cardsCol.update(query, newCard);
                    Log.info("DB", "Card updated: " + newCard.get("id") + "-" + newCard.get("name") + ". " +
                            "Changes: " + printDifferences(similarCard, newCard));
                    cardsUpdated++;
                }
            }
        }
    }

    private String printDifferences(BasicDBObject origObj, BasicDBObject newObj) {
        StringBuilder sb = new StringBuilder();

        for(String key : newObj.keySet()) {
            Object origVal = origObj.get(key);
            Object newVal = newObj.get(key);
            if(!newVal.equals(origVal)) {
                sb.append(key + ":" + origVal + ">" + newVal + " | ");
            }
        }

        return sb.toString();
    }

    private BasicDBList getCardsAsList(String url) {
        // Retrieve card data.
        StringBuilder cardHtml = getPageHtml(url);

        // Find card JSON from html data
        Pattern cardVarPattern = Pattern.compile("var hearthstoneCards = .+?]");
        Matcher cardVarMatcher = cardVarPattern.matcher(cardHtml);
        cardVarMatcher.find();

        StringBuilder cardJSON = new StringBuilder(cardVarMatcher.group());
        cardJSON.delete(0, "var hearthstoneCards = ".length());

        // Replace popularity with "popularity"
        Pattern popularityPattern = Pattern.compile(",popularity:\\d*");
        Matcher popularityMatcher = popularityPattern.matcher(cardJSON);
        cardJSON = new StringBuilder(popularityMatcher.replaceAll(""));

        BasicDBList cards = ((BasicDBList) JSON.parse(cardJSON.toString()));
        return cards;
    }

    /**
     * Retrieve contents of HTML page as a StringBuilder
     * @param url The URL to retrieve content from.
     * @return The {@link java.lang.StringBuilder} object containing the contents of the URL
     */
    private StringBuilder getPageHtml(String url) {
        URL hhURL;
        URLConnection hhURLConnection;
        BufferedReader bf;
        InputStreamReader isr;
        StringBuilder sb = new StringBuilder();

        try {
            hhURL = new URL(url);
            hhURLConnection = hhURL.openConnection();

            isr = new InputStreamReader(hhURLConnection.getInputStream());
            bf = new BufferedReader(isr);
            String inLine;

            while((inLine = bf.readLine()) != null) {
                sb.append(inLine);
            }

            isr.close();
            bf.close();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return sb;
    }
}
