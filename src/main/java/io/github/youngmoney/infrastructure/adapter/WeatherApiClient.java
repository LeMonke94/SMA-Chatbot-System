package io.github.youngmoney.infrastructure.adapter;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApiClient {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=de";
    private final String apiKey = "047ff19bc5a4b610adfaf6896396749c";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    //Methoden
    public String fetchWeatherData(String city) {
        try {
            String url = String.format(API_URL, city, apiKey);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                WeatherResponse weather = gson.fromJson(response.body(), WeatherResponse.class);
                return String.format("Das Wetter in %s: %s, Temperatur beträgt: %.1f°C, Wind: %.1f m/s.",
                        weather.name,
                        weather.weather[0].description,
                        weather.main.temp,
                        weather.wind.speed);
            } else {
                return "Fehler: Stadt nicht gefunden oder ein API-Problem ist aufgetreten.";
            }
        } catch (IOException | InterruptedException e) {
            return "Fehler bei der Verbindung zur API.";
        }
    }

    //JSON Umwandlung
    private static class WeatherResponse { String name; Weather[] weather; Main main; Wind wind; }
    private static class Weather { String description; }
    private static class Main { double temp; }
    private static class Wind { double speed; }
    
}
