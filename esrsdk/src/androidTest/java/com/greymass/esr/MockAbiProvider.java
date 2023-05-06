package com.greymass.esr;

import com.greymass.esr.interfaces.IAbiProvider;

public class MockAbiProvider implements IAbiProvider {

    private ResourceReader gReader;

    public MockAbiProvider(ResourceReader reader) {
        gReader = reader;
    }

    @Override
    public String getAbi(String account) throws ESRException {
        if ("eosio.token".equals(account))
            return gReader.readResourceString(R.raw.tokenabi);

        throw new ESRException("Tests should only request eosio.token");
    }

}
