package com.srujun.openhearthstone.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(Integer.parseInt(System.getenv("PORT")));
        System.out.println("Listening on: " + System.getenv("PORT"));
        try {
            while (true) {
                Socket socket = listener.accept();
                System.out.println("Connection from: " + listener.getInetAddress());
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("HELLO!");
                } finally {
                    socket.close();
                }
            }
        }
        finally {
            listener.close();
        }
    }
}
