package com.greymass.esr.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.greymass.esr.ESRException;
import com.greymass.esr.util.JSONUtil;

import java.util.Map;

public class ActionData {
    private Map<String, Object> gData;
    private String gPackedData;

    public ActionData(String data) {
        gPackedData = data;
    }

    public ActionData(Map<String, Object> data) {
        gData = data;
    }

    public ActionData(JsonObject data) {
        gData = JSONUtil.objectToMap(data);
    }

    public boolean isPacked() {
        return gPackedData != null;
    }

    public void setData(Map<String, Object> data) {
        gData = data;
        gPackedData = null;
    }

    public void setData(String packedData) {
        gPackedData = packedData;
        gData = null;
    }

    public Map<String, Object> getData() {
        return gData;
    }

    public String getPackedData() {
        return gPackedData;
    }

    public String toJSON() throws ESRException {
        if (isPacked())
            throw new ESRException("Cannot toJSON packed action data");

        return JSONUtil.stringify(gData);
    }

    @Override
    public String toString() {
        try {
            return isPacked() ? getPackedData() : toJSON();
        } catch (ESRException e) {
            return "Failed to toJSON - " + e.getMessage();
        }
    }
}
