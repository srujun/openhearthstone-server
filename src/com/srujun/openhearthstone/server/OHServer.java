package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryonet.Server;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.srujun.openhearthstone.server.listeners.LoginListener;

import java.io.IOException;
import java.net.UnknownHostException;

public class OHServer {
    public static final int PORT = 6391;

    private Server server;

    private MongoClientURI mongoClientURI;
    private static MongoClient mongo;
    private static DB db;

    public static OHServer instance;

    private OHServer() {
        server = new Server();
        server.addListener(new LoginListener());
        KryoPackets.registerPacketObjects(server);

        // Start Kryo Server
        try {
            server.bind(PORT);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public static DB getDB() {
        return db;
    }

    public static void main(String args[]) {
       instance = new OHServer();
    }
}
