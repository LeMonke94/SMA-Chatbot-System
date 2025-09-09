package io.github.youngmoney.infrastructure.adapter;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TranslatorApiClient {
    private static final String API_URL = "https://api-free.deepl.com/v2/translate";
    private final String apiKey = "21947c34-929d-ad82-932b-5747f7ba2f31:fx";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // ========== Methoden ==========

    // Vereinfachte Übersetzungsmethode ohne Source-Language
    public String translateText(String textInput, String targetLangInput) {
        return translateText(textInput, targetLangInput, null);
    }

    // Übersetzung mit optionaler Angabe der Quellsprache
    public String translateText(String textInput, String targetLangInput, String sourceLangInput) {
        try {
            // Eingabevalidierung
            if (textInput == null || textInput.isBlank()) {
                return "Error: Kein Eingabetext übergeben.\n";
            }
            if (targetLangInput == null || targetLangInput.isBlank()) {
                return "Error: Keine Zielsprache übergeben.\n";
            }

            // Request-Body für die API-Anfrage aufbauen
            String body = buildFormBody(textInput, targetLangInput, sourceLangInput);

            // HTTP-Request mit API-Key und POST-Daten erstellen
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "DeepL-Auth-Key " + apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // Anfrage senden und Antwort als String erhalten
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verarbeitung der Antwort
            if (response.statusCode() == 200) {
                DeeplResponse deepl = gson.fromJson(response.body(), DeeplResponse.class);
                if (deepl != null && deepl.translations != null && deepl.translations.length > 0) {
                    return deepl.translations[0].text;  // Rückgabe der übersetzten Nachricht
                }
                return "Error: Unerwartete Antwort der Übersetzungs-API.\n";
            } else {
                return "Error: API-Fehler (" + response.statusCode() + "): " + response.body() + "\n";
            }
        } catch (IOException | InterruptedException e) {
            return "Error: Fehler bei der Verbindung zur API.\n";
        }
    }

    // ========== Hilfsmethoden (privat) ==========

    // Request-Body als x-www-form-urlencoded zusammenbauen
    private String buildFormBody(String text, String targetLang, String sourceLang) {
        StringBuilder b = new StringBuilder();
        b.append("text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8))
         .append("&target_lang=").append(URLEncoder.encode(normalizeTarget(targetLang), StandardCharsets.UTF_8));
        if (sourceLang != null && !sourceLang.isBlank()) {
            b.append("&source_lang=").append(URLEncoder.encode(sourceLang.toUpperCase(), StandardCharsets.UTF_8));
        }
        return b.toString();
    }

    // Normalisierung von Spracheingaben (z.B. "deutsch" -> "DE")
    private String normalizeTarget(String raw) {
        String r = raw.trim().toLowerCase();
        switch (r) {
            case "de": case "deutsch": case "deutsche": return "DE";
            case "en": case "englisch": return "EN";
            case "fr": case "französisch": return "FR";
            case "es": case "spanisch": return "ES";
            case "it": case "italienisch": return "IT";
            case "nl": return "NL";
            case "pl": return "PL";
            case "pt": return "PT-PT";
            case "pt-br": case "brasilianisch": return "PT-BR";
            case "ru": return "RU";
            case "tr": return "TR";
            default:  return raw.toUpperCase(); 
        }
    }

    // JSON
    public static class DeeplResponse {
        Translation[] translations;
    }

    private static class Translation {
        @SuppressWarnings("unused")
        String detected_source_language;  
        String text;                      
    }
}
