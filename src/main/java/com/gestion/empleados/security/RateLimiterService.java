package com.gestion.empleados.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.login-max:10}")
    private int loginMax;
    @Value("${app.security.rate-limit.scan-max:60}")
    private int scanMax;
    @Value("${app.security.rate-limit.window-seconds:60}")
    private int windowSeconds;

    public boolean allowLogin(String key) {
        return allow("login:" + key, loginMax);
    }

    public boolean allowScan(String key) {
        return allow("scan:" + key, scanMax);
    }

    private boolean allow(String key, int maxInWindow) {
        long now = Instant.now().toEpochMilli();
        long min = now - (windowSeconds * 1000L);
        Deque<Long> q = buckets.computeIfAbsent(key, unused -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < min) {
                q.pollFirst();
            }
            if (q.size() >= maxInWindow) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }
}
