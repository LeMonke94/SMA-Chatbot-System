package io.github.youngmoney.infrastructure.adapter;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class WeatherApiClient {
    private static final String CURRENT_STATUS_API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=de";
    private static final String FORECAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=de";
    private final String apiKey = "047ff19bc5a4b610adfaf6896396749c";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    //Methoden
    public String fetchCurrentWeatherData(String cityInput) {
        try {
            String city = URLEncoder.encode(cityInput, StandardCharsets.UTF_8);
            String url = String.format(CURRENT_STATUS_API_URL, city, apiKey);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                WeatherResponse weather = gson.fromJson(response.body(), WeatherResponse.class);
                return String.format("In %s ist es aktuell:\n  - Temperatur: %.1f°C\n  - Wind: %.1f m/s\n  - %s.\n",
                        weather.name,
                        weather.main.temp,
                        weather.wind.speed,
                        weather.weather[0].description);
            } else {
                return "Error: Stadt nicht gefunden oder ein API-Problem ist aufgetreten.\n";
            }
        } catch (IOException | InterruptedException e) {
            return "Error: Fehler bei der Verbindung zur API.\n";
        }
    }

    public String fetchForecastWeatherData(String cityInput) {
        try {
            String city = URLEncoder.encode(cityInput, StandardCharsets.UTF_8);
            String url = String.format(FORECAST_API_URL, city, apiKey);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ForecastResponse forecast = gson.fromJson(response.body(), ForecastResponse.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy").withZone(ZoneId.systemDefault());

                StringBuilder sb = new StringBuilder("In " + forecast.city.name + " ist die Vorhersage für die nächsten 5 Tage wie folgt:\n");
                forecast.list.stream()
                    .filter(i -> i.dt_txt.contains("12:00:00"))
                    .limit(5) // 
                    .forEach(i -> {
                    String date = formatter.format(Instant.ofEpochSecond(i.dt));
                    sb.append(String.format("    - %s - %.0f°C, %s\n",
                        date,
                        i.main.temp,
                        i.weather[0].description));
                    });
                return sb.toString();
            } else {
                return "Error: Vorhersage für die Stadt nicht gefunden.\n";
            }

        } catch (IOException | InterruptedException e) {
            return "Error: Fehler bei der Verbindung zur API.\n";
        }
    }

    //JSON
    public static class WeatherResponse { Weather[] weather; Main main; Wind wind; String name; }
    private static class Weather { String description; }
    private static class Main { double temp; }
    private static class Wind { double speed; }

    private static class ForecastResponse { City city; java.util.List<ForecastItem> list; }
    private static class City { String name; }
    private static class ForecastItem { long dt; Main main; Weather[] weather; String dt_txt; }
}

