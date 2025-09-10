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
   3.9 Risiken & technische Schulden  
   3.10 Erweiterungen  
   3.11 Fehlerbehebung  
   3.12 Installationsanleitung  
   3.13 Konfiguration  
4. Bot-Dokumentation  
   4.1 Wetter-Bot  
   4.2 Wiki-Bot  
   4.3 Übersetzer-Bot  

---

## 1. Einleitung
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

## 2. Übersicht über die Struktur der Dokumentation

Die Dokumentation gliedert sich in zwei Hauptteile:  

- Systemdokumentation:  
  Darstellung des Basissystems, seiner Komponenten und Architektur.  
- Bot-Dokumentation:  
  Eigene Kapitel für die drei implementierten Bots mit Aufbau, Schnittstellen und Laufzeitsicht.  

---

## 3. Systemdokumentation

### 3.1 Einleitung
Die Systemdokumentation liefert eine detaillierte Sicht auf die Architektur und die Interaktionen der Hauptkomponenten.  
Sie dient als Referenz für Entwickler, die das System warten oder erweitern möchten.

---

### 3.2 Kontextabgrenzung

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

---

### 3.3 Lösungsstrategie

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

---

### 3.4 Bausteinsicht

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

---

### 3.5 Laufzeitsichten

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
|    │<─Welcome + History│                   │                    │                │    |
|    │    + HelpMessage──│                   │                    │                │    |
|---------------------------------------------------------------------------------------|

Ablaufbeschreibung für den Benutzer-Login:
1. System-Start: Benutzer startet die Anwendung über App.main(), Console wird initialisiert  
2. Login-Prompt: Console zeigt "Bitte den Benutzernamen eingeben:" und wartet auf Eingabe 
3. Logindaten-Eingabe: Benutzer gibt Username und Passwort ein (z.B. "User" / "Passwort") 
4. Authentifizierung: ChatSystem ruft UserManager.login() auf, der die HashMap mit den Login-Daten prüft
5. User-Objekt: Bei erfolgreicher Prüfung wird ein User-Objekt erstellt und zurückgegeben
6. Verlauf laden: ChatSystem ruft PersistenceManager.loadHistory() mit dem eingeloggten User auf
7. SQL-Abfrage: SELECT * FROM chat_history WHERE recipient = ? ORDER BY timestamp DESC LIMIT 100
8. Verlauf aufbereiten: Die letzten 100 Nachrichten werden in chronologischer Reihenfolge sortiert
9. Willkommensnachricht: StringBuilder erstellt Begrüßung mit Username und Chatverlauf
10. Hilfe anzeigen: Automatische Anzeige der verfügbaren Befehle (list bots, activate bot, etc.)

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

Ablaufbeschreibung für "list bots":
1. Befehlseingabe: Benutzer tippt "list bots" in die Console
2. String-Matching: ChatSystem prüft mit equalsIgnoreCase() auf bekannte Befehle
3. Bot-Abfrage: BotManager iteriert über availableBots Liste
4. Status-Check: Für jeden Bot wird geprüft, ob er in activeBots Liste ist
5. BotInfo-Erstellung: Neue BotInfo-Objekte mit Name, Beschreibung und Active-Status
6. Formatierte Ausgabe: StringBuilder erstellt Liste mit "- Botname (aktiv/nicht aktiv) - Beschreibung"
7. Anzeige: Console gibt die formatierte Liste aus

Ablaufbeschreibung für "activate bot wetterbot":
1. Befehlseingabe: Benutzer tippt "activate bot wetterbot"
2. String-Parsing: ChatSystem extrahiert Botname mit substring(13)
3. Bot-Suche: BotManager sucht in availableBots mit findAvailableBot()
4. Verfügbarkeitsprüfung: Stream-Filter mit equalsIgnoreCase() auf Bot-Namen
5. Aktivierung: Bot wird zu activeBots Liste hinzugefügt (wenn nicht schon drinnen)
6. Rückgabe: Boolean true bei Erfolg, false wenn Bot nicht gefunden
7. Bestätigung: "Bot 'wetterbot' aktiviert." oder Fehlermeldung "Error: Bot wetterbot wurde nicht gefunden" wird ausgegeben

Ablaufbeschreibung für "deactivate bot wetterbot"
1. Befehlseingabe: Benutzer tippt "deactivate bot wetterbot"
2. String-Parsing: ChatSystem extrahiert Botname mit substring(15)
3. Bot-Suche: BotManager sucht in availableBots mit findAvailableBot()
4. Deaktivierung: Bot wird aus activeBots Liste entfernt mit remove()
5. Bestätigung: "Bot 'wetterbot' deaktiviert." oder Fehlermeldung "Error: Bot wetterbot wurde nicht gefunden" wird ausgegeben

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

Ablaufbeschreibung für eine Botanfrage am Beispiel des Wetterbots:
1. Bot-Anfrage: Benutzer gibt "@wetterbot wie ist das Wetter in Berlin?" ein
2. @-Erkennung: ChatSystem erkennt "@" am Anfang und ruft BotManager.assignToBot() auf
3. Bot-Name extrahieren: String wird bei Leerzeichen gesplittet, "@wetterbot" → "wetterbot"
4. Bot-Suche: BotManager iteriert über activeBots Liste
5. Name-Matching: Vergleich mit getBotName().equalsIgnoreCase("wetterbot")
6. Message-Weitergabe: Rest der Nachricht "wie ist das Wetter in Berlin?" an Bot
7. Stadt-Extraktion: WetterBot nutzt Regex Pattern.compile("in (.+)")
8. Regex-Matching: Matcher findet "Berlin" nach "in"
9. Keyword-Check: Prüfung auf "wird"/"vorhersage" für Forecast-Entscheidung
10. API-Aufruf: fetchCurrentWeatherData("Berlin") wird aufgerufen
11. URL-Encoding: Stadt wird URL-encoded: "Berlin" → "Berlin"
12. HTTP-Request: GET-Request an api.openweathermap.org/data/2.5/weather?q=Berlin&appid=KEY&units=metric&lang=de
13. JSON-Response: OpenWeatherMap liefert JSON mit temp, wind, weather array
14. GSON-Parsing: JSON wird in WeatherResponse-Objekt umgewandelt
15. Formatierung: String.format() erstellt "In Berlin ist es aktuell:\n - Temperatur: 15°C\n..."
16. Rückgabe-Kette: Formatierter String → WetterBot → BotManager → ChatSystem
17. Ausgabe: Console zeigt die Wetterinformation dem Benutzer an

Szenario 4: Nachrichtenspeicherung (Querschnittsfunktion)
|----------------------------------------------------------------------------------------------------------------|
| User/Bot       Console       ChatSystem         PersistenceManager         SQLite DB                           |
|    │              │                 │                  │                       │                               |
|    │              |                 |                  |                       |                               |
|───────────────────────────────────────────────BENUTZEREINGABE──────────────────────────────────────────────────|
|    │              │                 |                  |                       |                               |
|    │─"Nachricht"─>│                 │                  │                       │                               |
|    │              │─processInput()─>│                  │                       │                               |
|    │              │                 │                  │                       │                               |
|    │              │                 │─saveMessage()───>│                       │                               |
|    │              │                 │  (User→User)     │                       │                               |
|    │              │                 │                  │─INSERT INTO──────────>|                               |
|    │              │                 │                  │ chat_history          │                               |
|    │              │                 │                  │ (sender='User',       │                               |
|    │              │                 │                  │  recipient='User',    │                               |
|    │              │                 │                  │  content='Nachricht', │                               |
|    │              │                 │                  │  timestamp=NOW)       │                               |
|    │              │                 │                  │<──────OK──────────────│                               |
|    │              │                 │<─────OK──────────│                       │                               |
|    │              │                 │                  │                       │                               |
|    │              │                 │                  │                       │                               |
|────────────────────────────────────────────────────BOT-ANTWORT─────────────────────────────────────────────────|
|    │              │                 │                  │                       │                               |
|    │              │                 │─saveMessage()───>│                       │                               |
|    │              │                 │  (System→User)   │                       │                               |
|    │              │                 │                  │─INSERT INTO────────────>                              |
|    │              │                 │                  │ chat_history          │                               |
|    │              │                 │                  │ (sender='System',     │                               |
|    │              │                 │                  │  recipient='User',    │                               |
|    │              │                 │                  │  content='Antwort',   │                               |
|    │              │                 │                  │  timestamp=NOW)       │                               |
|    │              │                 │                  │<──────OK──────────────│                               |
|    │              │                 │<────────OK───────│                       │                               |
|    │              │<────"Antwort"───│                  │                       │                               |
|    │<────Display──│                 │                  │                       │                               |
|    │              │                 │                  │                       │                               |
|----------------------------------------------------------------------------------------------------------------|

Ablaufbeschreibung der Nachrichtenspeicherung:

BENUTZEREINGABE:
1. Eingabe erfassen: Sobald der Benutzer eine Nachricht eingibt und Enter drückt
2. ChatMessage-Objekt erstellen
    - Sender: currentUser.getUsername()
    - Recipient: currentUser.getUsername() (an sich selbst)
    - Content: Die eingegebene Nachricht
    - Timestamp: LocalDateTime.now()
3. SQL-Prepared Statement:
    - INSERT INTO chat_history(sender, recipient, content, timestamp) 
    - VALUES (?, ?, ?, ?)
4. Parameter binden: Die Werte werden sicher über PreparedStatement eingefügt
5. Transaktion: Commit

BOT-ANTWORT:
1. Antwort generiert: Nachdem die Verarbeitung abgeschlossen ist
2. ChatMessage-Objekt erstellen
    - Sender: "System" (fester String)
    - Recipient: currentUser.getUsername()
    - Content: Die generierte Antwort (Bot-Response oder System-Meldung)
    - Timestamp: LocalDateTime.now()
3. SQL-Prepared Statement:
    - INSERT INTO chat_history(sender, recipient, content, timestamp) 
    - VALUES (?, ?, ?, ?)
4. Parameter binden: Die Werte werden sicher über PreparedStatement eingefügt
5. Transaktion: Commit

Integration in andere Szenarien:
Login: Keine Speicherung der Login-Daten selbst
Bot-Befehle: Sowohl "list bots", "activate bot" etc. als auch deren Antworten werden gespeichert
Bot-Anfragen: "@wetterbot Berlin" und die Wetterantwort werden beide gespeichert
Exit: Der "exit"-Befehl wird gespeichert, aber keine System-Antwort mehr

Besonderheiten:
Synchron: Speicherung erfolgt synchron vor der weiteren Verarbeitung
Keine Batch-Operations: Jede Nachricht wird einzeln gespeichert
Try-with-resources: Automatisches Schließen der DB-Verbindungen
Historie-Limit: Beim Laden werden nur die letzten 100 Nachrichten abgerufen
Chronologische Sortierung: Nachrichten werden beim Laden umgekehrt (älteste zuerst)

Datenbankstruktur:
```SQL
CREATE TABLE IF NOT EXISTS chat_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender TEXT NOT NULL,
    recipient TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp TEXT NOT NULL
);
```

---

### 3.6 Infrastruktursicht 

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
- Java Runtime Environment 24 
- Internetverbindung für den API-Zugriff
- Schreibrechte im Anwendungsverzeichnis für die DB-Datei

---

### 3.7 Querschnittliche Konzepte

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

---

### 3.8 Schnittstellen

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
    Search Page:
        - Endpoint: /w/rest.php/v1/search/page?q={term}&limit={n}
        - Method:   GET
        - Response: JSON mit title, description, excerpt
  
API-Spezifikation DeepL
    Translate:
        - Endpoint: /translate
        - Method:   POST
        - Request:  Form-Parameter 
                        text=<zu_übersetzender_Text>
                        target_lang=<Zielsprache, z.B. DE, EN, FR>
                        source_lang=<optionale_Quellsprache>
                        formality=<optional: more|less>
        - Auth:     Header "Authorization: DeepL-Auth-Key {API_KEY}"
        - Response: JSON mit translations[0].text (Übersetzung) 
                    und detected_source_language
    Languages:
        - Endpoint: /languages?type=target|source
        - Method:   GET
        - Response: JSON Liste verfügbarer Sprachen mit Codes (z.B. DE, EN)

---

### 3.9 Risiken & Technische Schulden

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

---

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

Schritt 2: API-Client erstellen 
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

---

### 3.11 Fehlerbehebung

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

---

### 3.12 Installationsanleitung 

Systemvoraussetzungen:
    - JDK 24+
    - Git (optional für Repository-Clone)
    - Maven (optional für Build)
    - Internetverbindung für API-Zugriff

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

---

### 3.13 Konfiguration

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

---

## 4.2 WikiBot

Kontextabgrenzung:

┌──────────┐    Nachricht     ┌──────────┐      HTTP       ┌─────────────┐
│ Benutzer │─────────────────►│ WikiBot  │────────────────►│ Wikipedia   │
└──────────┘                  └──────────┘                 └─────────────┘
     ▲                              │                          │
     │                              │         JSON             │
     └──────────────────────────────┴──────────────────────────┘
                  Formatierte Kurzbeschreibung

Lösungsstrategie:
    - Pattern Matching: Regex-Erkennung von Fragen mit „Was ist …“, „Wer ist …“, oder einfache Schlagwortsuche
    - Keyword-Erkennung: Fokus auf das Schlagwort nach @wiki
    - Fehlerbehandlung: Meldungen bei „keine Ergebnisse“ oder falschen Eingaben
    - API-Integration: Nutzung des Wikipedia REST-API Endpoints /search/page über einen WikiApiClient (Adapter Pattern)
  
Bausteinsicht:
WikiBot
├── wikiAdapter: WikiApiClient         // Dependency Injection
├── queryPattern: Pattern              // Regex für Begriffe
├── getBotName(): "WikiBot"
├── getBotDescription(): String
└── processMessage(String): String     // Hauptlogik

Laufzeitsicht:

Szenario: Begriff abfragen
    - Input: "@wiki Was ist Bielefeld?"
    - processMessage() wird aufgerufen
    - Regex extrahiert Suchbegriff → "Bielefeld"
    - API-Call: GET /w/rest.php/v1/search/page?q=Bielefeld&limit=3
    - JSON-Parsing mit GSON → Liste von Treffern (Titel + Beschreibung)
    - Ausgabe formatiert:
      Folgende Information habe ich zu Bielefeld:
       - Bielefeld: Großstadt in NRW, Deutschland
       - Arminia Bielefeld: Sportverein aus Bielefeld
       - Bielefeld-Verschwörung: populäre Satire über die Stadt Bielefeld

Szenario: Kein Treffer
    - Input: "@wiki Was ist Foobarstadt?"
    - API-Response: leere Liste
    - Ausgabe:
      Kein Treffer für: {BEGRIFF}

Schnittstellen:
    Eingabe-Pattern:
        @wiki Was ist {BEGRIFF}?
        @wiki Wer ist {BEGRIFF}?
        @wiki {BEGRIFF}

    Ausgabe-Format:
    Folgende Information habe ich zu {BEGRIFF}:
      - {Titel1}: {Beschreibung1}
      - {Titel2}: {Beschreibung2}
      - ...

API-Integration:
    - Endpoint: https://de.wikipedia.org/w/rest.php/v1/search/page
    - Method: GET
    - Parameter:
        - q={term} → Suchbegriff
        - limit={n} → max. Trefferanzahl (Standard: 3)
    - Response: JSON mit title, description, excerpt
    - Authentifizierung: keine notwendig
    - Rate Limit: abhängig von Wikipedia, aber großzügig (öffentlich nutzbar)

---

## 4.3 ÜbersetzerBot

Kontextabgrenzung:

┌──────────┐    Nachricht     ┌──────────────┐     HTTP       ┌───────────────┐
│ Benutzer │─────────────────►│ ÜbersetzerBot│───────────────►│ DeepL API      │
└──────────┘                  └──────────────┘                └───────────────┘
     ▲                              │                               │
     │                              │           JSON                │
     └──────────────────────────────┴───────────────────────────────┘
                      Übersetzter Text

Lösungsstrategie:
   - Pattern Matching: Regex für Erkennung von Zielsprachen ("nach EN", "ins Deutsche", "to FR")
   - Kurzsyntax: EN->DE / EN>DE / EN-DE / EN→DE : Text zur direkten Quell- und Zielsprachenangabe
   - Fehlerbehandlung: Rückmeldungen bei nicht erkannten Sprachen oder leeren Texten
   - API-Integration: Adapter Pattern mit TranslatorApiClient (DeepL)

Bausteinsicht:
ÜbersetzerBot
├── translatorAdapter: TranslatorApiClient  // Dependency Injection
├── targetLangPattern: Pattern              // Regex für Sprachangabe
├── arrowSyntaxPattern: Pattern             // Kurzsyntax EN->DE / EN>DE / EN-DE / EN→DE
├── afterColonPattern: Pattern              // Text nach ":" extrahieren
├── getBotName(): "translatebot"
├── getBotDescription(): String
└── processMessage(String): String          // Hauptlogik

Laufzeitsicht:

Szenario: Standardübersetzung
- Input: `@translatebot übersetze ins Deutsche: A bird in the hand is worth two in the bush.`
- Parsing: Regex erkennt Ziel `DE` aus der Formulierung *„ins Deutsche“*.
- API-Call: `POST /v2/translate` mit `target_lang=DE`
- Verarbeitung: JSON-Parsing mit GSON
- Ausgabe: `Der Spatz in der Hand ist besser als die Taube auf dem Dach.`

Szenario: Kurzsyntax
- Input: `@translatebot EN->FR: Good morning`
- Parsing: Kurzsyntax-Parser erkennt `source_lang=EN`, `target_lang=FR`, Text=`Good morning`
- API-Call: `POST /v2/translate` mit `source_lang=EN`, `target_lang=FR`
- Ausgabe: `Bonjour`

Ansprechmöglichkeiten

| Eingabe (Beispiel)                                                                   | Ausgabe (Beispiel)                     |
|--------------------------------------------------------------------------------------|----------------------------------------|
| `@translatebot übersetze ins Deutsche: A bird in the hand is worth two in the bush.` | `Der Spatz in der Hand ist besser ...` |
| `@translatebot` → `DE` → `A bird in the hand is worth two in the bush.`              | `Der Spatz in der Hand ist besser ...` |
| `@translatebot Übersetze ins Französische: Guten Morgen, wie geht es dir?`           | `Bonjour, comment ça va ?`             |
| `@translatebot EN->FR: Good morning`                                                 | `Bonjour`                              |

Fehlermeldungen:
Bei fehlerhafter Eingabe zeigt der Bot eine Hilfe mit Beispielen. 

Schnittstellen:
   Eingabe-Pattern:
      - @translatebot übersetze ins {SPRACHE}: {TEXT}
      - @translatebot nach {SPRACHE}: {TEXT}
      - {SRC}-> {DEST}: {TEXT}
      - {SRC}> {DEST}: {TEXT}
      - {SRC}-{DEST}: {TEXT}
      - {SRC}→{DEST}: {TEXT}

   Ausgabe-Format:
      ```
      Übersetzung ({source_lang} → {target_lang}):
      {translated_text}
      ```
      
API-Integration:
- Endpoint: https://api-free.deepl.com/v2/translate
- Method: POST
- Parameter:
  - text={TEXT}
  - target_lang={LANG} (optional: source_lang)
- Authentifizierung: API-Key im HTTP-Header  
  `Authorization: DeepL-Auth-Key <API-KEY>`
- Antwort: JSON → `{ "translations":[{"detected_source_language":"EN","text":"..."}] }`
- Limit: 500.000 Zeichen / Monat (Free Tier)

---
