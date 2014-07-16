package com.srujun.openhearthstone.server.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mongodb.BasicDBObject;
import com.srujun.openhearthstone.server.KryoPackets;
import com.srujun.openhearthstone.server.OHServer;

public class LoginListener extends Listener {

    public LoginListener() {
        super();
    }

    @Override
    public void connected(Connection connection) {
        super.connected(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        super.disconnected(connection);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof KryoPackets.Credentials) {
            KryoPackets.Credentials cred = (KryoPackets.Credentials) object;

            if(cred.isNewUsername) {
                // If new user.
                // TODO: Check for username validity
                BasicDBObject newUserQuery = new BasicDBObject("username", cred.username);

                if(OHServer.getDB().getCollection("users").find(newUserQuery).count() == 0) {
                    // Add new user to database.
                    OHServer.getDB().getCollection("users").insert(newUserQuery);
                    // Tell client that user is logged in.
                    connection.sendTCP(new KryoPackets.Credentials.LoggedIn());
                    return;
                } else {
                    // This username exists! Ask client for new username.
                    connection.sendTCP(new KryoPackets.Credentials.UserExists());
                    return;
                }
            } else {
                // If existing user.
                BasicDBObject query = new BasicDBObject("username", cred.username);
                if(OHServer.getDB().getCollection("users").find(query).count() == 0) {
                    // Username not found in database!
                    connection.sendTCP(new KryoPackets.Credentials.UsernameNotFound());
                    return;
                } else {
                    // Tell client that user is logged in.
                    connection.sendTCP(new KryoPackets.Credentials.LoggedIn());
                    return;
                }
            }
        }
    }

    @Override
    public void idle(Connection connection) {
        super.idle(connection);
    }
}
