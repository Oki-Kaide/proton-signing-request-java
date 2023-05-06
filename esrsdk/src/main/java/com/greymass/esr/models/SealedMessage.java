package com.greymass.esr.models;

import com.google.gson.JsonObject;

public class SealedMessage {
    public static final String FROM = "from";
    public static final String NONCE = "nonce";
    public static final String CIPHERTEXT = "ciphertext";
    public static final String CHECKSUM = "checksum";
    private String gFrom;
    private String gNonce;
    private String gCipherText;
    private int gChecksum;

    public SealedMessage(JsonObject jsonObject) {
        gFrom = jsonObject.get(FROM).getAsString();
        gNonce = jsonObject.get(NONCE).getAsString();
        gCipherText = jsonObject.get(CIPHERTEXT).getAsString();
        gChecksum = jsonObject.get(CHECKSUM).getAsInt();
    }

    public String getCipherText() {
        return gCipherText;
    }

    public int getChecksum() {
        return gChecksum;
    }

    public String getFrom() {
        return gFrom;
    }

    public String getNonce() {
        return gNonce;
    }
}
