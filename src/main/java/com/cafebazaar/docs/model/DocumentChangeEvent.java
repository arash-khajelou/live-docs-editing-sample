package com.cafebazaar.docs.model;

import com.cafebazaar.docs.enumeration.DocumentChangeEventType;

public class DocumentChangeEvent {
    private Long docId;
    private DocumentChangeEventType type;
    private int fromIndex;
    private int toIndex;
    private String metadata;

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public DocumentChangeEventType getType() {
        return type;
    }

    public void setType(DocumentChangeEventType type) {
        this.type = type;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
