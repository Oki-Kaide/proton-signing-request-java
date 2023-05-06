package com.greymass.esr;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.Maps;
import com.greymass.esr.models.AccountName;
import com.greymass.esr.models.Action;
import com.greymass.esr.models.ActionData;
import com.greymass.esr.models.ActionName;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.Transaction;

import java.util.List;
import java.util.Map;

import static com.greymass.esr.models.Identity.IDENTITY;
import static com.greymass.esr.models.PermissionLevel.ACTOR;
import static com.greymass.esr.models.PermissionLevel.PERMISSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ESRTest {

    protected static final String FOO = "foo";
    protected static final String BAR = "bar";
    protected static final String BAZ = "baz";
    protected static final String OTHER = "other";
    protected static final String ACTIVE = "active";
    protected static final String MRACTIVE = "mractive";
    protected static final String EOSIO_TOKEN = "eosio.token";
    protected static final String TRANSFER = "transfer";
    protected static final String FROM = "from";
    protected static final String TO = "to";
    protected static final String QUANTITY = "quantity";
    protected static final String MEMO = "memo";
    protected static final String EXPIRATION_TIMESTAMP = "2018-02-15T00:00:00.000";
    protected static final String EXAMPLE_CALLBACK = "https://example.com/?tx={{tx}}";

    protected ResourceReader gReader;

    public ESRTest() {
        gReader = new ResourceReader(getContext());
    }

    protected SigningRequest makeSigningRequest() {
        return new SigningRequest(new ESR(getContext(), new MockAbiProvider(gReader)));
    }

    protected void assertTransferAction(Action action, String actor, String permission) {
        assertEquals("Account should be eosio.token", "eosio.token", action.getAccount().getName());
        assertEquals("Name should be transfer", "transfer", action.getName().getName());
        assertEquals("Should be one permission", 1, action.getAuthorization().size());
        assertEquals("Should be " + actor + " for permission account name", actor, action.getAuthorization().get(0).getAccountName().getName());
        assertEquals("Should be " + permission + " for permission name", permission, action.getAuthorization().get(0).getPermissionName().getName());

    }

    protected void assertTransferActionUnpacked(Action action, String actor, String permission, String from, String to, String quantity, String memo) {
        assertTransferAction(action, actor, permission);
        assertFalse("Action should not be packed", action.getData().isPacked());
        Map<String, Object> data = action.getData().getData();
        assertEquals("Should be from " + from, from, data.get(FROM));
        assertEquals("Should be to " + to, to, data.get(TO));
        assertEquals("Quantity should be " + quantity, quantity, data.get(QUANTITY));
        assertEquals("Memo should be " + memo, memo, data.get(MEMO));
    }

    protected void assertTransferActionPacked(Action action, String actor, String permission, String packedData) {
        assertTransferAction(action, actor, permission);
        assertTrue("Action data should be packed", action.getData().isPacked());
        assertEquals("Should be expected encoded action data", packedData, action.getData().getPackedData());
    }

    protected void assertIdentityTransactionNoContext(Transaction transaction, String actor, String permissionName) {
        List<Action> actions = transaction.getActionsList();
        assertEquals("Transaction should have one action", 1, actions.size());
        assertFalse("Action should not be packed", actions.get(0).getData().isPacked());
        assertEquals("Transaction should have no context free actions", 0, transaction.getContextFreeActionsList().size());
        assertEquals("Transaction should have no transaction extensions", 0, transaction.getTransactionExtensions().size());
        assertEquals("Expiration should be " + Transaction.DEFAULT_EXPIRATION, Transaction.DEFAULT_EXPIRATION, transaction.getExpiration());
        assertTrue("ref_block_num should be 0", 0 == transaction.getRefBlockNum());
        assertTrue("ref_block_prefix should be 0", 0 == transaction.getRefBlockPrefix());
        assertTrue("max_cpu_usage_ms should be 0", 0 == transaction.getMaxCpuUsageMs());
        assertTrue("max_net_usage_words be 0", 0 == transaction.getMaxNetUsageWords());
        assertTrue("delay_sec should be 0", 0 == transaction.getDelaySec());

        Action action = actions.get(0);
        assertEquals("Should have empty account name", "", action.getAccount().getName());
        assertEquals("Should have action name of " + IDENTITY, IDENTITY, action.getName().getName());
        Map<String, Object> data = action.getData().getData();
        assertTrue("Should have one key in action data", data.size() == 1);
        assertTrue("Key should be permission", data.containsKey(PERMISSION));
        Map<String, Object> permission = (Map<String, Object>) data.get(PERMISSION);
        assertTrue("Should have 2 keys in permission", permission.size() == 2);
        assertTrue("Should have an actor key in permission", permission.containsKey(ACTOR));
        assertTrue("Should have a permission key in permission", permission.containsKey(PERMISSION));
        assertEquals("Actor should be " + actor, actor, permission.get(ACTOR));
        assertEquals("Permission should be " + permissionName, permissionName, permission.get(PERMISSION));
        assertTrue("Should be only one authorization", action.getAuthorization().size() == 1);
        PermissionLevel auth = action.getAuthorization().get(0);
        assertEquals("Actor should be " + actor, actor, auth.getAccountName().getName());
        assertEquals("Permission should be " + permissionName, permissionName, auth.getPermissionName().getName());
    }

    protected Action makePackedTransferAction(String permAcct, String permName, String packedData) {
        Action action = new Action();
        action.setAccount(new AccountName(EOSIO_TOKEN));
        action.setName(new ActionName(TRANSFER));
        action.addAuthorization(new PermissionLevel(permAcct, permName));
        action.setData(new ActionData(packedData));
        return action;
    }

    protected Action makeTransferAction(String permAcct, String permName, String from, String to, String quant, String memo) {
        Action action = new Action();
        action.setAccount(new AccountName(EOSIO_TOKEN));
        action.setName(new ActionName(TRANSFER));
        action.addAuthorization(new PermissionLevel(permAcct, permName));
        Map<String, Object> dataMap = Maps.newHashMap();
        dataMap.put(FROM, from);
        dataMap.put(TO, to);
        dataMap.put(QUANTITY, quant);
        dataMap.put(MEMO, memo);
        action.setData(new ActionData(dataMap));
        return action;
    }

    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }
}
