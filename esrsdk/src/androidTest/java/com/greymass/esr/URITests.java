package com.greymass.esr;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.greymass.esr.interfaces.IRequest;
import com.greymass.esr.models.Action;
import com.greymass.esr.models.Chain;
import com.greymass.esr.models.InfoPair;
import com.greymass.esr.models.RequestFlag;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.greymass.esr.SigningRequest.PLACEHOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class URITests extends ESRTest {

    @Test
    public void fromURI() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        String requestUri = "esr://gmNgZGBY1mTC_MoglIGBIVzX5uxZRqAQGMBoExgDAjRi4fwAVz93ICUckpGYl12skJZfpFCSkaqQllmcwczAAAA";
        signingRequest.load(requestUri);

        assertEquals("ChainId should be EOS", signingRequest.getChainId().getChainAlias(), Chain.EOS.getAlias());
        IRequest request = signingRequest.getRequest();
        assertTrue("Should get an Action type of request", (request instanceof Action));
        Action action = (Action) request;
        assertEquals("Account should be eosio.token", "eosio.token", action.getAccount().getName());
        assertEquals("Name should be transfer", "transfer", action.getName().getName());
        assertEquals("Should be one permission", 1, action.getAuthorization().size());
        assertEquals("Should be placeholder name for permission account name", PLACEHOLDER_NAME, action.getAuthorization().get(0).getAccountName().getName());
        assertEquals("Should be placeholder name for permission name", PLACEHOLDER_NAME, action.getAuthorization().get(0).getPermissionName().getName());
        assertTrue("Action data should be packed", action.getData().isPacked());
        assertEquals("Should be expected encoded action data", "0100000000000000000000000000285D01000000000000000050454E47000000135468616E6B7320666F72207468652066697368", action.getData().getPackedData());
        assertEquals("Callback should be empty", "", signingRequest.getCallback());
        RequestFlag flag = signingRequest.getRequestFlag();
        assertEquals("Flag should be 3", (byte) 3, flag.getFlagValue());
        assertTrue("Flag should be broadcast", flag.isBroadcast());
        assertTrue("Flag should be background", flag.isBackground());
        List<InfoPair> pairs = signingRequest.getInfoPairs();
        assertTrue("Should be no info pairs", pairs.isEmpty());
    }

}
