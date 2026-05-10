package com.vivid.sdk;

import com.vivid.clients.api.Heartbeat;

/**
 * An API to send heartbeat messages to a Vivid backend.
 * <p>
 * A heartbeat is used by Vivid to keep track of the status of the client.
 * It can be sent periodically to ensure that the client is still active.
 * <p>
 * There are no consequences if heartbeats are not sent.
 * But with heartbeats, Vivid can display the client status in the UI, even if the client does not use REST.
 */
public interface HeartbeatApi {

    /**
     * Send a default heartbeat to the Vivid backend.
     * <p>
     * This default heartbeat must be constructed by this HeartbeatApi implementation.
     * The values of the heartbeat are then taken from the Vivid properties or similar sources.
     * <p>
     * This method is typically used by the Vivid client to send heartbeats so that business logic does not need to know about the heartbeat implementation.
     */
    void sendHeartbeat();

    /**
     * Send a specific heartbeat to the Vivid backend.
     *
     * @param heartbeat the heartbeat to send
     */
    void sendHeartbeat(Heartbeat heartbeat);

}
