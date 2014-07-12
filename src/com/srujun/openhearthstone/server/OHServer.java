package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.IOException;

public class OHServer {
    public DB db;

    public static OHServer instance;
    private Server server;
    private MongoClient mongoClient;

    private OHServer() {
        server = new Server();
        server.addListener(new TestListener());

        registerPacketObjects();

        try {
            server.bind(6391);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.start();
    }

    private void registerPacketObjects() {
        Kryo kryo = server.getKryo();
    }

    public static void main(String args[]) {
       instance = new OHServer();
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
        }

        @Override
        public void idle(Connection connection) {
        }
    }
}
