package io.github.youngmoney.domain;

public class User {
    private final String username;

    //Konstruktor
    public User(String nameInput) {
        this.username = nameInput;
    }

    //Methoden
    public String getUsername() {
        return username;
    }
    
}
