package io.github.youngmoney.bots;

public interface IBot {
    
    String getBotName();
    String getBotDescription();
    String processMessage(String input);

}
