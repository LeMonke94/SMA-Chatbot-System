package io.github.youngmoney.application;

import io.github.youngmoney.bots.IBot;
import io.github.youngmoney.domain.BotInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BotManager {
    private final List<IBot> availabeBots = new ArrayList<>();
    private final List<IBot> activeBots = new ArrayList<>();

    //Methoden
    public void loadBots(List<IBot> botsInput) {
        availabeBots.addAll(botsInput);
    }

    public boolean activateBot(String nameInput) {
        Optional<IBot> botOptional = findAviableBot(nameInput);
        if (botOptional.isEmpty()) {
            return false;
        }
        IBot bot = botOptional.get();
        if (!activeBots.contains(bot)) {
            activeBots.add(bot);
        }
        return true;
    }

    public boolean deactivateBot(String nameInput) {
        Optional<IBot> botOptional = findAviableBot(nameInput);
        if (botOptional.isEmpty()) {
            return false;
        }
        activeBots.remove(botOptional.get());
        return true;
    }

    public List<BotInfo> listBots() {
        List<BotInfo> botInfos = new ArrayList<>();
        for (IBot bot : availabeBots) {
            boolean isActive = activeBots.contains(bot);
            botInfos.add(new BotInfo(bot.getBotName(), bot.getBotDescription(), isActive ));
        }
        return botInfos;
    }

    public Optional<String> assignToBot(String input) {
        if (!input.startsWith("@")) {
            return Optional.empty();
        }
        String[] parts = input.split(" ", 2);
        String botNameInput = parts[0].substring(1);

        for (IBot bot: activeBots) {
            if (bot.getBotName().equalsIgnoreCase(botNameInput)) {
                String command = (parts.length > 1) ? parts[1] : "";
                return Optional.of(bot.processMessage(command));
            }
        }
        return Optional.of("Bot '" + botNameInput + "' ist noch nicht aktiviert, oder konnte nicht gefunden werden.\n");
    }

    private Optional<IBot> findAviableBot(String nameInput) {
        return availabeBots.stream().filter(i -> i.getBotName().equalsIgnoreCase(nameInput)).findFirst();
    }

}
