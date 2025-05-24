package com.flappybird;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LoginRegisterController {

    private final File userFile = new File("users.txt");
    private Map<String, String> users = new HashMap<>();

    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private TextField registerNameField;
    @FXML
    private TextField registerSurnameField;
    @FXML
    private TextField registerUsernameField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private PasswordField registerConfirmPasswordField;
    @FXML
    private Button registerButton;

//FXML offers an alternative to designing user interfaces using procedural code.
    @FXML
    private void initialize() {
        loadUsersFromFile();
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
    }

    private void loadUsersFromFile() {
        if (!userFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ";" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin() {
        String username = loginUsernameField.getText();
        String password = loginPasswordField.getText();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            try {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                GameManager gameManager = new GameManager();
                gameManager.setUsername(username); // To transmit the username to the game screen
                gameManager.startGame(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error ");
            alert.setHeaderText(null);
            alert.setContentText("Username or password is incorrect! ");
            alert.showAndWait();
        }
    }

    private void handleRegister() {
        String username = registerUsernameField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText("Passwords don't match!");
            alert.showAndWait();
            return;
        }

        if (users.containsKey(username)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText("This username is already taken!");
            alert.showAndWait();
            return;
        }

        users.put(username, password);
        saveUsersToFile();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Registration is complete, you can login now.");
        alert.showAndWait();

        // Clear form
        registerNameField.clear();
        registerSurnameField.clear();
        registerUsernameField.clear();
        registerPasswordField.clear();
        registerConfirmPasswordField.clear();
    }
}