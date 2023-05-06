package com.greymass.esr.models;

public class ActionName {
    private String gName;

    public ActionName(String name) {
        gName = name;
    }

    public void setName(String name) {
        gName = name;
    }

    public String getName() {
        return gName;
    }

    @Override
    public String toString() {
        return gName;
    }

}
