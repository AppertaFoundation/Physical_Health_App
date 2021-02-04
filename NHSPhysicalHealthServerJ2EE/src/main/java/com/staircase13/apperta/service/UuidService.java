package com.staircase13.apperta.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidService {
    public String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
