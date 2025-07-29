package com.company.edu.service.auth;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockUntil = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5; // 10분 내 5회
    private static final long BLOCK_DURATION_MS = 10 * 60 * 1000; // 10분

    public boolean isAllowed(String key) {
        if (blockUntil.getOrDefault(key, 0L) > System.currentTimeMillis()) {
            // 블록된 상태
            return false;
        }

        int count = requestCounts.getOrDefault(key, 0);
        if (count >= MAX_REQUESTS) {
            blockUntil.put(key, System.currentTimeMillis() + BLOCK_DURATION_MS);
            requestCounts.remove(key); // 카운트 초기화
            return false;
        }

        requestCounts.put(key, count + 1);
        return true;
    }
}
