package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greymass.esr.ESRException;
import com.greymass.esr.interfaces.IRequest;
import com.greymass.esr.util.JSONUtil;

import java.util.List;
import java.util.Map;

public class Transaction extends TransactionHeader implements IRequest {
    public static final String VARIANT_TYPE = "transaction";
    public static final String EXPIRATION = "expiration";
    public static final String REF_BLOCK_NUM = "ref_block_num";
    public static final String MAX_NET_USAGE_WORDS = "max_net_usage_words";
    public static final String REF_BLOCK_PREFIX = "ref_block_prefix";
    public static final String MAX_CPU_USAGE_MS = "max_cpu_usage_ms";
    public static final String DELAY_SEC = "delay_sec";
    private static final String CONTEXT_FREE_ACTIONS = "context_free_actions";
    private static final String ACTIONS = "actions";
    private static final String TRANSACTION_EXTENSIONS = "transaction_extensions";

    private List<Action> gContextFreeActions = Lists.newArrayList();
    private List<Action> gActions = Lists.newArrayList();
    private List<Extension> gTransactionExtensions = Lists.newArrayList();

    public Transaction() {
        super();
    }

    public Transaction(String expiration, Long refBlockNum,
                       Long refBlockPrefix, int maxNetUsageWords,
                       int maxCpuUsageMs, int delaySec,
                       List<Action> contextFreeActions, List<Action> actions,
                       List<Extension> transactionExtensions) {
        super(expiration, refBlockNum, refBlockPrefix, maxNetUsageWords, maxCpuUsageMs, delaySec);
        gContextFreeActions = contextFreeActions;
        gActions = actions;
        gTransactionExtensions = transactionExtensions;
    }

    public Transaction(JsonObject obj) throws ESRException {
        super(obj.get(EXPIRATION).getAsString(), obj.get(REF_BLOCK_NUM).getAsLong(),
                    obj.get(REF_BLOCK_PREFIX).getAsLong(), obj.get(MAX_NET_USAGE_WORDS).getAsInt(),
                    obj.get(MAX_CPU_USAGE_MS).getAsInt(), obj.get(DELAY_SEC).getAsInt());

        gContextFreeActions = new Actions(obj.getAsJsonArray(CONTEXT_FREE_ACTIONS)).getActions();
        gActions = new Actions(obj.getAsJsonArray(ACTIONS)).getActions();
        gTransactionExtensions = makeExtensionList(obj.getAsJsonArray(TRANSACTION_EXTENSIONS));
    }

    public Transaction shallowClone() {
        Transaction cloned = new Transaction();
        cloned.setExpiration(getExpiration());
        cloned.setRefBlockNum(getRefBlockNum());
        cloned.setRefBlockPrefix(getRefBlockPrefix());
        cloned.setMaxCpuUsageMs(getMaxCpuUsageMs());
        cloned.setMaxNetUsageWords(getMaxNetUsageWords());
        cloned.setContextFreeActions(getContextFreeActionsList());
        cloned.setActions(getActionsList());
        cloned.setTransactionExtensions(getTransactionExtensions());
        return cloned;
    }

    public List<Action> getContextFreeActionsList() {
        return gContextFreeActions;
    }

    public void setContextFreeActions(List<Action> contextFreeActions) {
        gContextFreeActions = contextFreeActions;
    }

    public List<Action> getActionsList() {
        return gActions;
    }

    public void setActions(List<Action> actions) {
        gActions = actions;
    }

    public List<Extension> getTransactionExtensions() {
        return gTransactionExtensions;
    }

    public void setTransactionExtensions(List<Extension> transactionExtensions) {
        gTransactionExtensions = transactionExtensions;
    }

    private static List<Extension> makeExtensionList(JsonArray array) throws ESRException {
        List<Extension> extensions = Lists.newArrayList();
        for (JsonElement el : array) {
            if (!(el instanceof JsonObject))
                throw new ESRException("Extensions should be an object");

            JsonObject obj = (JsonObject) el;
            extensions.add(new Extension(obj));
        }

        return extensions;
    }

    @Override
    public List<Action> getRawActions() {
        return getActionsList();
    }

    @Override
    public List<Object> toVariant() {
        List<Object> variant = Lists.newArrayList();
        variant.add(VARIANT_TYPE);
        variant.add(toMap());
        return variant;
    }

    public String toJSON() {
        return JSONUtil.stringify(toMap());
    }

    @Override
    public String toString() {
        return toJSON();
    }

    private Map<String, Object> toMap() {
        Map<String, Object> transactionMap = Maps.newHashMap();
        transactionMap.put(EXPIRATION, getExpiration());
        transactionMap.put(REF_BLOCK_NUM, getRefBlockNum());
        transactionMap.put(REF_BLOCK_PREFIX, getRefBlockPrefix());
        transactionMap.put(MAX_NET_USAGE_WORDS, getMaxNetUsageWords());
        transactionMap.put(MAX_CPU_USAGE_MS, getMaxCpuUsageMs());
        transactionMap.put(DELAY_SEC, getDelaySec());

        List<Map<String, Object>> contextFreeActionMaps = Lists.newArrayList();
        for (Action action : gContextFreeActions)
            contextFreeActionMaps.add(action.toMap());

        transactionMap.put(CONTEXT_FREE_ACTIONS, contextFreeActionMaps);

        List<Map<String, Object>> actionMaps = Lists.newArrayList();
        for (Action action : gActions)
            actionMaps.add(action.toMap());

        transactionMap.put(ACTIONS, actionMaps);

        List<Map<Short, String>> transactionExtensionMaps = Lists.newArrayList();
        for (Extension extension : gTransactionExtensions)
            transactionExtensionMaps.add(extension.toMap());

        transactionMap.put(TRANSACTION_EXTENSIONS, transactionExtensionMaps);

        return transactionMap;
    }

    public boolean hasTapos() {
        return !(DEFAULT_EXPIRATION.equals(getExpiration()) &&
                getRefBlockNum() == 0 &&
                getRefBlockPrefix() == 0);
    }
}
