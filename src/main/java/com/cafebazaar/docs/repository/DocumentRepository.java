package com.cafebazaar.docs.repository;

import com.cafebazaar.docs.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
