package com.greymass.esr.models;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import java.util.Map;

public class Extension {
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private Short gType;
    private String gData;

    public Extension(Short type, String data) {
        gType = type;
        gData = data;
    }

    public Extension(JsonObject obj) {
        gType = obj.get(TYPE).getAsShort();
        gData = obj.get(DATA).getAsString();
    }

    public void setType(Short type) {
        gType = type;
    }

    public Short getType() {
        return gType;
    }

    public void setData(String data) {
        gData = data;
    }

    public String getData() {
        return gData;
    }

    public Map<Short, String> toMap() {
        Map<Short, String> extension = Maps.newHashMap();
        extension.put(gType, gData);
        return extension;
    }
}
