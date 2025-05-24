package com.flappybird;

import javafx.application.Application;
import javafx.stage.Stage;

public class FlappyBirdApp extends Application {
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void start(Stage primaryStage) {
        GameManager gameManager = new GameManager();
        gameManager.setUsername(username);  // We transfer the username
        gameManager.startGame(primaryStage);
    }
}