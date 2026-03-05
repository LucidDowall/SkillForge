package com.luciddowall.skillforge.engine;

import java.util.ArrayDeque;

/** Very small object pool to reduce allocation churn. */
public final class ContextPool {
    private final ArrayDeque<CastContext> pool = new ArrayDeque<>(64);

    public CastContext borrow() {
        CastContext ctx = pool.pollFirst();
        return (ctx == null) ? new CastContext() : ctx;
    }

    public void release(CastContext ctx) {
        // keep pool bounded
        if (pool.size() < 64) pool.addFirst(ctx);
    }
}
