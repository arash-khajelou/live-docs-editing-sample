package com.cafebazaar.docs.service;

import com.cafebazaar.docs.model.Document;
import com.cafebazaar.docs.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(
            DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }

    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document updateDocument(Long id, Document documentDetails) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found with id " + id));
        document.setContent(documentDetails.getContent());
        return documentRepository.save(document);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}