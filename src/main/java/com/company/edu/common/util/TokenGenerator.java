package com.company.edu.common.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenGenerator {
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
