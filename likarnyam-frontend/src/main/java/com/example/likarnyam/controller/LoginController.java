package com.example.likarnyam.controller;

import com.example.likarnyam.client.ApiClient;
import com.example.likarnyam.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Простая валидация — поля не должны быть пустыми
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        // Блокируем кнопку пока идёт запрос
        loginButton.setDisable(true);
        loginButton.setText("Signing in...");

        try {
            // Отправляем запрос на бэкенд
            String token = ApiClient.login(email, password);

            // Сохраняем токен в сессии
            UserSession.getInstance().setJwtToken(token);

            // Переходим на главный экран
            navigateToHome();

        } catch (Exception e) {
            showError("Invalid email or password");
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/home.fxml")  // ← убедись что путь именно такой
            );
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            showError("Failed to load home screen");
            e.printStackTrace();
        }
    }
}