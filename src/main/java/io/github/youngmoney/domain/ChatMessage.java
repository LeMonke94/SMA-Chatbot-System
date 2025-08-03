package io.github.youngmoney.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private final String sender;
    private final String recipient;
    private final String content;
    private final LocalDateTime timestamp;

    //Konstruktor
    public ChatMessage(String senderInput, String recipientInput, String contentInput, LocalDateTime timestampInput) {
        this.sender = senderInput;
        this.recipient = recipientInput;
        this.content = contentInput;
        this.timestamp = timestampInput;
    }

    //Methoden
    public String getSender() {
        return this.sender;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public String getMessage() {
        return this.content;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("[%s] %s: %s", formattedTimestamp, sender, content);
    }
    
}
