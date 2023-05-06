package com.greymass.esr.models;

public class Callback {

    private String gUrl;
    private boolean gBackground;

    public Callback(String url) {
        gUrl = url;
    }

    public Callback(String url, boolean background) {
        gUrl = url;
        gBackground = background;
    }

}
