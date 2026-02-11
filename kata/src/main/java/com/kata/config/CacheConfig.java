package com.kata.config;

import com.kata.models.SeatHold;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CacheConfig {

    @Bean
    public Map<Long, SeatHold> holdCache() {
        return new ConcurrentHashMap<>();
    }
}
