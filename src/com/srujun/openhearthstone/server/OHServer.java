package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.IOException;
import java.net.UnknownHostException;

public class OHServer {
    public static final int PORT = Integer.valueOf(System.getenv("PORT")); // 6391

    public static OHServer instance;

    private Server server;
    private MongoClient mongo;
    private MongoClientURI mongoClientURI;
    private DB db;

    private OHServer() {
        server = new Server();
        server.addListener(new TestListener());
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
            System.out.println(e.getMessage());
        } finally {
            db = mongo.getDB(mongoClientURI.getDatabase());
        }
    }

    public static void main(String args[]) {
       instance = new OHServer();
    }

    public DB getDB() {
        return db;
    }

    class TestListener extends Listener {
        @Override
        public void connected(Connection connection) {
        }

        @Override
        public void disconnected(Connection connection) {
        }

        @Override
        public void received(Connection connection, Object object) {
            if(object instanceof KryoPackets.Credentials) {
                KryoPackets.Credentials cred = (KryoPackets.Credentials) object;
                BasicDBObject newUser = new BasicDBObject("username", cred.username);

                if(db.getCollection("oh_users").findOne(newUser) == null) {
                    db.getCollection("oh_users").insert(newUser);
                    return;
                } else {
                    connection.sendTCP(new KryoPackets.Credentials.UserExists());
                    return;
                }
            }
        }

        @Override
        public void idle(Connection connection) {
        }
    }
}
