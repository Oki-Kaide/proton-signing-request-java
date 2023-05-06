package com.greymass.esr.models;

import com.google.gson.JsonArray;
import com.greymass.esr.ESRException;
import com.greymass.esr.interfaces.IRequest;

public class IRequestFactory {

    public static IRequest fromVariant(JsonArray variant) throws ESRException {
        String variantType = variant.get(0).getAsString();
        switch (variantType) {
            case Action.VARIANT_TYPE:
                return new Action(variant.get(1).getAsJsonObject());
            case Actions.VARIANT_TYPE:
                return new Actions(variant.get(1).getAsJsonArray());
            case Transaction.VARIANT_TYPE:
                return new Transaction(variant.get(1).getAsJsonObject());
            case Identity.VARIANT_TYPE:
                return new Identity(new PermissionLevel(variant.get(1).getAsJsonObject()));
            default:
                throw new ESRException("Unknown request variant type: " + variantType);
        }
    }

}
