# SMA-Chatbot-System Dokumentation
---

# Inhaltsverzeichnis
1. Einleitung
2. Übersicht / Struktur der Dokumentation
3. Systemdokumentation
    3.1 Einleitung
    3.2 Kontextabgrenzung
    3.3 Lösungsstrategie
    3.4 Bausteinsicht
    3.5 Laufzeitsichten
    3.6 Infrastruktursicht
    3.7 Querschnittliche Konzepte
    3.8 Schnittstellen
    3.9 Risiken und technische Schulden
    3.10 Erweiterungen
    3.11 Fehlerbehebung
    3.12 Installationsanleitung
    3.13 Konfiguration
4. Bot-Dokumentation (Erweiterungen)
    4.1 WetterBot
    4.2 WikiBot
    4.3 ÜbersetzerBot

---

# 1. Einleitung
Das SMA-Chatbot-System ist ein modular aufgebautes erweiterbares Multi-Agents-System, welches als Laufzeitumgebung für verschiedene Chatbots dient. Der vorliegende Prototyp demonstriert die grundlegenden Funktionen eines Bot Basissystems mit der Fähigkeit neue Chatbots hinzuzufügen, einer Benutzerverwaltung und einer persistenten Speicherung des Chatverlaufs.

Projektziele:
- Entwicklung eines lauffähigen Prototyps für ein Chatbot-Basissystem
- Einfache Integration neuer Bots durch standartisierte Schnittstellen
- Mehrbenutzerfähigkeit mit einem Login-System
- Persistente Speicherung von Chatverläufen (100 Nachrichten Mindestens)
- Demonstration der Umgebung anhand von drei Chatbots

Hauptfunktionen:
- Konsolenbasierte Bedienung (TUI) austauschbar durch eine GUI
- Aktivierung/Deaktivierung von Bots
- Benutzerverwaltung mit Login-Funktion (Hardkodiert)
- Chatverlaufspeicherung in SQLite-Datenbank
- Command-Patterns für die Kommunikation mit den Bots

---

# 2. Übersicht / Struktur der Dokumentation
Die Dokumentation gliedert sich in zwei Hauptbereiche:

Systemdokumentation
Darstellung der internen Arbeitsweise der Software:
    - Architektur und Komponenten des Basissystems
    - Schnittstellen zwischen den Modulen
    - Datenbankanbindung
    - Laufzeitverhalten

Bot-Dokumentation (Erweiterungen)
Detaillierte Dokumentation der Implementierten Chatbots:
    - WeterBot (vollständig implementiert)
    - WikiBot (vollständig implementiert)
    - Übersetzerbot (vollständig implementiert)

---

# 3. Systemdokumentation

## 3.1 Einleitung
Die Systemdokumentation beschreibt die interne Arbeitsweise des Chatbot-Systems, einschließlich der Schnittstellen, APIs, sowie der Code-Architektur und Struktur. Sie dient als Referenz für Entwickler, welche das System warten oder weiterentwickeln möchten.


## 3.2 Kontextabgrenzung

### 3.2.1 Fachlicher Kontext
Das System grenzt sich von externen Kommunikationspartnern ab:

|-------------------------|---------------------------------------------|---------------------------------------------|
| Kommunikationsbeziehung | Eingabe                                     | Ausgabe                                     |
|-------------------------|---------------------------------------------|---------------------------------------------|
| Benutzer                | Textbefehle, Login-Daten, Bot-Anfragen      | Chatantworten, Systemmeldungen, Chatverlauf |
| OpenWeatherMap API      | HTTP GET-Anfragen mit Stadt und API-Key     | Wetterdaten als JSON                        |
| Wikipedia REST API      | HTTP GET-Anfragen mit Suchbegriff           | Artikelinformationen als JSON               |
| DeepL API               | HTTP POST-Anfragen mit Text und Zielsprache | Übersetzung als JSON                        |
| SQLite Datenbank        | SQL-Befehle (INSERT, SELECT)                | Gespeicherte Nachrichten                    |
|-------------------------|---------------------------------------------|---------------------------------------------|

### 3.2.2 Technischer Kontext

┌─────────────┐     Console I/O      ┌──────────────────┐
│   Benutzer  │◄────────────────────►│  Console (TUI)   │
└─────────────┘                      └──────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │   ChatSystem     │
                                    └──────────────────┘
                                             │
                ┌────────────────────────────┼────────────────────────────┐
                ▼                            ▼                            ▼
        ┌──────────────┐            ┌──────────────┐            ┌──────────────────┐
        │ UserManager  │            │  BotManager  │            │PersistenceManager│
        └──────────────┘            └──────────────┘            └──────────────────┘
                                             │                            │     JDBC
                                             ▼                            ▼
                                    ┌──────────────┐            ┌──────────────────┐
                                    │     Bot      │            │  SQLite DB       │
                                    └──────────────┘            └──────────────────┘
                                             │      HTTP/HTTPS
                                             ▼
                                    ┌──────────────────┐
                                    │ External APIs    │
                                    └──────────────────┘

Technische Kanäle:
- Console I/O: System-Input/System-Output für Benutzereingabe
- HTTP/HTTPS: REST-API Kommunikation (JSON)
- JDBC: SQLite-Datenbankzugriff


## 3.3 Lösungsstrategie

Technologientscheidungen:
|------------|----------------------------------------------------------------|
| JAVA 24    | Plattformunabhängig, moderne Features                          |
| SQLite     | Leichtgewichtige eingebettete Datenbank, kein Server notwendig |
| GSON       | JSON-Parsing Bibliothek                                        |
| REST-APIs  | Standartisierte externe Datenquellen                           |
|------------|----------------------------------------------------------------|

Architekturentscheidungen:
|------------------------------------|--------------------------------------------------------------------------|
| Schichtenarchitektur               | Klare Trennung in Application, Bots, Domain, Infrastructure und UI Layer |
| Dependency Injection               | Constructor Injection für lose Kopplung                                  |
| Inferface-basierte Bot-Integration | Alle Bots implementieren das IBot-Interface                              |
| Command Pattern                    | Bot Aktivierung über @botname Syntaxis (Variante A aus Anforderungen)    |
|------------------------------------|--------------------------------------------------------------------------|

Entwurfsprinzipien (SOLID)
|-----------------------|-------------------------------------------------------------|
| Single Responsibility | Jede Klasse hat genau eine Aufgabe                          |
| Open/Closed           | Neue Bots können ohne Änderung des Kerns hinzugefügt werden |
| Liskov Substitution   | Alle Bot-Implementierungen sind austauschbar                |
| Interface Segregation | Minimales IBot interface mit 3 Methoden                     |
| Dependency Inversion  | Abhängigeiten zeigen auf Abstraktionen                      |
|-----------------------|-------------------------------------------------------------|


## 3.4 Bausteinsicht

Ebene 1: Gesammtsystem (Black Box)
┌─────────────────────────────────────────────────────────┐
│                    SMA-Chatbot-System                   │
├─────────────────────────────────────────────────────────┤
│ Eingabe: Benutzer-Kommandos, Bot-Anfragen               │
│ Ausgabe: Chat-Antworten, Systemmeldungen                │
│ Zweck: Multi-Bot Chat-Plattform mit Persistenz          │
└─────────────────────────────────────────────────────────┘

Ebene 2: Subsysteme (White Box)
┌─────────────────────────────────────────────────────────┐
│                    SMA-Chatbot-System                   │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │
│ │     UI      │  │ Application │  │ Infrastructure  │   │
│ │   Layer     │  │    Layer    │  │     Layer       │   │
│ └─────────────┘  └─────────────┘  └─────────────────┘   │
│                  ┌─────────────┐  ┌─────────────┐       │
│                  │   Domain    │  │    Bots     │       │
│                  │    Layer    │  │             │       │
│                  └─────────────┘  └─────────────┘       │
└─────────────────────────────────────────────────────────┘

Ebene 3: Hierarchische Darstellung der Komponenten

io.github.youngmoney/
├── App.java
├── application/
│   ├── BotManager.java
│   ├── ChatSystem.java
│   └── UserManager.java
├── bots/
│   ├── IBot.java
│   ├── TranslatorBot.java
│   ├── WetterBot.java
│   └── WikiBot.java
├── domain/
│   ├── BotInfo.java
│   ├── ChatMessage.java
│   └── User.java
├── infrastructure/
│   ├── adapter/
│   │   ├── TranslatorApiClient.java
│   │   ├── WeatherApiClient.java
│   │   └── WikipediaApiClient.java
│   └── persistence/
│       └── PersistenceManager.java
└── ui/
    └── Console.java


Komponentenbeschreibungen:

Application Layer:
    - BotManager: Verwaltet Bot-Lifecycle (aviableBots, activeBots)
    - ChatSystem: Orchestriert Login, Nachrichtenverarbeitung und Persistenz
    - UserManager: Einfache HashMap-basierte Authentifizierung

Bots:
    - IBot: Alle Bots implementieren das Interface
    - TranslatorBot: ÜbersetzerBot Implementation
    - WetterBot: WetterBot Implementation
    - WikiBot: WikiBot Implementation

Domain Layer:
    - BotInfo: Bot-Metadaten (immutable)
    - ChatMessage: Definiert den Aufbau einer Nachricht für die DB (immutable)
    - User: Definiert die Klasse des Benutzers (immutable)

Infrastructure Layer:
    adapter:
        - TranslatorApiClient: Integration der API für den Übersetzerbot
        - WeatherApiClient: Integration der API für den WetterBot
        - WikipediaApiClient: Integration der API für den WikiBot
    persistence:
        - PersistenceManager: Operationen für SQLite

UI Layer:
    - Console: Benutzereingabe/ausgabe weitergabe an ChatSystem


## 3.5 Laufzeitsichten

Szenario 1: Benutzer-Login:
|---------------------------------------------------------------------------------------|
| Console            ChatSystem         UserManager      PersistenceManager      SQLite |
|    │                   │                   │                    │                │    |
|    │─handleLogin()────>│                   │                    │                │    |
|    │                   │──login(user,pw)──>│                    │                │    |
|    │                   │<──User Object─────│                    │                │    |
|    │                   │──loadHistory()────────────────────────>│                │    |
|    │                   │                   │                    │──SELECT───────>│    |
|    │                   │                   │                    │<──Result───────│    |
|    │                   │<──List<ChatMessage>────────────────────│                │    |
|    │<──Welcome+History │                   │                    │                │    |
|    │                   │                   │                    │                │    |
|---------------------------------------------------------------------------------------|

1. Benutzer startet das System  
2. Login-Aufforderung → Eingabe Benutzername/Passwort  
3. Prüfung durch UserManager  
4. Begrüßung des Benutzers und Anzeige des Chatverlaufs

Szenario 2: list Bots, activate bot Wetterbot, deactivate bot Wetterbot:
|--------------------------------------------------------------------|
|  User            Console            ChatSystem          BotManager |
|   │                 │                    │                  │      |
|   │───"list bots"──>│                    │                  │      |
|   │                 │──processInput()───>│                  │      |
|   │                 │                    │───listBots()────>│      |
|   │                 │                    │<──List<BotInfo>──│      |
|   │                 │<───Bot-Liste───────│                  │      |
|   │<───Anzeige──────│                    │                  │      |
|   │                 │                    │                  │      |
|   │                 │                    │                  │      |
|   │──"activate bot  │                    │                  │      |
|   │   wetterbot"───>│                    │                  │      |
|   │                 │──processInput()───>│                  │      |
|   │                 │                    │──activateBot()──>│      |
|   │                 │                    │<────true─────────│      |
|   │                 │<──"Bot aktiviert"──│                  │      |
|   │<───Bestätigung──│                    │                  │      |
|   │                 │                    │                  │      |
|   │                 │                    │                  │      |
|   │─"deactivate bot │                    │                  │      |
|   │   wetterbot"───>│                    │                  │      |
|   │                 │──processInput()───>│                  │      |
|   │                 │                    │─deactivateBot()─>│      |
|   │                 │                    │<────true─────────│      |
|   │                 │<──"Bot deaktiviert"│                  │      |
|   │<───Bestätigung──│                    │                  │      |
|   │                 │                    │                  │      |
|--------------------------------------------------------------------|

##############################################################################################################################
1. Benutzer schreibt "list bots"
2. BotManager prüft ob Botname in aviableBots Liste ist
HIER BESCHREIBUNG WEITERSCHREIBEN
##############################################################################################################################

Szenario 3: Bot-Anfrage (@wetterbot)
|-------------------------------------------------------------------------------------------------------------------------|
| User       Console         ChatSystem        BotManager          WetterBot           WeatherApiClient    OpenWeatherMap |   
|  │            │                 │                │                   │                      │                   │       |  
|  │─"@wetterbot│                 │                │                   │                      │                   │       |
|  │  Berlin"──>│                 │                │                   │                      │                   │       |
|  │            │─processInput()─>│                │                   │                      │                   │       |
|  │            │                 │─assignToBot()─>│                   │                      │                   │       |
|  │            │                 │                │─processMessage()─>│                      │                   │       |
|  │            │                 │                │                   │─extractCity()        │                   │       |
|  │            │                 │                │                   │                      │                   │       |
|  │            │                 │                │                   │─fetchCurrentWeather─>│                   │       |
|  │            │                 │                │                   │                      │───HTTP GET───────>│       |
|  │            │                 │                │                   │                      │<───JSON───────────│       |
|  │            │                 │                │                   │<──────formatted──────│                   │       |
|  │            │                 │                │<────response──────│                      │                   │       |
|  │            │                 │<────response───│                   │                      │                   │       |
|  │            │                 │                │                   │                      │                   │       |
|  │            │<────response────│                │                   │                      │                   │       |
|  │<──Wetter───│                 │                │                   │                      │                   │       |
|  │            │                 │                │                   │                      │                   │       |
|-------------------------------------------------------------------------------------------------------------------------|

##############################################################################################################################
HIER DEN PROZESS BESCHREIBEN
##############################################################################################################################


## 3.6 Infrastruktursicht

Deployment-Umgebung
┌─────────────────────────────────────────┐
│         Entwickler-Workstation          │
│  OS: Windows/Linux/macOS                │
│  JRE: Java 24                           │
│  ┌─────────────────────────────────┐    │
│  │    SMA-Chatbot-System.jar       │    │
│  │  ┌──────────────────────────┐   │    │
│  │  │     Console (TUI)        │   │    │
│  │  └──────────────────────────┘   │    │
│  │  ┌──────────────────────────┐   │    │
│  │  │    Application Core      │   │    │
│  │  └──────────────────────────┘   │    │
│  │  ┌──────────────────────────┐   │    │
│  │  │    SQLite (embedded)     │   │    │
│  │  │  chatbot_history.db      │   │    │
│  │  └──────────────────────────┘   │    │
│  └─────────────────────────────────┘    │
│                  │                      │
│                  │ HTTPS                │
│                  ▼                      │
│  ┌─────────────────────────────────┐    │
│  │     Internet                    │    │
│  │  - OpenWeatherMap API           │    │
│  │  - Wikipedia API                │    │
│  │  - DeepL API                    │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘

Systemanforderungen:
- Java Runtime Environment 24 oder höher
- Internetverbindung für den API-Zugriff
- Schreibrechte im Anwendungsverzeichnis für die DB-Datei


## 3.7 Querschnittliche Konzepte

Fehlerbehandlung:
- Benutzerfreundliche Fehlermeldungen auf Deutsch
- Keine ungefangenen Exceptions in Bot-Implementierungen

Persistenzkonzept:
- Prepared Statements gegen SQL-Injection
- Automatische DB-Initialisierung beim Start
- Maximal 100 Nachrichten im Chatverlauf für bessere Performanz

Bot-Integration Pattern:
```java
public interface IBot {
    String getBotName();
    String getBotDescription();
    String processMessage(String input);
}
```

Textanalyse:
- Pattern-basierte Texterkennung durch Regex
- Keine KI/NLP erforderlich
- Case-Sensitive

Sicherheit:
- Einfaches Login, hardcodiert (Prototyp Phase)
- API-Keys müssen noch externalisiert werden


## 3.8 Schnittstellen

Interne Schnittstellen:
|-----------------------|----------------|--------------------------------|
| Schnittstelle         | Typ            | Beschreibung                   |
|-----------------------|----------------|--------------------------------|
| IBot                  | Java Interface | 3 Methoden für Bot-Integration |
| ChatSystem            | Java API       | Steuerung aller Komponenten    |
| PersistenceManager    | Java API       | Datenbankzugriff               |
|-----------------------|----------------|--------------------------------|

Externe Schnittstellen:
|----------------|------------|--------|----------------------------------|
| System         | Protokoll  | Format | Endpoint                         |
|----------------|------------|--------|----------------------------------|
| OpenWeatherMap | HTTPS/REST | JSON   | api.openweathermap.org/data/2.5/ |
| Wikipedia      | HTTPS/REST | JSON   | wikipedia.org/w/rest.php/v1/     |
| DeepL          | HTTPS/REST | JSON   | api-free.deepl.com/v2/           |
| SQLite         | JDBC       | SQL    | Lokale Datei chatbot_history.db  |
|----------------|------------|--------|----------------------------------|

API-Spezifikation OpenWeatherMap
    Current Weather:
        - Endpoint: /weather?q={city}&appid={key}&units=metric&lang=de
        - Method:   GET
        - Response: JSON mit temp, wind, weather array
    Forecast:
        - Endpoint: /forecast?q={city}&appid={key}&units=metric&lang=de
        - Method:   GET
        - Response: JSON mit 5-Tage Vorhersage

API-Spezifikation Wikipedia
##############################################################################################################################
HIER API SPEZIFIKATION FÜR DAVID
##############################################################################################################################

API-Spezifikation DeepL
##############################################################################################################################
HIER API SPEZIFIKATION FÜR YUSUF
##############################################################################################################################


## 3.9 Risiken und technische Schulden

Identifizierte Risiken:
|--------------------------|---------------------|------------|-----------------------------------------|
| Risiko                   | Wahrscheinlichkeit  | Auswirkung | Maßnahme                                |
|--------------------------|---------------------|------------|-----------------------------------------|
| API-Key Kompromittierung | Hoch                | Mittel     | Externalisierung in einer Config-Datei  |
| SQL-Injection            | Niedrig             | Hoch       | Prepared Statements (bereits umgesetzt) |
| Concurrent Access        | Mittel              | Mittel     | Single-Thread Design                    |
|--------------------------|---------------------|------------|-----------------------------------------|

Technische Schulden:

1. Sicherheit:
    - API-Key hardkodiert in TranslatorApiClient.java, WeatherApiClient.java und WikipediaApiClient.java
    - Passwörter in Klartext in UserManager.java
    - Keine Verschlüsselung der Kommunikation

2. Code-Qualität:
    - Gemischte Sprachen in den Kommentaren (Deutsch mit Englisch)
    - Fehlende Unit-Tests

3. Architektur:
    - Direkte Kopplung an SQLite
    - Keine Konfigurationsdatei
    - Single-Threading

4. Verletzung von Prinzipien:
    - DRY: Wiederholung von String-Konstanten
    - Tell-Don't-Ask: Teilweise prozeduraler Code


## 3.10 Erweiterungen
Anleitung zum Hinzufügen neuer Chatbots

Schritt 1: Bot-Klasse implementieren
```java
package io.github.youngmoney.bots;

public class MeinNeuerBot implements IBot {

    public MeinNeuerBot(BotApiClient botAdapterInput) { //Optional
    this.botAdapter = botAdapterInput;
    }
    
    @Override
    public String getBotName() {
        return "meinbot"; // Name für den Bot
    }
    
    @Override
    public String getBotDescription() {
        return "Beschreibung des Bots für die Liste";
    }
    
    @Override
    public String processMessage(String input) {
        // Bot-Logik hier
        if (input == null || input.isBlank()) {
            return "Bitte geben Sie eine Eingabe ein.\n";
        }
        
        // Verarbeitung
        return "Antwort des Bots\n";
    }
}
```

Schritt 2: API-Client erstellen (Optional)
```java
package io.github.youngmoney.infrastructure.adapter;

public class MeinApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    public String fetchData(String query) {
        // API-Aufruf implementieren
        // Fehlerbehandlung nicht vergessen
        return "API-Antwort";
    }
}
```

Schritt 3: Bot in App.java registrieren
```java
// In App.java main()-Methode:
// Bestehende Bots
WetterBot wetterBot = new WetterBot(weatherAdapter);
//...

// Neuen Bot hinzufügen
MeinApiClient meinAdapter = new MeinApiClient();
MeinNeuerBot meinBot = new MeinNeuerBot(meinAdapter);

// In Bot-Liste aufnehmen
List<IBot> allBots = List.of(wetterBot, ..., meinBot); // <- hier ergänzen
botManager.loadBots(allBots);
```

Wichtig zu beachten:
    - Bot-Name muss eindeutig sein
    - processMessage() darf keine Exceptions werfen
    - Immer String mit neuer Zeile zurückgeben
    - Fehler als Benutzerfreundliche Nachricht definieren


## 3.11 Fehlerbehebung
|---------------------------|--------------------------------|-------------------------------|
| Problem                   | Ursache                        | Lösung                        |
|---------------------------|--------------------------------|-------------------------------|
| "Stadt nicht gefunden"    | Stadt nicht nach "in"          | Syntax: "in Berlin" verwenden |
| "Bot nicht aktiviert"     | Bot ist nicht aktiv            | activate bot botname eingeben |
| "API-Problem aufgetreten" | API-Key fehlt/ungültig         | API-Key in adapter prüfen     |
| "Database locked"         | Paralleler Zugriff             | Anwendung neu starten         |
| Login fehlgeschlagen      | Falscher Benutzername/Passwort | Daten im UserManager prüfen   |
| Keine Historie            | Neue DB/Benutzer               | Normal beim ersten Login      |
|---------------------------|--------------------------------|-------------------------------|


## 3.12 Installationsanleitung

Voraussetzungen
    - JDK 24+
    - Git (optional für Repository-Clone)
    - Maven (optional für Build)
    - Internetverbindung für API-Zugriffe

Installationsprozess:

1. Repository klonen
    Bash commands:
        - git clone https://github.com/LeMonke94/SMA-Chatbot-System.git
        - cd SMA-Chatbot-System

2. API-Keys konfigurieren:
    API-Keys für die jeweiligen APIs besorgen
    z.B. für OpenWeatherMap: https://openweathermap.org/api
    In WeatherApiClient.java Zeile 18 eintragen:
        - private final String apiKey = "IHR_API_KEY_HIER";

3. Dependencies bereitstellen:

    Projekt-Dependencies:
        - junit:junit:4.11
        - org.xerial:sqlite-jdbc:3.45.1.0
        - com.google.code.gson:gson:2.10.1

    Build-Plugins:
        - maven-clean-plugin:3.1.0
        - maven-resources-plugin:3.0.2
        - maven-compiler-plugin:3.8.0
        - maven-surefire-plugin:2.22.1
        - maven-jar-plugin:3.0.2
        - maven-install-plugin:2.5.2
        - maven-deploy-plugin:2.8.2
        - maven-site-plugin:3.7.1
        - maven-project-info-reports-plugin:3.0.0

4. Kompilieren:
    Bash command:
        - mvn clean compile
    Oder alternativ:
        - javac -cp "lib/*" -d out src/main/java/io/github/youngmoney/**/*.java

5. Ausführen:
    Basg command:
        - mvn exec:java -Dexec.mainClass="io.github.youngmoney.App"
    Oder alternativ:
        - java -cp "out:lib/*" io.github.youngmoney.App

Ersten Schritte:
Nach dem Start der Anwendung mit Username, Passwort einloggen. Bot der Wahl aktivieren, Bot nutzen, Programm beenden.


## 3.13 Konfiguration
Aktueller Stand (Hardkodiert)

Derzeit sind Konfigurationen im Code verteilt:
    API-Keys in jeweiligen ApiClient-Klassen
    Benutzer in UserManager
    DB-Pfad in PersistenceManager

Geplante Konfiguration (config.properties)

    # API Keys
    weather.api.key=047ff19bc5a4b610adfaf6896396749c
    deepl.api.key=IHR_DEEPL_KEY
    wiki.api.url=https://de.wikipedia.org/w/rest.php/v1/

    # Database
    db.path=./chatbot_history.db
    db.history.limit=100

    # System
    language.default=de
    console.encoding=UTF-8

    # Users (Temporär für Prototyp)
    users.predefined=User:Passwort,Martin:123,Jordan:999

---

# 4. Bot-Dokumentation (Erweiterungen)

## 4.1 WetterBot
Kontextabgrenzung:

┌──────────┐    Nachricht     ┌────────────┐     HTTP      ┌──────────────────┐
│ Benutzer │─────────────────►│ WetterBot  │──────────────►│ OpenWeatherMap   │
└──────────┘                  └────────────┘               └──────────────────┘
     ▲                              │                              │
     │                              │          JSON                │
     └──────────────────────────────┴──────────────────────────────┘
                      Formatierte Wetterinfo

Lösungsstrategie:
    - Pattern Matching: Regex "in (.+)" für Stadtextraktion
    - Keyword-Erkennung: "wird"/"vorhersage" für Forecast
    - Fehlerbehandlung: Benutzerfreundliche deutsche Meldungen
    - API-Integration: Adapter Pattern mit WeatherApiClient

Bausteinsicht:
WetterBot
├── weatherAdapter: WeatherApiClient  // Dependency Injection
├── cityPattern: Pattern              // Regex
├── getBotName(): "Wetterbot"
├── getBotDescription(): String
├── processMessage(String): String    // Hauptlogik
└── extractCity(String): String       // Helfer für cityPattern

Laufzeitsicht:

Szenario: Aktuelles Wetter
    - Input: "@wetterbot wie ist das Wetter in Berlin?"
    - processMessage() aufgerufen
    - extractCity() → "Berlin"
    - Prüfung Keywords → kein "wird"
    - weatherAdapter.fetchCurrentWeatherData("Berlin")
    - API-Call: GET /weather?q=Berlin
    - JSON-Parsing mit GSON
    - Formatierung: "In Berlin ist es aktuell:\n - Temperatur: 15.2°C\n..."

Szenario: Wettervorhersage
    - Input: "@wetterbot wie wird das Wetter in Hamburg?"
    - Keyword "wird" erkannt
    - weatherAdapter.fetchForecastWeatherData("Hamburg")
    - API-Call: GET /forecast?q=Hamburg
    - Filterung auf 12:00:00 Einträge
    - Formatierung als 5-Tages-Übersicht

Schnittstellen:
    Eingabe-Pattern:
        @wetterbot wie ist das Wetter in {STADT}?
        @wetterbot wie wird das Wetter in {STADT}?
        @wetterbot vorhersage für {STADT}

Ausgabe-Format:
Aktuelles Wetter:
In {Stadt} ist es aktuell:
  - Temperatur: {temp}°C
  - Wind: {speed} m/s
  - {description}

Vorhersage:
In {Stadt} ist die Vorhersage für die nächsten 5 Tage:
  - {Tag}, {Datum} - {temp}°C, {description}
  - ...

API-Integration:
    - Endpoint: api.openweathermap.org/data/2.5/
    - Format: JSON
    - Authentifizierung: API-Key als Query-Parameter
    - Rate Limit: 1000 Anfragen/Tag (Free Tier)


## 4.2 WikiBot
##############################################################################################################################
HIER BOT DOKU FÜR WIKIBOT
##############################################################################################################################


## 4.3 ÜbersetzerBot
##############################################################################################################################
HIER BOT DOKU FÜR ÜBERSETZER
##############################################################################################################################