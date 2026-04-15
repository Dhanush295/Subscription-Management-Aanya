package com.netflixsub.notification;

import com.netflixsub.common.Ids;
import com.netflixsub.common.Json;
import java.time.Instant;

public class Notification {
    public final String id;
    public final String userId;
    public final String type;
    public final String message;
    public Instant sentAt = Instant.now();

    public Notification(String userId, String type, String message) {
        this.id = Ids.shortId();
        this.userId = userId;
        this.type = type;
        this.message = message;
    }

    public Notification(String id, String userId, String type, String message, Instant sentAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        if (sentAt != null) this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "{\"id\":\"" + id + "\","
             + "\"userId\":\"" + userId + "\","
             + "\"type\":\"" + type + "\","
             + "\"message\":\"" + Json.esc(message) + "\","
             + "\"sentAt\":\"" + sentAt + "\"}";
    }
}
