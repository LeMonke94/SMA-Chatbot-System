package io.github.youngmoney.infrastructure.persistence;

import io.github.youngmoney.domain.ChatMessage;
import io.github.youngmoney.domain.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PersistenceManager {
    private static final String DB_URL ="jdbc:sqlite:chatbot_history.db";
    
    //Methoden
    public void initDB() {
        String sql = """
                CREATE TABLE IF NOT EXISTS chat_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT NOT NULL,
                recipient TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp TEXT NOT NULL
                );""";
        try (Connection c = DriverManager.getConnection(DB_URL); 
        Statement statement = c.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error: Fehler bei der Initialisierung der Datenbank: " + e.getMessage() + "\n");
        }
    }

    public void saveMessage(ChatMessage messageInput) {
        String sql = "INSERT INTO chat_history(sender, recipient, content, timestamp) VALUES(?,?,?,?)";
        try (Connection c = DriverManager.getConnection(DB_URL);
        PreparedStatement preparedStatement = c.prepareStatement(sql)) {
            preparedStatement.setString(1, messageInput.getSender());
            preparedStatement.setString(2, messageInput.getRecipient());
            preparedStatement.setString(3, messageInput.getMessage());
            preparedStatement.setString(4, messageInput.getTimestamp().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: Fehler beim Speichern der Nachricht: " + e.getMessage());
        }
    }

    public List<ChatMessage> loadHistory(User user) {
        String sql = """
                SELECT sender, recipient, content, timestamp FROM chat_history
                WHERE recipient = ?
                ORDER BY timestamp DESC
                LIMIT 100
                """;
        List<ChatMessage> history = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ChatMessage message = new ChatMessage(
                    rs.getString("sender"),
                    rs.getString("recipient"),
                    rs.getString("content"),
                    LocalDateTime.parse(rs.getString("timestamp")));
                history.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error: Fehler beim Laden des Verlaufs: " + e.getMessage() + "\n");
        }
        Collections.reverse(history);
        return history;
    }

}
