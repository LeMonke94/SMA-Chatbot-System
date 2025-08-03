package io.github.youngmoney.application;

import io.github.youngmoney.domain.User;
import io.github.youngmoney.domain.BotInfo;
import java.util.List;
import java.util.Optional;

public class ChatSystem {
    private final UserManager userManager;
    private final BotManager botManager;
    private User currentUser;

    //Konstruktor
    public ChatSystem(UserManager userManagerInput, BotManager botManagerInput) {
        this.userManager = userManagerInput;
        this.botManager = botManagerInput;
        this.currentUser = null;
    }

    //Methoden
    public String loginAttempt(String username, String password) {
        User newUser = userManager.login(username, password);
        if (newUser != null) {
            this.currentUser = newUser;
            return "Willkommen, " + newUser.getUsername() + "!\n\n" + getHelpMessage();
        }
        return null;
    }

    public boolean isUserLoggedIn() {
        return this.currentUser != null;
    }

    public String processInput(String messageInput) {
        if ("exit".equalsIgnoreCase(messageInput)) {
            return "exit";
        }
        if ("list bots".equalsIgnoreCase(messageInput)) {
            List<BotInfo> bots = botManager.listBots();
            StringBuilder sb = new StringBuilder("Verfügbare Bots:\n");
            bots.forEach(bot -> sb.append("- ").append(bot.toString()).append("\n"));
            return sb.toString();
        }
        if (messageInput.toLowerCase().startsWith("activate bot ")) {
            String botName = messageInput.substring(13);
            botManager.activateBot(botName);
            return "Bot '" + botName + "' aktiviert.\n";
        }
        if (messageInput.toLowerCase().startsWith("deactivate bot ")) {
            String botName = messageInput.substring(15);
            botManager.deactivateBot(botName);
            return "Bot '" + botName + "' deaktiviert.\n";
        }
        Optional<String> botResponse = botManager.assignToBot(messageInput);
        return botResponse.orElse("Echo für " + currentUser.getUsername() + ": " + messageInput);
    }

    private String getHelpMessage() {
        StringBuilder sb = new StringBuilder("----------------------------------------------------\n");
        sb.append("Hier sind die verfügbaren Befehle::\n\n");
        sb.append("Systembefehle:\n");
        sb.append("  list bots               - Zeigt alle verfügbaren Bots an.\n");
        sb.append("  activate bot <name>     - Aktiviert einen Bot.\n");
        sb.append("  deactivate bot <name>   - Deaktiviert einen Bot.\n");
        sb.append("  exit                    - Beendet das Programm.\n\n");
        sb.append("Bot-Befehle (Beispiel):\n");
        sb.append("  @wetterbot <stadt>      - Fragt das aktuelle Wetter für eine Stadt ab.\n");
        sb.append("----------------------------------------------------\n\n");
        return sb.toString();
    }
    
}
