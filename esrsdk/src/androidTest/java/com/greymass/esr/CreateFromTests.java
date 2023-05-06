package com.greymass.esr;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.greymass.esr.interfaces.IRequest;
import com.greymass.esr.models.Action;
import com.greymass.esr.models.Actions;
import com.greymass.esr.models.Chain;
import com.greymass.esr.models.Identity;
import com.greymass.esr.models.InfoPair;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.RequestFlag;
import com.greymass.esr.models.Transaction;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CreateFromTests extends ESRTest {

    @Test
    public void createFromAction() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        signingRequest.setRequest(makeTransferAction(FOO, ACTIVE, FOO, BAR, "1.000 EOS", "hello there"));

        assertEquals("ChainId should be EOS", signingRequest.getChainId().getChainAlias(), Chain.EOS.getAlias());
        IRequest request = signingRequest.getRequest();
        assertTrue("Should get an Action type of request", (request instanceof Action));
        Action action = (Action) request;
        assertTransferActionPacked(action, FOO, ACTIVE, "000000000000285D000000000000AE39E80300000000000003454F53000000000B68656C6C6F207468657265");

        assertEquals("Callback should be empty", "", signingRequest.getCallback());
        RequestFlag flag = signingRequest.getRequestFlag();
        assertEquals("Flag should be 1", (byte) 1, flag.getFlagValue());
        assertTrue("Flag should be broadcast", flag.isBroadcast());
        assertFalse("Flag should not be background", flag.isBackground());
        List<InfoPair> pairs = signingRequest.getInfoPairs();
        assertTrue("Should be no info pairs", pairs.isEmpty());
    }

    @Test
    public void createFromActions() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        Actions actions = new Actions();
        actions.addAction(makeTransferAction(FOO, ACTIVE, FOO, BAR, "1.000 EOS", "hello there"));
        actions.addAction(makeTransferAction(BAZ, ACTIVE, BAZ, BAR, "1.000 EOS", "hello there"));

        RequestFlag requestFlag = RequestFlag.getDefault();
        requestFlag.setBackground(true);

        signingRequest.setRequestFlag(requestFlag);
        signingRequest.setRequest(actions);
        signingRequest.setCallback(EXAMPLE_CALLBACK);

        assertEquals("ChainId should be EOS", signingRequest.getChainId().getChainAlias(), Chain.EOS.getAlias());
        IRequest request = signingRequest.getRequest();
        assertTrue("Should get an Actions type of request", (request instanceof Actions));
        Actions actionsResult = (Actions) request;
        List<Action> actionList = actionsResult.getActions();

        assertEquals("Should be 2 actions", 2, actionList.size());

        Action action = actionList.get(0);
        assertTransferActionPacked(action, FOO, ACTIVE, "000000000000285D000000000000AE39E80300000000000003454F53000000000B68656C6C6F207468657265");

        action = actionList.get(1);
        assertTransferActionPacked(action, BAZ, ACTIVE, "000000000000BE39000000000000AE39E80300000000000003454F53000000000B68656C6C6F207468657265");

        assertEquals("Callback should be " + EXAMPLE_CALLBACK, EXAMPLE_CALLBACK, signingRequest.getCallback());
        RequestFlag flag = signingRequest.getRequestFlag();
        assertEquals("Flag should be 3", (byte) 3, flag.getFlagValue());
        assertTrue("Flag should be broadcast", flag.isBroadcast());
        assertTrue("Flag should be background", flag.isBackground());
        List<InfoPair> pairs = signingRequest.getInfoPairs();
        assertTrue("Should be no info pairs", pairs.isEmpty());
    }

    @Test
    public void createFromTransaction() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        Transaction transaction = new Transaction();
        transaction.setDelaySec(123);
        transaction.setExpiration(EXPIRATION_TIMESTAMP);
        transaction.setMaxCpuUsageMs(99);
        transaction.setActions(Lists.newArrayList(makePackedTransferAction(FOO, ACTIVE, "000000000000285D000000000000AE39E80300000000000003454F53000000000B68656C6C6F207468657265")));
        RequestFlag requestFlag = RequestFlag.getDefault();
        requestFlag.setBroadcast(false);
        signingRequest.setRequestFlag(requestFlag);
        signingRequest.setCallback(EXAMPLE_CALLBACK);
        signingRequest.setRequest(transaction);

        assertEquals("ChainId should be EOS", signingRequest.getChainId().getChainAlias(), Chain.EOS.getAlias());
        IRequest request = signingRequest.getRequest();
        assertTrue("Should get a Transaction type of request", (request instanceof Transaction));
        Transaction transactionResult = (Transaction) request;
        List<Action> actionList = transaction.getActionsList();

        assertEquals("Should be 1 action", 1, actionList.size());

        Action action = actionList.get(0);
        assertTransferActionPacked(action, FOO, ACTIVE, "000000000000285D000000000000AE39E80300000000000003454F53000000000B68656C6C6F207468657265");

        assertEquals("Callback should be " + EXAMPLE_CALLBACK, EXAMPLE_CALLBACK, signingRequest.getCallback());
        RequestFlag flag = signingRequest.getRequestFlag();
        assertEquals("Flag should be 0", (byte) 0, flag.getFlagValue());
        assertFalse("Flag should not be broadcast", flag.isBroadcast());
        assertFalse("Flag should not be background", flag.isBackground());

        List<InfoPair> pairs = signingRequest.getInfoPairs();
        assertTrue("Should be no info pairs", pairs.isEmpty());

        assertEquals("Should be no context free actions", 0, transactionResult.getContextFreeActionsList().size());
        assertEquals("Should be no transaction extensions", 0, transactionResult.getTransactionExtensions().size());
        assertEquals("delay_sec should be 123", 123, transactionResult.getDelaySec());
        assertEquals("expiration should be " + EXPIRATION_TIMESTAMP, EXPIRATION_TIMESTAMP, transactionResult.getExpiration());
        assertEquals("max_cpu_usage_ms should be 99", 99, transactionResult.getMaxCpuUsageMs());
        assertEquals("max_net_usage_words should be 0 (default)", 0, transactionResult.getMaxNetUsageWords());
        assertTrue("ref_block_num should be 0 (default)", 0L == transactionResult.getRefBlockNum());
        assertTrue("ref_block_prefix should be 0 (default)", 0L == transactionResult.getRefBlockPrefix());
    }

    @Test
    public void createIdentityTransaction() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        signingRequest.getRequestFlag().setBackground(true);
        signingRequest.setCallback("https://example.com");
        Identity identity = new Identity();
        signingRequest.setRequest(identity);
        Map<String, String> abiMap = signingRequest.fetchAbis();
        Transaction transaction = signingRequest.resolveTransaction(abiMap, new PermissionLevel(FOO, BAR));
        assertIdentityTransactionNoContext(transaction, FOO, BAR);

        Transaction transaction2 = signingRequest.resolveTransaction(abiMap, new PermissionLevel(OTHER, ACTIVE));
        assertIdentityTransactionNoContext(transaction2, OTHER, ACTIVE);
    }

    @Test
    public void generateCorrectIdentityRequests() throws ESRException {
        String reqUri = "esr://AgABAwACJWh0dHBzOi8vY2guYW5jaG9yLmxpbmsvMTIzNC00NTY3LTg5MDAA";
        SigningRequest request = makeSigningRequest();
        request.load(reqUri);
        assertTrue("Should be identity request", request.isIdentity());
        assertNull("Should be null identity", request.getIdentity());
        assertNull("Should be null identity permission", request.getIdentityPermission());
        assertEquals("Should encode back to original uri", reqUri, request.encode());

        Transaction transaction = request.resolveTransaction(new HashMap<String, String>(), new PermissionLevel(FOO, BAR));
        assertIdentityTransactionNoContext(transaction, FOO, BAR);
    }

}
