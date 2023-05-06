package com.greymass.esr.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;

public class JSONUtil {

    private static final Gson gGson;
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        gGson = builder.create();
    }

    public static String stringify(Object toEncode) {
        return gGson.toJson(toEncode);
    }

    public static Object parse(JsonElement element) {
        if (element.isJsonObject())
            return objectToMap((JsonObject) element);

        if (element.isJsonArray())
            return arrayToList((JsonArray) element);

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString())
                return primitive.getAsString();

            if (primitive.isNumber())
                return primitive.getAsDouble();

            if (primitive.isBoolean())
                return primitive.getAsBoolean();
        }

        return null;
    }

    public static Map<String, Object> objectToMap(JsonObject object) {
        Map<String, Object> result = Maps.newHashMap();
        for (String property : object.keySet()) {
            result.put(property, parse(object.get(property)));
        }
        return result;
    }

    public static List<Object> arrayToList(JsonArray array) {
        List<Object> result = Lists.newArrayList();
        for (JsonElement element : array) {
            result.add(parse(element));
        }
        return result;
    }

}
