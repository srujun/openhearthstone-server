package com.srujun.openhearthstone.server;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
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

    public CardDatabaseManager() {

    }

    public void updateDatabase() {
        // Reset counters.
        cardsUpdated = cardsAdded = 0;

        // Update collectible cards
        updateCards(this.HEARTHHEAD_CARDS_COLLECTIBLE);
        // Update uncollectible cards
        updateCards(this.HEARTHHEAD_CARDS_UNCOLLECTIBLE);

        System.out.println("[DB] Cards updated: " + cardsUpdated);
        System.out.println("[DB] Cards added: " + cardsAdded);
    }

    private void updateCards(String url) {
        DBCollection cardsCol = OHServer.getDB().getCollection("hh-cards");

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
                BasicDBObject similarCard = (BasicDBObject)cardsCol.findOne(query);

                if(similarCard == null) {
                    // Card isn't in DB. Let's add it!
                    System.out.println("[DB] New card: " + newCard.get("id") + "-" + newCard.get("name"));
                    cardsCol.insert(newCard);
                    cardsAdded++;
                } else {
                    // Card needs to be updated!
                    System.out.println("[DB] Card updated: " + newCard.get("id") + "-" + newCard.get("name"));
                    cardsCol.update(query, newCard);
                    cardsUpdated++;
                }
            }
        }
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
