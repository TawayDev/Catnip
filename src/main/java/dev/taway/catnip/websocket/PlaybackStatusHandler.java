package dev.taway.catnip.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.data.music.MusicQueueEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PlaybackStatusHandler extends TextWebSocketHandler {
    private static final Logger log = LogManager.getLogger(PlaybackStatusHandler.class);
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper;

    public PlaybackStatusHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New WebSocket connection established: {}", session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} - Status: {}", session.getId(), status);
        sessions.remove(session);
    }

    public void broadcastStatus(MusicQueueEntry status) {
        sessions.forEach(session -> {
            try {
                String json = objectMapper.writeValueAsString(status);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("Failed to send status update to session {}", session.getId(), e);
            }
        });
    }
}