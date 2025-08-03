package io.github.youngmoney.application;

import io.github.youngmoney.domain.User;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private final Map<String, String> users = new HashMap<>();

    //Konstruktor
    public UserManager(){
        users.put("User", "Passwort");
        users.put("Martin", "123");
        users.put("Jordan", "999");
    }

    //Methoden
    public User login(String usernameInput, String passwordInput) {
        String password = users.get(usernameInput);
        if (password != null && password.equals(passwordInput)) {
            return new User(usernameInput);
        }
        return null;
    }

}



