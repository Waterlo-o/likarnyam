package com.example.likarnyam.controller;

import com.example.likarnyam.client.ApiClient;
import com.example.likarnyam.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckbox;

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
            String token = ApiClient.login(email, password);

            if (rememberMeCheckbox.isSelected()) {
                UserSession.getInstance().saveToken(token);
            } else {
                UserSession.getInstance().setJwtToken(token);
            }

            try {
                String tok = UserSession.getInstance().getJwtToken();
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/api/auth/me"))
                        .header("Authorization", "Bearer " + tok)
                        .GET().build();
                java.net.http.HttpResponse<String> resp = client.send(req,
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    com.fasterxml.jackson.databind.JsonNode info =
                            new com.fasterxml.jackson.databind.ObjectMapper()
                                    .readTree(resp.body());
                    UserSession.getInstance().setRole(
                            info.has("role") ? info.get("role").asText() : "DOCTOR");
                }
            } catch (Exception ignored) {
                UserSession.getInstance().setRole("DOCTOR");
            }

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