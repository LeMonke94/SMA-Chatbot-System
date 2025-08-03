package io.github.youngmoney.domain;

public class BotInfo {
    private final String name;
    private final String description;
    private final boolean isActive;

    //Konstruktor
    public BotInfo(String nameInput, String descriptionInput, boolean isActiveInput) {
        this.name = nameInput;
        this.description = descriptionInput;
        this.isActive = isActiveInput;
    }

    //Methoden
    @Override
    public String toString() {
        return String.format("%s (%s)\n %s", name, isActive ? "aktiv." : "nicht aktiv.", description);
    }

}
