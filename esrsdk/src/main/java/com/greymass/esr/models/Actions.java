package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greymass.esr.ESRException;
import com.greymass.esr.interfaces.IRequest;

import java.util.List;
import java.util.Map;

public class Actions implements IRequest {
    public static final String VARIANT_TYPE = "action[]";
    private List<Action> gActions = Lists.newArrayList();

    public Actions(JsonArray actions) throws ESRException {
        for (JsonElement el : actions) {
            if (!(el instanceof JsonObject))
                throw new ESRException(VARIANT_TYPE + " should be an array of objects");

            gActions.add(new Action((JsonObject) el));
        }
    }

    public Actions(List<Action> actions) {
        gActions = actions;
    }

    public Actions() {

    }

    public void addAction(Action action) {
        gActions.add(action);
    }

    public List<Action> getActions() {
        return gActions;
    }

    @Override
    public List<Action> getRawActions() {
        return getActions();
    }

    @Override
    public List<Object> toVariant() {
        List<Object> variant = Lists.newArrayList();
        variant.add(VARIANT_TYPE);
        List<Map<String, Object>> actionMaps = Lists.newArrayList();
        for (Action action : getActions())
            actionMaps.add(action.toMap());

        variant.add(actionMaps);
        return variant;
    }
}
