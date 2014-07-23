package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryonet.Server;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.srujun.openhearthstone.server.listeners.ClientListener;
import com.srujun.openhearthstone.server.packets.KryoPackets;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class OHServer {
    public static final int PORT = 6391;

    private static MongoClientURI mongoClientURI;
    private static MongoClient mongo;
    private static DB db;

    public static OHServer instance;

    private OHServer() {
        // Connect to DB
        try {
            mongoClientURI = new MongoClientURI(System.getenv("MONGOHQ_URL"));
            mongo = new MongoClient(mongoClientURI);
        } catch (UnknownHostException e) {
            System.out.println("Error connecting to MongoHQ.");
            System.out.println(e.getMessage());
        } finally {
            db = mongo.getDB(mongoClientURI.getDatabase());
        }
    }

    public static enum Collections {
        USERS, CARDS
    }

    public static DBCollection getCollection(Collections col) {
        switch(col) {
            case USERS: return db.getCollection("users");
            case CARDS: return db.getCollection("hh-cards");
            default: return null;
        }
    }

    public static void main(String args[]) {
        instance = new OHServer();
        List arguments = Arrays.asList(args);

        if(args.length > 0) {
            if(arguments.contains("-r")) { // Reset cards database.
                System.out.println("[DB] Starting cards database reset.");
                CardDatabaseManager cdm = new CardDatabaseManager();
                cdm.deleteDatabase();

                System.out.println("[DB] Database card count: " + OHServer.getCollection(Collections.CARDS).count());

                // Now update database...
                cdm.updateDatabase();
                System.out.println("[DB] Database card count: " + OHServer.getCollection(Collections.CARDS).count());
            }
            if(arguments.contains("-u")) { // Update cards database.
                System.out.println("[DB] Starting cards database update.");
                CardDatabaseManager cdm = new CardDatabaseManager();
                cdm.updateDatabase();

                System.out.println("[DB] Database card count: " + OHServer.getCollection(Collections.CARDS).count());
            }
        } else {
            Server server = new Server();
            server.addListener(new ClientListener());
            KryoPackets.registerPacketClasses(server);

            // Start Kryo Server
            try {
                server.bind(PORT);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
