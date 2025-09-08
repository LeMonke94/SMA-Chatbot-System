# SMA-CHATBOT-SYSTEM

## Inhaltsverzeichnis
1. Einleitung  
2. Systemübersicht & Architektur  
3. Komponenten- und Schnittstellendokumentation  
4. Datenmodell & Persistenz  
5. Betrieb & Installation  
6. Konfiguration (API-Keys, DB)  
7. Nutzung / Beispiele  
8. Eigene Bots hinzufügen  
9. Troubleshooting  

## 1. Einleitung
Das SMA-Chatbot-System ist ein modular aufgebautes erweiterbares Multi-Agents-System, welches als Laufzeitumgebung für verschiedene Chatbots dient. Der vorliegende Prototyp demonstriert die grundlegenden Funktionen eines Bot Basissystems mit der Fähigkeit neue Chatbots hinzuzufügen, einer Benutzerverwaltung und einer persistenten Speicherung des Chatverlaufs.

### 1.1 Projektziele
    - Entwicklung einer flexiblen Chatbot-Plattform
    - Umsetzung folgender Bots: Wetter-Bot, Wiki-Bot, Übersetzer-Bot
    - Bedienung des Systems durch eine TUI
    - Mehrbenutzerfähigkeit mit Login
    - Persistente Speicherung von Chatverläufen
    - Einfache Integration neuer Bots durch standartisierte Schnittstellen


## 2. Systemübersicht & Architektur 

### 2.1 Kontextabgrenzung

#### 2.1.1 Fachlicher Kontext:
Das System interagiert mit unterschiedlichen Akteuren und externen Systemen:

Akteur/System           Eingabe                         Ausgabe

Benutzer                Textbefehle, Bot-Anfragen       Chatantworten, Systemmeldungen
OpenWeatherMapAPI       HTTP-Anfragen                   Wetterdaten (JSON)
SQLite Datenbank        SQL-Befehle                     Chatverlauf

#### 2.1.2 Technischer Kontext:
Hier kommt ein Ablaufdiagramm

### 2.2 Architektur
Schichtenarchitektur: Klare Trennung in Application, Bots, Domain, Infrastructure und UI Layer. 
Dependency Injection: Lose Kopplung durch Constructor Injection
SQLite: Leichtgewichtige eingebettete Datenbank
Interface-basierte Bot-Integration: Alle Bots implementieren das IBot Interface
Command Pattern: Bots werden über @botname Syntax angesprochen

Bausteinsicht:
┌─────────────────────────────────────────────────────────┐
│                    SMA-Chatbot-System                   │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │
│ │     UI      │  │ Application │  │ Infrastructure  │   │
│ │   Layer     │  │    Layer    │  │     Layer       │   │
│ └─────────────┘  └─────────────┘  └─────────────────┘   │
│                  ┌─────────────┐                        │
│                  │   Domain    │                        │
│                  │    Layer    │                        │
│                  └─────────────┘                        │
└─────────────────────────────────────────────────────────┘

Package-Struktur:
io.github.youngmoney/
├── App.java                            # Haupteinstiegspunkt
├── application/                        # Kernlogik
│   ├── BotManager.java                     # Bot-Lifecycle Management
│   ├── ChatSystem.java                     # Orchestrierung
│   └── UserManager.java                    # Benutzer-Authentifizierung
├── bots/                               # Bot-Implementierungen
│   ├── IBot.java                           # Bot-Interface
│   ├── WetterBot.java                      # Wetter-Bot
│   └── WikiBot.java                        # Wiki-Bot
├── domain/                             # Domänenobjekte
│   ├── BotInfo.java                        # Bot-Metadaten
│   ├── ChatMessage.java                    # Nachrichtenmodell
│   └── User.java                           # Benutzermodell
├── infrastructure/                     # Externe Systeme
│   ├── adapter/                            # API-Anbindungen
│   │   ├── WeatherApiClient.java               # WetterBot-API
│   │   └── WikipediaApiClient.java             # WikiBot-API
│   └── persistence/                    # Persistenz
│       └── PersistenceManager.java         # SQLite-DB
└── ui/                                 # Benutzeroberfläche
    └── Console.java                        # TUI

## 3. Komponenten- und Schnittstellendokumentation

Application Layer:
    - BotManager:
        Registrierung, Aktiv-/Inaktiv-Schaltung der Bots. Routing der eingehenden Nachrichten an passende Bots, Verwaltung von Bot-Metadaten.
    ChatSystem:
        Koordiniert Input von UI, User-Verwaltung, Routing an BotManager und Persistenz.
    UserManager:
        Login durch Passwort-Verifikation.


