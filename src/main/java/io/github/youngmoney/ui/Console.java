package io.github.youngmoney.ui;

import io.github.youngmoney.application.ChatSystem;
import java.util.Scanner;

public class Console {
    private final ChatSystem chatSystem;
    private final Scanner scanner;

    //Konstruktor
    public Console(ChatSystem chatSystem) {
        this.chatSystem = chatSystem;
        this.scanner = new Scanner(System.in);
    }

    //Methoden
    public void startConsole() {
        print("System: Hallo lieber Benutzer.");
        boolean loggedIn = handleLogin();
        if (loggedIn) {
            runChatLoop();
        }
        print("System: Programm wird beendet.");
        scanner.close();
    }

    private boolean handleLogin() {
        while (true) {
            print("System: Bitte den Benutzernamen eingeben (oder 'exit' um das Programm zu beenden):");
            print("> ");
            String username = scanner.nextLine();
            if ("exit".equalsIgnoreCase(username)) {
                return false;
            }
            print("System: Bitte das Passwort eingeben:");
            print("> ");
            String password = scanner.nextLine();
            String message = chatSystem.loginAttempt(username, password);
            if (message != null) {
                print(message);
                return true;
            } else {
                print("Error: Benutzername oder Passwort falsch, versuche es nochmal.\n");
            }
        }
    }

    private void runChatLoop() {
        while (true) {
            print("Deine Eingabe: ");
            String input = scanner.nextLine();
            String response = chatSystem.processInput(input);
            if ("exit".equalsIgnoreCase(response)) {
                break;
            }
            print("\nSystem:\n" + response);
        }
    }

    public void print(String text) {
        System.out.println(text);
    }

}

