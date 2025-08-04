package io.github.youngmoney.bots;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.youngmoney.infrastructure.adapter.WeatherApiClient;

public class WetterBot implements IBot{
    private final WeatherApiClient weatherAdapter;
    private final Pattern cityPattern = Pattern.compile("in (.+)", Pattern.CASE_INSENSITIVE);

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
        return "Liefert das aktuelle Wetter für eine Stadt || Liefert die Wetterprognose für die Woche in einer Stadt.";
    }

    @Override
    public String processMessage(String input) {
        if (input == null || input.isBlank()) {
            return "Bitte gib einen Input ein.\nBeispielinput:\n - Wie ist das Wetter in Bielefeld?\n - Wie wird das Wetter in Bielefeld?\n";
        }

        String city = extractCity(input);
        if (city == null) {
            return "Leider konnte ich die Stadt in deiner Frage nicht finden.\nBitte formuliere wie folgt: '... in STADTNAME?'";
        }

        if (input.toLowerCase().contains("wird") || input.toLowerCase().contains("vorhersage")) {
            return weatherAdapter.fetchForecastWeatherData(city);
        } else {
            return weatherAdapter.fetchCurrentWeatherData(city);
        }
    }

    private String extractCity(String input) {
        Matcher matcher = cityPattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).replace("?", "").trim();
        }
        return null;
    }

}

