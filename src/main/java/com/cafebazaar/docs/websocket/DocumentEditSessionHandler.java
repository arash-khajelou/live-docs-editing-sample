package com.cafebazaar.docs.websocket;

import com.cafebazaar.docs.model.DocumentChangeEvent;
import com.cafebazaar.docs.service.EditSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DocumentEditSessionHandler extends TextWebSocketHandler {

    private final EditSessionService editSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    public DocumentEditSessionHandler(EditSessionService editSessionService) {
        this.editSessionService = editSessionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String docId = getDocIdFromSession(session);
        sessionMap.computeIfAbsent(docId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        DocumentChangeEvent event = objectMapper.readValue(message.getPayload(), DocumentChangeEvent.class);
        String strDocId = getDocIdFromSession(session);
        // Convert strDocId to long
        long docId = Long.parseLong(strDocId);
        editSessionService.applyEvent(docId, event).subscribe();
        broadcastEvent(strDocId, event);
    }

    private void broadcastEvent(String docId, DocumentChangeEvent event) {
        List<WebSocketSession> sessions = sessionMap.getOrDefault(docId, new CopyOnWriteArrayList<>());
        if (sessions != null) {
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        String eventString = objectMapper.writeValueAsString(event);
                        session.sendMessage(new TextMessage(eventString));
                    } catch (Exception e) {
                        // Close the session
                        try {
                            session.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String docId = getDocIdFromSession(session);
        List<WebSocketSession> sessions = sessionMap.get(docId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(docId);
            }
        }
    }

    private String getDocIdFromSession(WebSocketSession session) {
        // Extract the docId from the URL
        String path = Objects.requireNonNull(session.getUri()).getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}