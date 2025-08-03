package io.github.youngmoney;

import io.github.youngmoney.application.BotManager;
import io.github.youngmoney.application.ChatSystem;
import io.github.youngmoney.application.UserManager;
import io.github.youngmoney.bots.IBot;
import io.github.youngmoney.bots.WetterBot;
import io.github.youngmoney.infrastructure.adapter.WeatherApiClient;
import io.github.youngmoney.ui.Console;
import java.util.List;

public class App {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        BotManager botManager = new BotManager();
        ChatSystem chatSystem = new ChatSystem(userManager, botManager);

        //Bots erstellen, in die <IBot> Liste befüllen, und in botManager laden
        //Wetterbot
        WeatherApiClient weatherAdapter = new WeatherApiClient();
        WetterBot wetterBot = new WetterBot(weatherAdapter);

        List<IBot> allBots = List.of(wetterBot);
        botManager.loadBots(allBots);

        //Console erstellen und aktivieren
        Console systemUi = new Console(chatSystem);
        systemUi.startConsole();
    }
}
