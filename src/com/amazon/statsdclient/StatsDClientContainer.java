package com.amazon.statsdclient;

import java.util.HashMap;
import java.util.Map;

/**
 * A StatsdClientContainer serves to keep track of
 * all active StatsdClients so they can all
 * be reset upon a network change.
 *
 * Created by kestingj on 7/9/15.
 */
public enum StatsDClientContainer {


    INSTANCE;

    private final Map<StatsDClientKey, StatsDClient> clientMap =
            new HashMap<>();

    public synchronized StatsDClient getClient(final StatsDClientKey key) {
        StatsDClient client = clientMap.get(key);
        if (client == null) {
            if (key.operational) {
                client = new NonBlockingStatsDClient(key.prefix, key.domain, key.port);
            } else {
                client = new NoOpStatsDClient();
            }
            clientMap.put(key, client);
        }
        return client;
    }

    /**
     * Closes all statsdClient objects contained within this StatsDClientContainer.
     */
    public synchronized void closeAll () {
        for (StatsDClientKey key : clientMap.keySet()) {
            if (key.operational) {
                NonBlockingStatsDClient temp = (NonBlockingStatsDClient)clientMap.get(key);
                temp.stop();
            }
        }
        clientMap.clear();
    }

    /**
     * resets all StatsdClients stored in this. Should only be called upon when network status changes.
     */
    /*package*/ synchronized void reset () {
        for (StatsDClientKey key : clientMap.keySet()) {
            if (key.operational) {
                NonBlockingStatsDClient temp = (NonBlockingStatsDClient)clientMap.get(key);
                temp.reset();
            }
        }
    }
}
