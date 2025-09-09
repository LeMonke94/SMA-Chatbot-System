package io.github.youngmoney;

import java.util.List;

import io.github.youngmoney.application.BotManager;
import io.github.youngmoney.application.ChatSystem;
import io.github.youngmoney.application.UserManager;
import io.github.youngmoney.bots.IBot;
import io.github.youngmoney.bots.TranslatorBot;
import io.github.youngmoney.bots.WetterBot;
import io.github.youngmoney.bots.WikiBot;
import io.github.youngmoney.infrastructure.adapter.TranslatorApiClient;
import io.github.youngmoney.infrastructure.adapter.WeatherApiClient;
import io.github.youngmoney.infrastructure.adapter.WikipediaApiClient;
import io.github.youngmoney.infrastructure.persistence.PersistenceManager;
import io.github.youngmoney.ui.Console;

public class App {
    public static void main(String[] args) {
        PersistenceManager persistenceManager = new PersistenceManager();
        persistenceManager.initDB();
        UserManager userManager = new UserManager();
        BotManager botManager = new BotManager();
        ChatSystem chatSystem = new ChatSystem(userManager, botManager, persistenceManager);

        //Bots erstellen, in die <IBot> Liste befüllen, und in botManager laden
        //Wetterbot
        WeatherApiClient weatherAdapter = new WeatherApiClient();
        WetterBot wetterBot = new WetterBot(weatherAdapter);

        WikipediaApiClient wikipediaAdapter = new WikipediaApiClient();
        WikiBot wikiBot = new WikiBot(wikipediaAdapter);

        TranslatorApiClient translatorAdapter = new TranslatorApiClient();
        TranslatorBot translatorBot = new TranslatorBot(translatorAdapter);

        List<IBot> allBots = List.of(wetterBot, wikiBot, translatorBot);
        botManager.loadBots(allBots);

        //Console erstellen und aktivieren
        Console systemUi = new Console(chatSystem);
        systemUi.startConsole();
    }
}
