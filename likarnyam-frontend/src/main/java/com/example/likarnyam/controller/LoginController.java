package com.example.likarnyam.controller;

import com.example.likarnyam.client.ApiClient;
import com.example.likarnyam.session.UserSession;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    // --- Элементы формы ---
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckbox;

    // --- Элементы для анимации (из нового FXML) ---
    @FXML private HBox loginCard;
    @FXML private Circle bgCircle1;
    @FXML private Circle bgCircle2;

    @FXML
    public void initialize() {
        // Убеждаемся, что FXML подгрузил элементы, прежде чем анимировать
        if (loginCard != null && bgCircle1 != null && bgCircle2 != null) {

            // 1. Прячем карточку и сдвигаем её чуть вниз перед появлением
            loginCard.setOpacity(0);
            loginCard.setTranslateY(30);

            // 2. Создаем плавное появление (Fade In)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginCard);
            fadeIn.setToValue(1);

            // 3. Создаем выезд вверх
            TranslateTransition moveUp = new TranslateTransition(Duration.millis(800), loginCard);
            moveUp.setToY(0);

            // 4. Запускаем "парение" фоновых кругов
            animateCircle(bgCircle1, 20, 30, 4);
            animateCircle(bgCircle2, -15, -25, 5);

            // 5. Запускаем анимации появления карточки одновременно
            new ParallelTransition(fadeIn, moveUp).play();
        }
    }

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

    @FXML
    private void handleClose(ActionEvent event) {
        // Закрывает приложение при нажатии на крестик
        javafx.application.Platform.exit();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true); // Возвращаем место под текст ошибки
    }

    private void animateCircle(Circle c, double x, double y, double dur) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(dur), c);
        tt.setByX(x);
        tt.setByY(y);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/home.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.centerOnScreen(); // Центрируем после подгонки нового экрана
            stage.show();
        } catch (Exception e) {
            showError("Failed to load home screen");
            e.printStackTrace();
        }
    }
}