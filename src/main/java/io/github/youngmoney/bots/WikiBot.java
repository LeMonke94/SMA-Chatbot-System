package io.github.youngmoney.bots;

import io.github.youngmoney.infrastructure.adapter.WikipediaApiClient;

public class WikiBot implements IBot {

    private final WikipediaApiClient wikipedia;

    public WikiBot(WikipediaApiClient wikipediaApiClient) {
        this.wikipedia = wikipediaApiClient;
    }

    @Override
    public String getBotName() {
        return "wiki";
    }

    @Override
    public String getBotDescription() {
        return "Liefert Kurzinfos aus Wikipedia";
    }

    @Override
    public String processMessage(String input) {
        String term = (input == null) ? "" : input.trim();
        if (term.isEmpty()) {
            return "Bitte einen Begriff angeben, z. B. `@wiki Bielefeld`.";
        }
        return wikipedia.fetchWikiSummary(term, "de", 3);
    }
}