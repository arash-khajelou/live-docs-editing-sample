package com.cafebazaar.docs.controller;

import com.cafebazaar.docs.model.Document;
import com.cafebazaar.docs.service.EditSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/edit-session")
public class EditSessionController {

    private final EditSessionService editSessionService;

    @Autowired
    public EditSessionController(
            EditSessionService editSessionService
    ) {
        this.editSessionService = editSessionService;
    }

    @GetMapping("/init/{docId}")
    public Mono<Document> initSession(@PathVariable Long docId) {
        return editSessionService.initSession(docId);
    }

    @GetMapping("/view/{docId}")
    public Mono<String> getSession(@PathVariable Long docId) {
        return editSessionService.getSession(docId);
    }

    @PostMapping("/save/{docId}")
    public Mono<Void> saveSession(@PathVariable Long docId) {
        return editSessionService.saveDocumentContent(docId);
    }

    @PostMapping("/end/{docId}")
    public Mono<Void> endSession(@PathVariable Long docId) {
        return editSessionService.endSession(docId);
    }

    @GetMapping("/share/{docId}")
    public String shareSession(@PathVariable Long docId) {
        return "ws://localhost:8080/ws-doc-edit/" + docId;
    }
}
