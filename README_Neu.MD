# Software- Dokumentation SMA-CHATBOT-SYSTEM

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
5. Änderungslog

---

## 1. Einleitung
Das SMA-Chatbot-System ist ein modular aufgebautes erweiterbares Multi-Agents-System, welches als Laufzeitumgebung für verschiedene Chatbots dient. Der vorliegende Prototyp demonstriert die grundlegenden Funktionen eines Bot Basissystems mit der Fähigkeit neue Chatbots hinzuzufügen, einer Benutzerverwaltung und einer persistenten Speicherung des Chatverlaufs.

Hauptziele:  
- Entwicklung eines lauffähigen Prototyps 
- Einfache Integration neuer Bots  
- Konsolengestützte Bedienung (TUI)  
- Mehrbenutzerfähigkeit mit Login
- Persistenz von Chatverläufen in einer Datenbank  
- Demonstration anhand von drei Bots: Wetter, Wiki, Übersetzer  

---

## 2. Übersicht über die Struktur der Dokumentation

Die Dokumentation gliedert sich in zwei Hauptteile:  

- **Systemdokumentation**:  
  Darstellung des Basissystems, seiner Komponenten und Architektur.  
- **Bot-Dokumentation**:  
  Eigene Kapitel für die drei implementierten Bots mit Aufbau, Schnittstellen und Laufzeitsicht.  

---

## 3. Systemdokumentation

### 3.1 Einleitung
Die Systemdokumentation liefert eine detaillierte Sicht auf die Architektur und die Interaktionen der Hauptkomponenten.  
Sie dient als Referenz für Entwickler, die das System warten oder erweitern möchten.

---

### 3.2 Kontextabgrenzung

Das System interagiert mit unterschiedlichen Akteuren und externen Systemen:

Akteur/System           Eingabe                         Ausgabe                     

Benutzer                Textbefehle, Bot-Anfragen       Chatantworten, Systemmeldungen
OpenWeatherMapAPI       HTTP-Anfragen                   Wetterdaten (JSON)
SQLite Datenbank        SQL-Befehle                     Chatverlauf

*Platzhalter Abbildung: Fachliches Kontextdiagramm*

**Technischer Kontext**:  
- Implementierung in **Java 17+**  
- Kommunikation über REST-APIs (HTTP, JSON)  
- Persistenz über SQLite  

*Platzhalter Abbildung: UML-Deployment-Diagramm*

---

### 3.3 Lösungsstrategie

- **Programmiersprache**: Java, wegen Plattformunabhängigkeit und guter Bibliotheken.  
- **Architekturprinzipien**: lose Kopplung, Modularität, klare Schnittstellen.  
- **Schnittstellenstandard**: Bots implementieren ein gemeinsames Interface (`IBot`).  
- **Erweiterbarkeit**: neue Bots können ohne Änderung des Kerns hinzugefügt werden.  
- **Trennung Fachlichkeit/Technik**: Konsolen-Eingabe getrennt von Logik.  

---

### 3.4 Bausteinsicht (Wahl treffen was besser ist)

**Kernkomponenten**:  
- `ChatSystem`: zentrale Steuerung  
- `UserManager`: Login, Benutzerhandling  
- `BotManager`: Verwaltung und Aktivierung von Bots  
- `PersistenceManager`: Schnittstelle zur Datenbank  

**Bots**:  
- `WetterBot`  
- `WikiBot`  
- `TranslatorBot`  

*Platzhalter Abbildung: UML-Komponentendiagramm*

---

### 3.5 Laufzeitsichten

**Login-Szenario**:  
1. Benutzer startet System  
2. Login-Aufforderung → Eingabe Benutzername/Passwort  
3. Abgleich mit DB  
4. Anzeige des Chatverlaufs  

**Bot-Szenario**:  
1. Benutzer schreibt `@wetterbot wie ist das Wetter in Berlin?`  
2. `ChatSystem` erkennt Tag  
3. `BotManager` ruft `WetterBot` auf  
4. `WeatherApiClient` → API-Abfrage → Antwort  

*Platzhalter Abbildung: Sequenzdiagramm*

---

### 3.6 Infrastruktursicht (angucken!!!)

- **Entwicklungsumgebung**: macOS, JDK 17–24  
- **Build-System**: Maven/Gradle  
- **Versionsverwaltung**: GitHub  
- **DB**: SQLite (lokale Datei, austauschbar)  

*Platzhalter Abbildung: Deployment-Diagramm*

---

### 3.7 Querschnittliche Konzepte

- **Sicherheit**: Einfaches Login, keine Verschlüsselung  
- **Persistenz**: DAO-Pattern, austauschbar  
- **Texterkennung**: Regex-Pattern  
- **Designprinzipien**: SOLID, lose Kopplung  

---

### 3.8 Schnittstellen

| Komponente       | Schnittstelle | Technologie |
|------------------|---------------|-------------|
| Wetter-Bot       | REST API      | OpenWeatherMap |
| Wiki-Bot         | REST API      | Wikipedia REST |
| Übersetzer-Bot   | REST API      | Deepl |
| Datenbank        | SQLite        | JDBC |

---

### 3.9 Risiken & Technische Schulden

- Harte Kopplung an SQLite im MVP  
- Keine Rollenverwaltung für Benutzer  
- Nur CLI, keine GUI vorhanden  
- Performance bei sehr großen Chatlogs ungetestet  

---

### 3.10 Erweiterungen

Neue Bots:  
- Implementieren `IBot`  
- Registrierung in `BotManager`  
- Nutzung eigener API-Clients  

Beispiel:
```java
public class MyNewBot implements IBot {
    @Override
    public String process(String input) {
        return "Antwort meines Bots";
    }
}

---

### 3.11 Fehlerbehebung

- **API-Key fehlt** → In der Datei `config.properties` prüfen, ob die erforderlichen Schlüssel (z. B. für OpenWeatherMap oder Deepl) korrekt hinterlegt sind.  
- **DB locked** → Die Datenbankdatei ist gesperrt. Anwendung beenden und sicherstellen, dass keine parallelen Prozesse auf die SQLite-Datei zugreifen.  
- **Bot nicht gefunden** → Prüfen, ob der Bot korrekt im `BotManager` registriert wurde und ob die Namenskonventionen eingehalten sind.  
- **Fehlerhafte API-Antwort** → Netzwerkverbindung prüfen, ggf. API-Key neu generieren.  

---

### 3.12 Installationsanleitung (ansehen gemeinsam)

1. Repository klonen:  
   `git clone https://github.com/LeMonke94/SMA-Chatbot-System.git`  
   `cd SMA-Chatbot-System`  

2. Build mit Maven ausführen:  
   `mvn clean package`  

3. Starten der Anwendung:  
   `java -jar target/sma.jar`  

**Systemvoraussetzungen:**  
- JDK 17 oder höher  
- Internetzugang für API-Aufrufe  
- Optional: Git für Versionsverwaltung  

---

### 3.13 Konfiguration

Die Konfiguration erfolgt über die Datei `config.properties`.  
Folgende Parameter können angepasst werden:

- `weather.api.key` → API-Key für OpenWeatherMap  
- `deepl.api.key` → API-Key für Deepl  
- `db.path` → Pfad zur SQLite-Datenbankdatei  
- `language.default` → Standardsprache des Systems  

Beispiel:
weather.api.key=XXXXXXXXXXXXXX
deepl.api.key=YYYYYYYYYYYYYY
db.path=./chatbot.db
language.default=DE

---

## 4. Bot-Dokumentation

### 4.1 Wetter-Bot

**Funktion:**  
Der Wetter-Bot liefert aktuelle Wetterdaten und Vorhersagen für eine angegebene Stadt.  

**Datenquelle:**  
- OpenWeatherMap REST API  

**Struktur:**  
- Klasse `WetterBot`  
- Klasse `WeatherApiClient` (Kommunikation mit API)  

**Ablauf (Laufzeitsicht):**  
1. Benutzer fragt: `@wetterbot wie ist das Wetter in Bielefeld?`  
2. `ChatSystem` leitet die Anfrage an den `WetterBot` weiter  
3. `WeatherApiClient` ruft die OpenWeatherMap API auf  
4. Antwort wird formatiert und ausgegeben  

*Platzhalter Abbildung: Sequenzdiagramm WetterBot*

---

### 4.2 Wiki-Bot

**Funktion:**  
Der Wiki-Bot beantwortet Fragen mit Kurzinfos aus Wikipedia.  

**Datenquelle:**  
- Wikipedia REST API  

**Struktur:**  
- Klasse `WikiBot`  
- Klasse `WikipediaApiClient`  

**Ablauf (Laufzeitsicht):**  
1. Benutzer fragt: `@wiki Was ist Bielefeld?`  
2. `WikiBot` ruft die Wikipedia-REST-API auf  
3. Die Antwort wird in Kurzform aufbereitet und ausgegeben  

*Platzhalter Abbildung: Sequenzdiagramm WikiBot*

---

### 4.3 Übersetzer-Bot

**Funktion:**  
Der Übersetzer-Bot übersetzt Texte zwischen verschiedenen Sprachen.  

**Datenquelle:**  
- Deepl REST API  

**Struktur:**  
- Klasse `TranslatorBot`  
- Klasse `TranslatorApiClient`  

**Ablauf (Laufzeitsicht):**  
1. Benutzer gibt ein: `@translatebot übersetze ins Deutsche: A bird in the hand...`  
2. `TranslatorBot` erkennt Ziel- und Quellsprache  
3. `TranslatorApiClient` sendet Anfrage an Deepl  
4. Ergebnis wird zurückgegeben und ausgegeben  

*Platzhalter Abbildung: Sequenzdiagramm ÜbersetzerBot*

---
