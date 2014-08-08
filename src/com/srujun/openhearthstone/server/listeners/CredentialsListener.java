package com.srujun.openhearthstone.server.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mongodb.BasicDBObject;
import com.srujun.openhearthstone.server.OHServer;
import com.srujun.openhearthstone.server.packets.CredentialsPacket;

public class CredentialsListener extends Listener {

    public CredentialsListener() {
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof CredentialsPacket) {
            CredentialsPacket cred = (CredentialsPacket) object;

            if(cred.isNewUsername) {
                // If new user.
                // TODO: Check for username validity

                // Find new user in database to catch duplicates.
                BasicDBObject newUserQuery = new BasicDBObject("username", cred.username);
                if(OHServer.getCollection(OHServer.Collections.USERS).find(newUserQuery).count() == 0) {
                    // Add new user to database.
                    OHServer.getCollection(OHServer.Collections.USERS).insert(newUserQuery);
                    // Tell client that user is logged in.
                    //connection.username = cred.username;
                    connection.sendTCP(new CredentialsPacket.LoginResponse(CredentialsPacket.LoginResponse.Response.SUCCESSFUL));
                    return;
                } else {
                    // This username exists! Ask client for new username.
                    connection.sendTCP(new CredentialsPacket.LoginResponse(CredentialsPacket.LoginResponse.Response.ALREADY_EXISTS));
                    return;
                }
            } else {
                // If existing user.
                BasicDBObject query = new BasicDBObject("username", cred.username);
                if(OHServer.getCollection(OHServer.Collections.USERS).find(query).count() == 0) {
                    // Username not found in database!
                    connection.sendTCP(new CredentialsPacket.LoginResponse(CredentialsPacket.LoginResponse.Response.NOT_FOUND));
                    return;
                } else {
                    // Tell client that user is logged in.
                    connection.setName(cred.username);
                    Log.info("OHServer", connection.toString() + " logged in.");
                    connection.sendTCP(new CredentialsPacket.LoginResponse(CredentialsPacket.LoginResponse.Response.SUCCESSFUL));
                    return;
                }
            }
        }
    }
}
