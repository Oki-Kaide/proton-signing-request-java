package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greymass.esr.ESRException;

import java.util.List;
import java.util.Map;

import kotlin.text.Charsets;

public class InfoPair {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    private String gKey;
    private String gHexValue;

    public InfoPair(String key, String value) {
        gKey = key;
        gHexValue = value;
    }

    public String getKey() {
        return gKey;
    }

    public String getHexValue() {
        return gHexValue;
    }

    public String getStringValue() {
        return new String(getBytesValue(), Charsets.UTF_8);
    }

    public byte[] getBytesValue() {
        return BaseEncoding.base16().decode(gHexValue);
    }

    public static InfoPair fromDeserializedJsonObject(JsonObject obj) throws ESRException {
        String key = obj.get(KEY).getAsString();
        JsonElement value = obj.get(VALUE);
        if (value.getAsJsonPrimitive().isString())
            return new InfoPair(key, value.getAsString());

        throw new ESRException("InfoPair value should always be a hex string when deserializing");
    }

    public static List<InfoPair> listFromDeserializedJsonArray(JsonArray pairs) throws ESRException {
        List<InfoPair> infoPairs = Lists.newArrayList();

        for (JsonElement el : pairs) {
            if (!(el instanceof JsonObject))
                throw new ESRException("Info pairs must be objects");

            infoPairs.add(InfoPair.fromDeserializedJsonObject((JsonObject) el));
        }

        return infoPairs;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> pair = Maps.newLinkedHashMap();
        pair.put(KEY, gKey);
        pair.put(VALUE, gHexValue);
        return pair;
    }

    public void setHexValue(String hexValue) {
        gHexValue = hexValue;
    }
}
