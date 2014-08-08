package com.srujun.openhearthstone.server.packets;

public class CredentialsPacket {

    static public class LoginResponse {
        public enum Response {
            INVALID, ALREADY_EXISTS, NOT_FOUND, SUCCESSFUL
        }

        public LoginResponse() {}
        public LoginResponse(Response message) {
            this.message = message;
        }

        public Response message;
    }

    public boolean isNewUsername;
    public String username;
}
