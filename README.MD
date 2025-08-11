# Anforderungen Checkliste:

#   2. Eigene Bots
        2.1 Wetter-Bot
            2.1.1 Aktuelles Wetter                  ✅ WeatherApiClient.java
            2.1.2 Vorhersage (5 Tage)               ✅ WeatherApiClient.java
            2.1.3 Datenquelle                       ✅ WeatherApiClient.java
        2.2 Wiki-Bot                                
            2.2.1 Informationen zu einem Begriff    ❌
            2.2.2 Datenquelle                       ❌
        2.3 Übersetzer-Bot
            2.3.1 Beispiel                          ❌
            2.3.2 Datenquelle                       ❌
#   3. Funktionale Anforderungen
        3.1 Bedienung/TUI/GUI
            3.1.1 Variante A                        ✅ BotManager.java
            3.1.2 Variante B
            3.1.3 Variante C
            3.1.4 Variante D
        3.2 Benutzer
            3.2.1 Benutzer                          ✅ UserManager.java
            3.2.2 Login                             ✅ ChatSystem.java
            3.2.3 Benutzerverwaltung                ✅ UserManager.java
        3.3 Persistenz
            3.3.1 Datenbank                         ✅ PersistenceManager.java
            3.3.2 Chatverlauf                       ✅ PersistenceManager.java
        3.4 Hinzufügen von Chatbots
            3.4.1 Initiales Laden                   ✅ IBot.java
            3.4.2 Aktivieren von Bots               ✅ BotManager.java
#   4. Nicht-Funktionale Anforderungen
        4.1 Programmiersprache                      ✅ Java
        4.2 Sprachverständlichkeit                  ✅ Pattern-Basiert (Wetterbot)
        4.3 Modularität                             ✅ application|bots|domain|infrastructure|persistence|ui
        4.4 Skalierbarkeit                          ✅ application|bots|domain|infrastructure|persistence|ui
        
#   Dokumentation

