package com.fonepay.devportal.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * Generates a standard 26-character time-sortable ULID string.
     */
    public static String nextUlid() {
        return UlidCreator.getUlid().toString();
    }

    /**
     * Generates a monotonic ULID string, guaranteeing strict ordering within the
     * same millisecond.
     */
    public static String nextMonotonicUlid() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
