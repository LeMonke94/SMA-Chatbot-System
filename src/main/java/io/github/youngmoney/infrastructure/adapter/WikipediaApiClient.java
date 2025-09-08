package io.github.youngmoney.infrastructure.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


public class WikipediaApiClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String fetchWikiSummary(String term, String lang, int limit) {
        try {
            if (term == null || term.isBlank()) {
                return "Bitte einen Begriff angeben.";
            }
            if (lang == null || lang.isBlank()) lang = "de";
            if (limit <= 0) limit = 3;

            String url = String.format(
                    "https://%s.wikipedia.org/w/rest.php/v1/search/page?q=%s&limit=%d",
                    lang,
                    URLEncoder.encode(term, StandardCharsets.UTF_8),
                    limit
            );

            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200 || res.body() == null || res.body().isBlank()) {
                return "Keine Daten von Wikipedia erhalten.";
            }

            JsonObject root = gson.fromJson(res.body(), JsonObject.class);
            JsonArray pages = root.has("pages") && root.get("pages").isJsonArray()
                    ? root.getAsJsonArray("pages")
                    : new JsonArray();

            if (pages.size() == 0) {
                return "Kein Treffer für: " + term;
            }

            StringBuilder sb = new StringBuilder("Folgende Information habe ich zu ").append(term).append("\n");
            int count = 0;
            for (JsonElement el : pages) {
                if (!el.isJsonObject()) continue;
                JsonObject p = el.getAsJsonObject();
                String title = getString(p, "title");
                String desc  = getString(p, "description");

                sb.append(" - ")
                  .append(title == null ? "–" : title)
                  .append(": ")
                  .append((desc == null || desc.isBlank()) ? "–" : desc)
                  .append("\n");

                count++;
                if (count >= limit) break;
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "Fehler beim Abrufen von Wikipedia: " + e.getMessage();
        }
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }
}