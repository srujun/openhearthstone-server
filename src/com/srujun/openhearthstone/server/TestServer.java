package com.srujun.openhearthstone.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class TestServer {
    private Server server;

    TestServer() {
        server = new Server();
        server.addListener(new TestListener());

        registerObjects();

        try {
            server.bind(6391);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.start();
        System.out.println("Server started!");
    }

    private void registerObjects() {
        Kryo kryo = server.getKryo();
    }

    public static void main(String args[]) {
        new TestServer();
    }

    static class TestListener extends Listener {
        @Override
        public void connected(Connection connection) {
            System.out.println("Connection: " + connection.getRemoteAddressTCP());
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
