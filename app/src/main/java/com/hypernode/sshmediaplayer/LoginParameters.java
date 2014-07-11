package com.hypernode.sshmediaplayer;

/**
 * Created by johannes on 08.07.14.
 */
public class LoginParameters {

    String username;
    String hostname;
    String password;
    int port; //5901;

    public LoginParameters(String username, String hostname, String password, int port) {
        this.username = username;
        this.hostname = hostname;
        this.password = password;
        this.port = port;
    }


}
