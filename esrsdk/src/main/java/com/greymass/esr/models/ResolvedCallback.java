package com.greymass.esr.models;

import java.util.Map;

public class ResolvedCallback {

    public static final String SIG = "SIG";
    public static final String TX = "tx";
    public static final String RBN = "rbn";
    public static final String RID = "rid";
    public static final String EX = "ex";
    public static final String REQ = "req";
    public static final String SA = "sa";
    public static final String SP = "sp";
    public static final String BN = "bn";

    private String gUrl;
    private boolean gBackground;
    private Map<String, String> gPayload;

    public ResolvedCallback(String url, boolean background, Map<String, String> payload) {
        gUrl = url;
        gBackground = background;
        gPayload = payload;
    }

    public String getUrl() {
        return gUrl;
    }

    public boolean isBackground() {
        return gBackground;
    }

    public Map<String, String> getPayload() {
        return gPayload;
    }
}
