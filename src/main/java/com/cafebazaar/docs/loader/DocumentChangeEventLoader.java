package com.cafebazaar.docs.loader;

import com.cafebazaar.docs.model.DocumentChangeEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Flux;

public class DocumentChangeEventLoader {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, DocumentChangeEvent> documentChangeEventOps;

    public DocumentChangeEventLoader(
            ReactiveRedisConnectionFactory factory,
            ReactiveRedisOperations<String, DocumentChangeEvent> documentChangeEventOps
    ) {
        this.factory = factory;
        this.documentChangeEventOps = documentChangeEventOps;
    }
}
