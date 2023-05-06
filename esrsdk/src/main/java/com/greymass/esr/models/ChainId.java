package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.greymass.esr.ESRException;

import java.util.List;

public class ChainId {

    public static final String CHAIN_ID = "chain_id";

    private static final String ALIAS_LABEL = "chain_alias";
    private String gChainId;
    private int gChainAlias;
    private String gChainName;

    public ChainId(Chain chain) {
        gChainId = chain.getId();
        gChainAlias = chain.getAlias();
        gChainName = chain.name().toLowerCase();
    }

    public ChainId(String chainId, int chainAlias, String chainName) {
        gChainId = chainId;
        gChainAlias = chainAlias;
        gChainName = chainName;
    }

    public String getChainId() {
        return gChainId;
    }

    public int getChainAlias() {
        return gChainAlias;
    }

    public String getChainName() {
        return gChainName;
    }

    public static ChainId fromVariant(JsonArray variant) throws ESRException {
        if (ALIAS_LABEL.equals(variant.get(0).getAsString())) {
            int alias = variant.get(1).getAsInt();
            Chain chain = Chain.fromChainAlias(alias);
            if (chain == Chain.UNKNOWN)
                throw new ESRException("Cannont create ChainId from variant, chain alias unknown");

            return new ChainId(chain);
        } else {
            String id = variant.get(1).getAsString();
            Chain chain = Chain.fromChainId(id);
            if (chain == Chain.UNKNOWN)
                return new ChainId(id, Chain.UNKNOWN.getAlias(), Chain.UNKNOWN.name());

            return new ChainId(chain);
        }
    }

    public List<Object> toVariant() {
        if (gChainAlias != Chain.UNKNOWN.getAlias()) {
            List<Object> variant = Lists.newArrayList();
            variant.add(ALIAS_LABEL);
            variant.add(gChainAlias);
            return variant;
        } else {
            List<Object> variant = Lists.newArrayList();
            variant.add(CHAIN_ID);
            variant.add(gChainId);
            return variant;
        }
    }
}
