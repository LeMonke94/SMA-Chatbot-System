package io.github.youngmoney.application;

import io.github.youngmoney.domain.User;
import io.github.youngmoney.domain.BotInfo;
import io.github.youngmoney.infrastructure.persistence.PersistenceManager;
import io.github.youngmoney.domain.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ChatSystem {
    private final UserManager userManager;
    private final BotManager botManager;
    private final PersistenceManager persistenceManager;
    private User currentUser;
    private User system = new User("System");

    //Konstruktor
    public ChatSystem(UserManager userManagerInput, BotManager botManagerInput, PersistenceManager persistenceManagerInput) {
        this.userManager = userManagerInput;
        this.botManager = botManagerInput;
        this.persistenceManager = persistenceManagerInput;
        this.currentUser = null;
    }

    //Methoden
    public String loginAttempt(String username, String password) {
        User newUser = userManager.login(username, password);
        if (newUser != null) {
            this.currentUser = newUser;
            StringBuilder welcome = new StringBuilder("\nWillkommen, " + newUser.getUsername() + "!\n");
            welcome.append("------------------ Letzte Nachrichten ------------------\n");
            List<ChatMessage> history = persistenceManager.loadHistory(currentUser);
            if (history.isEmpty()) {
                welcome.append("Keinen Verlauf gefunden.");
            } else {
                history.forEach(message -> welcome.append(message.toString()).append("\n"));
            }
            welcome.append(getHelpMessage());
            return welcome.toString();
        }
        return null;
    }

    public boolean isUserLoggedIn() {
        return this.currentUser != null;
    }

    public String processInput(String messageInput) {
        persistenceManager.saveMessage(new ChatMessage(currentUser.getUsername(), currentUser.getUsername(), messageInput, LocalDateTime.now()));

        String systemResponse;
        if ("exit".equalsIgnoreCase(messageInput)) {
            return "exit";
        }
        if ("list bots".equalsIgnoreCase(messageInput)) {
            List<BotInfo> bots = botManager.listBots();
            StringBuilder sb = new StringBuilder("Verfügbare Bots:\n");
            bots.forEach(bot -> sb.append(" - ").append(bot.toString()).append("\n"));
            systemResponse = sb.toString();
        } else if (messageInput.toLowerCase().startsWith("activate bot ")) {
            String botName = messageInput.substring(13);
            botManager.activateBot(botName);
            systemResponse = "Bot '" + botName + "' aktiviert.\n";
        } else if (messageInput.toLowerCase().startsWith("deactivate bot ")) {
            String botName = messageInput.substring(15);
            botManager.deactivateBot(botName);
            systemResponse = "Bot '" + botName + "' deaktiviert\n";
        } else {
            Optional<String> botResponse = botManager.assignToBot(messageInput);
            systemResponse = botResponse.orElse("Echo: " + messageInput + "\n");
        }
        persistenceManager.saveMessage(new ChatMessage(system.getUsername(), currentUser.getUsername(), systemResponse, LocalDateTime.now()));
        return systemResponse;
    }

    private String getHelpMessage() {
        StringBuilder sb = new StringBuilder("---------------------------------------------------------\n");
        sb.append("Hier sind die verfügbaren Befehle::\n\n");
        sb.append("Systembefehle:\n");
        sb.append("  list bots               - Zeigt alle verfügbaren Bots an.\n");
        sb.append("  activate bot <name>     - Aktiviert einen Bot.\n");
        sb.append("  deactivate bot <name>   - Deaktiviert einen Bot.\n");
        sb.append("  exit                    - Beendet das Programm.\n\n");
        sb.append("Bot-Befehle (Beispiel):\n");
        sb.append("  @wetterbot <stadt>      - Fragt das aktuelle Wetter für eine Stadt ab.\n");
        sb.append("---------------------------------------------------------\n");
        return sb.toString();
    }
    
}

