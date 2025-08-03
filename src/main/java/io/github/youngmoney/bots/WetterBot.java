package io.github.youngmoney.bots;

import io.github.youngmoney.infrastructure.adapter.WeatherApiClient;

public class WetterBot implements IBot{
    private final WeatherApiClient weatherAdapter;

    //Konstruktor
    public WetterBot(WeatherApiClient weatherAdapterInput) {
        this.weatherAdapter = weatherAdapterInput;
    }

    //Methoden
    @Override
    public String getBotName() {
        return "Wetterbot";
    }

    @Override
    public String getBotDescription() {
        return "Liefert das aktuelle Wetter für eine Stadt.";
    }

    @Override
    public String processMessage(String input) {
        if (input == null || input.isBlank()) {
            return "Bitte gib einen Input ein.\nBeispielinput: wie ist das Wetter in Bielefeld?\n";
        }
        String[] words = input.split(" ");
        String city = words[words.length - 1].replace("?", "");
        return weatherAdapter.fetchWeatherData(city);
    }

}
