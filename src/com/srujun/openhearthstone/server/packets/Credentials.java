package com.srujun.openhearthstone.server.packets;

public class Credentials {
    static public class InvalidUsername {
    }

    static public class UserExists {
    }

    static public class UsernameNotFound {
    }

    static public class LoggedIn {
    }

    public boolean isNewUsername;
    public String username;
}
