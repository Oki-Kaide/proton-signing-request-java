package com.greymass.esr.models;

import com.google.gson.JsonObject;

public class LinkCreate {

    public static final String SESSION_NAME = "session_name";
    public static final String REQUEST_KEY = "request_key";

    private String gSessionName;
    private String gRequestKey;

    public LinkCreate(String sessionName, String requestKey) {
       gSessionName = sessionName;
       gRequestKey = requestKey;
    }

    public LinkCreate(JsonObject jsonObject) {
        gSessionName = jsonObject.get(SESSION_NAME).getAsString();
        gRequestKey = jsonObject.get(REQUEST_KEY).getAsString();
    }

    public String getSessionName() {
        return gSessionName;
    }

    public String getRequestKey() {
        return gRequestKey;
    }
}
