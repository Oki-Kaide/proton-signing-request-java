package com.greymass.esr;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.ResolvedCallback;
import com.greymass.esr.models.Transaction;
import com.greymass.esr.models.TransactionContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static com.greymass.esr.SigningRequest.PLACEHOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResolveTests extends ESRTest {

    @Test
    public void resolveToTransaction() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        signingRequest.setRequest(makeTransferAction(FOO, ACTIVE, FOO, BAR, "1.000 EOS", "hello there"));
        Map<String, String> abiMap = signingRequest.fetchAbis();
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setTimestamp(EXPIRATION_TIMESTAMP);
        transactionContext.setBlockNum(1234L);
        transactionContext.setExpireSeconds(0);
        transactionContext.setRefBlockPrefix(56789L);
        Transaction transaction = signingRequest.resolveTransaction(abiMap, new PermissionLevel(FOO, BAR), transactionContext);

        assertEquals("Transaction should have one action", 1, transaction.getActionsList().size());
        assertFalse("Action should not be packed", transaction.getActionsList().get(0).getData().isPacked());
        assertEquals("Transaction should have no context free actions", 0, transaction.getContextFreeActionsList().size());
        assertEquals("Transaction should have no transaction extensions", 0, transaction.getTransactionExtensions().size());
        assertEquals("Expiration should be " + EXPIRATION_TIMESTAMP, EXPIRATION_TIMESTAMP, transaction.getExpiration());
        assertTrue("ref_block_num should be 1234", 1234 == transaction.getRefBlockNum());
        assertTrue("ref_block_prefix should be 56789", 56789 == transaction.getRefBlockPrefix());
        assertTrue("max_cpu_usage_ms should be 0", 0 == transaction.getMaxCpuUsageMs());
        assertTrue("max_net_usage_words be 0", 0 == transaction.getMaxNetUsageWords());
        assertTrue("delay_sec should be 0", 0 == transaction.getDelaySec());

        assertTransferActionUnpacked(transaction.getActionsList().get(0), FOO, ACTIVE, FOO, BAR, "1.000 EOS", "hello there");
    }

    @Test
    public void resolvePlaceholderName() throws ESRException {
        SigningRequest signingRequest = makeSigningRequest();
        signingRequest.setRequest(makeTransferAction(PLACEHOLDER_NAME, SigningRequest.PLACEHOLDER_PERMISSION, PLACEHOLDER_NAME, SigningRequest.PLACEHOLDER_PERMISSION, "1.000 EOS", "hello there"));
        Map<String, String> abiMap = signingRequest.fetchAbis();
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setTimestamp(EXPIRATION_TIMESTAMP);
        transactionContext.setBlockNum(1234L);
        transactionContext.setExpireSeconds(0);
        transactionContext.setRefBlockPrefix(56789L);
        Transaction transaction = signingRequest.resolveTransaction(abiMap, new PermissionLevel(FOO, MRACTIVE), transactionContext);

        assertEquals("Transaction should have one action", 1, transaction.getActionsList().size());
        assertFalse("Action should not be packed", transaction.getActionsList().get(0).getData().isPacked());
        assertEquals("Transaction should have no context free actions", 0, transaction.getContextFreeActionsList().size());
        assertEquals("Transaction should have no transaction extensions", 0, transaction.getTransactionExtensions().size());
        assertEquals("Expiration should be " + EXPIRATION_TIMESTAMP, EXPIRATION_TIMESTAMP, transaction.getExpiration());
        assertTrue("ref_block_num should be 1234", 1234 == transaction.getRefBlockNum());
        assertTrue("ref_block_prefix should be 56789", 56789 == transaction.getRefBlockPrefix());
        assertTrue("max_cpu_usage_ms should be 0", 0 == transaction.getMaxCpuUsageMs());
        assertTrue("max_net_usage_words be 0", 0 == transaction.getMaxNetUsageWords());
        assertTrue("delay_sec should be 0", 0 == transaction.getDelaySec());

        assertTransferActionUnpacked(transaction.getActionsList().get(0), FOO, MRACTIVE, FOO, MRACTIVE, "1.000 EOS", "hello there");
    }

    @Test
    public void shouldResolveTemplatedCallbackUrls() throws ESRException {
        String reqUri = "esr://gmNgZGBY1mTC_MoglIGBIVzX5uxZRqAQGDBBaUWYAARoxMIkGAJDIyAM9YySkoJiK3391IrE3IKcVL3k_Fz7kgrb6uqSitpataQ8ICspr7aWAQA";
        SigningRequest request = makeSigningRequest();
        request.load(reqUri);
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setTimestamp(EXPIRATION_TIMESTAMP);
        transactionContext.setBlockNum(1234L);
        transactionContext.setExpireSeconds(0);
        transactionContext.setRefBlockPrefix(56789L);

        ResolvedSigningRequest resolved = request.resolve(new PermissionLevel(FOO, BAR), transactionContext);
        String signature = "SIG_K1_KBub1qmdiPpWA2XKKEZEG3EfKJBf38GETHzbd4t3CBdWLgdvFRLCqbcUsBbbYga6jmxfdSFfodMdhMYraKLhEzjSCsiuMs";
        String expectedCallback = "https://example.com?tx=6AFF5C203810FF6B40469FE20318856354889FF037F4CF5B89A157514A43E825&bn=1234";
        ResolvedCallback resolvedCallback = resolved.getCallback(Lists.newArrayList(signature), 1234);
        assertEquals("Callback should resolve properly", expectedCallback, resolvedCallback.getUrl());
    }

}
