package com.greymass.esr.models;

public enum Chain {
    UNKNOWN(0, null),
    EOS(1, "aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906"),
    TELOS(2, "4667b205c6838ef70ff7988f6e8257e8be0e1284a2f59699054a018f743b1d11"),
    JUNGLE(3, "e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473"),
    KYLIN(4, "5fff1dae8dc8e2fc4d5b23b2c7665c97f9e9d8edf2b6485a86ba311c25639191"),
    WORBLI(5, "73647cde120091e0a4b85bced2f3cfdb3041e266cbbe95cee59b73235a1b3b6f"),
    BOS(6, "d5a3d18fbb3c084e3b1f3fa98c21014b5f3db536cc15d08f9f6479517c6a3d86"),
    MEETONE(7, "cfe6486a83bad4962f232d48003b1824ab5665c36778141034d75e57b956e422"),
    INSIGHTS(8, "b042025541e25a472bffde2d62edd457b7e70cee943412b1ea0f044f88591664"),
    BEOS(9, "b912d19a6abd2b1b05611ae5be473355d64d95aeff0c09bedc8c166cd6468fe4"),
    WAX(10, "1064487b3cd1a897ce03ae5b6a865651747e2e152090f99c1d19d44e01aea5a4"),
    PROTON(11, "384da888112027f0321850a169f737c33e53b388aad48b5adace4bab97f437e0"),
    FIO(12, "21dcae42c0182200e93f954a074011f9048a7624c6fe81d3c9541a614a88bd1c");


    private String gChainId;
    private int gChainAlias;

    Chain(int alias, String chainId) {
        gChainAlias = alias;
        gChainId = chainId;
    }

    public static Chain fromChainId(String chainId) {
        if (chainId == null)
            return Chain.UNKNOWN;

        chainId = chainId.toLowerCase();

        for (Chain id : Chain.values())
            if (chainId.equals(id.getId()))
                return id;

         return Chain.UNKNOWN;
    }

    public static Chain fromChainAlias(int alias) {
        for (Chain id : Chain.values())
            if (alias == id.getAlias())
                return id;

        return Chain.UNKNOWN;
    }

    public int getAlias() {
        return gChainAlias;
    }

    public String getId() {
        return gChainId;
    }

    public ChainId toChainId() {
        return new ChainId(this);
    }

}
