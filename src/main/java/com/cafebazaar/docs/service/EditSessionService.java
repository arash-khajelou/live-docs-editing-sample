package com.cafebazaar.docs.service;

import com.cafebazaar.docs.enumeration.DocumentChangeEventType;
import com.cafebazaar.docs.model.Document;
import com.cafebazaar.docs.model.DocumentChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Service
public class EditSessionService {

    private final ReactiveRedisOperations<String, String> documentOps;
    private final ReactiveRedisOperations<String, DocumentChangeEvent> eventOps;
    private final DocumentService documentService;

    @Autowired
    public EditSessionService(
            ReactiveRedisOperations<String, String> documentOps,
            ReactiveRedisOperations<String, DocumentChangeEvent> eventOps,
            DocumentService documentService
    ) {
        this.documentOps = documentOps;
        this.eventOps = eventOps;
        this.documentService = documentService;
    }

    public Mono<Document> initSession(Long docId) {
        // Check if the redis value for content differs from the persisted content
        Optional<Document> documentOptional = documentService.getDocumentById(docId);
        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            return getDocumentContent(docId).switchIfEmpty(
                    Mono.just(document.getContent())
            ).flatMap(
                    content -> {
                        if (!content.equals(document.getContent())) {
                            document.setContent(content);
                            document.setUnsavedChanges(true);
                        }
                        return Mono.just(document);
                    }
            );
        } else {
            return Mono.error(new RuntimeException("Document not found with id: " + docId));
        }
    }

    public Mono<Void> applyEvent(Long docId, DocumentChangeEvent event) {
        String documentKey = getDocumentRedisKey(docId);
        String eventKey = getDocumentChangeEventRedisKey(docId);

        Mono<String> contentFromRedis = documentOps.opsForValue().get(documentKey);

        // Attempt to get the content from Redis, fallback to DocumentService if not present
        Mono<String> documentContent = contentFromRedis.switchIfEmpty(
                Mono.fromCallable(() -> documentService.getDocumentById(docId))
                        .publishOn(Schedulers.boundedElastic())
                        .flatMap(optionalDoc -> optionalDoc.<Mono<? extends String>>map(
                                        document -> Mono.just(document.getContent())).orElseGet(() ->
                                        Mono.error(new RuntimeException("Document not found with id: " + docId))
                                )
                        )
        );

        Mono<Boolean> updateContent = documentContent.flatMap(content -> {
            if (event.getType().equals(DocumentChangeEventType.INSERT)) {
                content = new StringBuilder(content).insert(event.getFromIndex(), event.getMetadata()).toString();
            } else {
                content = new StringBuilder(content).delete(event.getFromIndex(), event.getToIndex()).toString();
            }
            return documentOps.opsForValue().set(documentKey, content);
        });

        // Add event to list of events for this document
        Mono<Void> saveEvent = eventOps.opsForList().rightPush(eventKey, event).then();

        return Mono.when(updateContent, saveEvent);
    }

    public Flux<DocumentChangeEvent> getDocumentChangeEvents(Long docId) {
        String eventKey = getDocumentChangeEventRedisKey(docId);
        return eventOps.opsForList().range(eventKey, 0, -1);
    }

    public Mono<Void> emptyRedis(Long docId) {
        String documentKey = getDocumentRedisKey(docId);
        String eventKey = getDocumentChangeEventRedisKey(docId);
        return documentOps.opsForValue().delete(documentKey).then(eventOps.opsForList().delete(eventKey)).then();
    }

    public Mono<String> getDocumentContent(Long docId) {
        String documentKey = getDocumentRedisKey(docId);
        return documentOps.opsForValue().get(documentKey);
    }

    private String getDocumentRedisKey(Long docId) {
        return "doc:content:" + docId;
    }

    private String getDocumentChangeEventRedisKey(Long docId) {
        return "doc:events:" + docId;
    }

    public Mono<Void> saveDocumentContent(Long docId) {
        return getDocumentContent(docId).flatMap(content -> {
            Optional<Document> documentOptional = documentService.getDocumentById(docId);
            if (documentOptional.isEmpty()) {
                return Mono.error(new RuntimeException("Document not found with id: " + docId));
            } else {
                Document document = documentOptional.get();
                document.setContent(content);
                document.setUnsavedChanges(false);
                documentService.updateDocument(docId, document);

                return Mono.empty();
            }
        });
    }

    public Mono<Void> endSession(Long docId) {
        return getDocumentContent(docId).flatMap(content -> {
            Optional<Document> documentOptional = documentService.getDocumentById(docId);
            if (documentOptional.isEmpty()) {
                return Mono.error(new RuntimeException("Document not found with id: " + docId));
            } else {
                Document document = documentOptional.get();
                document.setContent(content);
                document.setUnsavedChanges(false);
                documentService.updateDocument(docId, document);

                return emptyRedis(docId);
            }
        });
    }

    public Mono<String> getSession(Long docId) {
        return getDocumentContent(docId).switchIfEmpty(
                Mono.fromCallable(() -> documentService.getDocumentById(docId))
                        .publishOn(Schedulers.boundedElastic())
                        .flatMap(optionalDoc -> optionalDoc.<Mono<? extends String>>map(
                                        document -> Mono.just(document.getContent())).orElseGet(() ->
                                        Mono.error(new RuntimeException("Document not found with id: " + docId))
                                )
                        )
        );
    }
}