package io.github.youngmoney.bots;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.youngmoney.infrastructure.adapter.TranslatorApiClient;

public class TranslatorBot implements IBot {
    private final TranslatorApiClient translatorAdapter;

    // --- Eingabe-Pattern ---

    // 1) Sprachangabe (Bsp.: "ins Deutsche", "in die Sprache Englisch", "nach FR", "to EN")
    private static final Pattern TARGET_LANG_PATTERN =
        Pattern.compile("(?:ins|in\\s+die\\s+sprache|nach|to)\\s+([A-Za-zÄÖÜäöüß-]+)", Pattern.CASE_INSENSITIVE);

    // 2) Text nach ":" oder "：" extrahieren
    private static final Pattern AFTER_COLON_TEXT_PATTERN = Pattern.compile("[:：]\\s*(.+)$");

    // 3) Kurzsyntax: EN->DE / EN>DE / EN-DE / EN→DE 
    //    (Bsp.: "EN->DE: Hello", "EN>DE Hello", "EN-DE: Hello", "EN→DE: Hello", "PT->PT-BR: Olá")
    private static final Pattern ARROW_SYNTAX_PATTERN = Pattern.compile(
        "\\b([A-Za-z]{2})\\s*(?:-\\s*>|–\\s*>|→|>|-)\\s*([A-Za-z]{2}(?:-[A-Za-z]{2})?)\\s*[:：]?\\s*(.+)",
        Pattern.CASE_INSENSITIVE
    );

    // Mapping von Sprachnamen auf DeepL-/ISO-Codes
    private static final Map<String, String> NAME_TO_CODE = Map.ofEntries(
        Map.entry("deutsch", "DE"),       Map.entry("deutsche", "DE"),
        Map.entry("englisch", "EN"),      Map.entry("englische", "EN"),
        Map.entry("französisch", "FR"),   Map.entry("französische", "FR"),
        Map.entry("spanisch", "ES"),      Map.entry("spanische", "ES"),
        Map.entry("italienisch", "IT"),   Map.entry("italienische", "IT"),
        Map.entry("portugiesisch", "PT"), Map.entry("portugiesische", "PT"),
        Map.entry("niederländisch", "NL"),Map.entry("niederländische", "NL"),
        Map.entry("polnisch", "PL"),      Map.entry("polnische", "PL"),
        Map.entry("russisch", "RU"),      Map.entry("russische", "RU"),
        Map.entry("japanisch", "JA"),     Map.entry("japanische", "JA"),
        Map.entry("chinesisch", "ZH"),    Map.entry("chinesische", "ZH"),
        Map.entry("koreanisch", "KO"),    Map.entry("koreanische", "KO"),
        Map.entry("türkisch", "TR"),      Map.entry("türkische", "TR"),
        Map.entry("griechisch", "EL"),    Map.entry("griechische", "EL")
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
        return "Übersetzt Texte in eine Zielsprache.\n"; 
    }

    @Override
    public String processMessage(String input) {
        if (input == null || input.isBlank()) {
            return usageError("Error: Fehler bei Eingabetext. Kein Eingabetext übergeben.");
        }

        // --- 1) Kurzsyntax "EN->DE[: ] <text>" (unterstützt auch EN>DE, EN-DE, EN→DE, Doppelpunkt optional)
        Matcher arrow = ARROW_SYNTAX_PATTERN.matcher(input);
        if (arrow.find()) {
            String source = normalizeLangCode(arrow.group(1));
            String target = normalizeLangCode(arrow.group(2));
            String text   = safeTrim(arrow.group(3));
            if (text.isEmpty()) {
                return "Error: Fehler bei Texteingabe. Kein Text hinter der Kurzsyntax angegeben.\n";
            }
            return translatorAdapter.translateText(text, target, source);
        }

        // --- 2) Variante "... ins Deutsche: <text>" / "... nach FR: <text>" / "... to EN: <text>"
        String target = extractTargetLang(input);
        String text   = extractTextAfterColon(input);

        // Optionaler Fallback: auch ohne ":" alles hinter der Sprachphrase als Text nutzen
        if ((text == null || text.isBlank()) && target != null) {
            text = extractTailAfterLanguagePhrase(input);
        }

        if (target == null) {
            return usageError("Error: Fehler bei Zielsprache. Keine Zielsprache gefunden (z. B. \"ins Deutsche\").");
        }
        if (text == null || text.isBlank()) {
            return usageError("Error: Fehler bei Texteingabe. Kein Text angegeben.");
        }

        return translatorAdapter.translateText(text, target);
    }

    // --- Hilfsfunktionen ---

    // Sprache aus Formulierungen wie "ins Deutsche", "nach FR", "to EN" extrahieren
    private String extractTargetLang(String input) {
        Matcher m = TARGET_LANG_PATTERN.matcher(input);
        if (m.find()) {
            String raw = m.group(1);
            if (raw == null) return null;
            String normalized = raw.trim().toLowerCase(Locale.ROOT);
            // Map gebeugte Namen -> Code; sonst: als Code interpretieren (z. B. "FR" oder "pt-br")
            String mapped = NAME_TO_CODE.get(normalized);
            return mapped != null ? mapped : normalizeLangCode(raw);
        }
        return null;
    }

    // Text direkt nach ":" extrahieren (falls vorhanden)
    private String extractTextAfterColon(String input) {
        Matcher m = AFTER_COLON_TEXT_PATTERN.matcher(input);
        if (m.find()) {
            return safeTrim(m.group(1));
        }
        return null;
    }

    // Fallback: Wenn kein ":" vorhanden ist, nimm alles hinter der erkannten Sprachphrase als Text
    private String extractTailAfterLanguagePhrase(String input) {
        Matcher m = TARGET_LANG_PATTERN.matcher(input);
        if (m.find()) {
            int end = m.end(); // Ende der Sprachphrase
            String tail = input.substring(end);
            return safeTrim(tail);
        }
        return null;
    }

    // Normalisiert Sprachcodes (z. B. "de" -> "DE", "pt-br" -> "PT-BR")
    private String normalizeLangCode(String codeOrName) {
        if (codeOrName == null) return null;
        String s = codeOrName.trim();
        // Wenn es ein bekannter Name ist (z. B. "deutsche"), mappe zuerst:
        String nameMapped = NAME_TO_CODE.get(s.toLowerCase(Locale.ROOT));
        if (nameMapped != null) return nameMapped;

        // Sonst als Code behandeln und groß schreiben (mit evtl. Region)
        s = s.replace('_', '-'); // erlaubte Alternative
        if (s.contains("-")) {
            String[] parts = s.split("-", 2);
            String p1 = parts[0].toUpperCase(Locale.ROOT);
            String p2 = parts[1].toUpperCase(Locale.ROOT);
            return p1 + "-" + p2;
        }
        return s.toUpperCase(Locale.ROOT);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String usageError(String msg) {
        return "Error: " + msg + "\n"
             + "Beispiele:\n"
             + " - @translatebot übersetze ins Deutsche: A bird in the hand is worth two in the bush.\n"
             + " - @translatebot EN->DE: Hello World\n"
             + " - @translatebot EN>FR Bonjour\n"
             + " - @translatebot EN-DE: Good morning\n"
             + " - @translatebot nach ES: Guten Morgen\n";
    }
}
