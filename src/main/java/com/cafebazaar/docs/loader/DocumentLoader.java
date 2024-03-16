package com.cafebazaar.docs.loader;

import com.cafebazaar.docs.model.Document;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

public class DocumentLoader {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Document> documentOps;

    public DocumentLoader(
            ReactiveRedisConnectionFactory factory,
            ReactiveRedisOperations<String, Document> documentOps
    ) {
        this.factory = factory;
        this.documentOps = documentOps;
    }
}
