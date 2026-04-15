package com.netflixsub.common;

import java.util.UUID;

public final class Ids {
    private Ids() {}
    public static String shortId() { return UUID.randomUUID().toString().substring(0, 8); }
    public static String token()   { return UUID.randomUUID().toString().replace("-", ""); }
}
