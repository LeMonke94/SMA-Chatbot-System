package io.github.youngmoney.bots;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.youngmoney.infrastructure.adapter.TranslatorApiClient;

public class TranslatorBot implements IBot {
    private final TranslatorApiClient translatorAdapter;

    // --- Eingabe-Parsing ---
    // erkennt "ins deutsche", "nach EN", "to FR" ...
    private static final Pattern TARGET_LANG_PATTERN =
        Pattern.compile("(?:ins|in die sprache|nach|to)\\s+([A-Za-zÄÖÜäöüß-]+)", Pattern.CASE_INSENSITIVE);

    // zieht den Text hinter ":" (auch Fullwidth-„：“) heraus
    private static final Pattern AFTER_COLON_TEXT_PATTERN = Pattern.compile("[:：]\\s*(.+)$");

    // Kurzsyntax: EN->DE: Text (Pfeile -, – , → , > sind erlaubt; Ziel kann z.B. PT-BR sein)
    private static final Pattern ARROW_SYNTAX_PATTERN = Pattern.compile(
        "\\b([A-Za-z]{2})\\s*(?:-\\s*>|–\\s*>|→)\\s*([A-Za-z]{2}(?:-[A-Za-z]{2})?)\\s*:\\s*(.+)",
        Pattern.CASE_INSENSITIVE
    );

    // Mapping von Namen auf Codes (Fallback: Großschreibung des Inputs)
    private static final Map<String, String> NAME_TO_CODE = Map.of(
        "deutsch", "DE", "deutsche", "DE",
        "englisch", "EN", "französisch", "FR",
        "spanisch", "ES", "italienisch", "IT"
    );

    public TranslatorBot(TranslatorApiClient translatorAdapter) {
        this.translatorAdapter = translatorAdapter;
    }

    @Override
    public String getBotName() {
        return "translatebot"; // @translatebot
    }

    @Override
    public String getBotDescription() {
        return "Übersetzt Texte in eine Zielsprache. Beispiel:\n" +
               " - @translatebot übersetze folgende Nachricht ins deutsche: A bird in the hand ...\n" +
               " - @translatebot EN->DE: Good morning";
    }

    @Override
    public String processMessage(String input) {
        if (input == null || input.isBlank()) {
            return "Error: Fehler bei Eingabetext. Kein Eingabetext übergeben.\n" +
                   "Beispiel:\n - @translatebot übersetze folgende Nachricht ins deutsche: A bird in the hand is worth two in the bush.\n" +
                   " - @translatebot EN->DE: Hello World\n";
        }

        // 1) Kurzsyntax "EN->DE: <text>"
        Matcher arrow = ARROW_SYNTAX_PATTERN.matcher(input);
        if (arrow.find()) {
            String source = arrow.group(1).toUpperCase(Locale.ROOT);
            String target = arrow.group(2).toUpperCase(Locale.ROOT);
            String text   = arrow.group(3).trim();
            if (text.isEmpty()) {
                return "Error: Fehler bei Texteingabe. Kein Text nach dem ':' angegeben.\n";
            }
            return translatorAdapter.translateText(text, target, source);
        }

        // 2) Variante "... ins deutsche: <text>"
        String target = extractTargetLang(input);
        String text = extractText(input);

        if (target == null) {
            return "Error: Fehler bei Zielsprache. Keine Zielsprache gefunden (z. B. 'ins deutsche').\n";
        }
        if (text == null || text.isBlank()) {
            return "Error: Fehler bei Texteingabe. Kein Text nach dem ':' angegeben.\n";
        }

        return translatorAdapter.translateText(text, target);
    }

    // Sprache aus Formulierungen wie "ins deutsche" extrahieren
    private String extractTargetLang(String input) {
        Matcher m = TARGET_LANG_PATTERN.matcher(input);
        if (m.find()) {
            String raw = m.group(1);
            if (raw == null) return null;
            String normalized = raw.trim().toLowerCase(Locale.ROOT);
            return NAME_TO_CODE.getOrDefault(normalized, normalized.toUpperCase(Locale.ROOT));
        }
        return null;
    }

    // Text hinter ":" extrahieren
    private String extractText(String input) {
        Matcher m = AFTER_COLON_TEXT_PATTERN.matcher(input);
        if (m.find()) return m.group(1).trim();
        return null;
    }
}
