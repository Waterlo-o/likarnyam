package com.example.likarnyam.controller;

import com.example.likarnyam.session.UserSession;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoadingController implements Initializable {

    // Связываем элементы из FXML
    @FXML private StackPane rootPane;
    @FXML private SVGPath iconPath;
    @FXML private Rectangle progressFill;
    @FXML private Label statusLabel;

    private final double MAX_BAR_WIDTH = 300.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Запускаем стартовые анимации при появлении окна
        rootPane.setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), rootPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), iconPath);
        pulse.setByX(0.06);
        pulse.setByY(0.06);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // 2. Запускаем фоновую загрузку
        startInitializationProcess();
    }

    private void startInitializationProcess() {
        new Thread(() -> {
            try {
                // Шаг 1: Подключение к серверу
                animateTextChange("Connecting to server...");
                animateProgress(0.3);
                waitForBackend(10);

                // Шаг 2: Проверка сессии
                animateTextChange("Checking secure session...");
                animateProgress(0.7);
                Thread.sleep(600);

                boolean hasToken = UserSession.getInstance().loadSavedToken();

                // Шаг 3: Запуск приложения
                animateTextChange("Launching application...");
                animateProgress(1.0);
                Thread.sleep(700);

                // Загружаем следующий экран
                Platform.runLater(() -> loadNextScene(hasToken));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to connect to backend");
                    progressFill.setFill(javafx.scene.paint.Color.web("#FF5252"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadNextScene(boolean hasToken) {
        try {
            String fxmlPath = hasToken ? "/fxml/home.fxml" : "/fxml/login.fxml";
            Parent nextRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));

            // Плавно скрываем текущий экран (rootPane)
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rootPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Получаем текущее окно (Stage) и подменяем сцену
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.getScene().setRoot(nextRoot);

                // Плавно показываем новый экран
                nextRoot.setOpacity(0.0);
                FadeTransition fadeInRoot = new FadeTransition(Duration.millis(400), nextRoot);
                fadeInRoot.setFromValue(0.0);
                fadeInRoot.setToValue(1.0);
                fadeInRoot.play();
            });
            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateProgress(double percentage) {
        Platform.runLater(() -> {
            double targetWidth = MAX_BAR_WIDTH * percentage;
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(600),
                            new KeyValue(progressFill.widthProperty(), targetWidth, Interpolator.EASE_BOTH))
            );
            timeline.play();
        });
    }

    private void animateTextChange(String newText) {
        Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), statusLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                statusLabel.setText(newText);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), statusLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        });
    }

    private void waitForBackend(int maxRetries) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(2))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .GET()
                .build();

        for (int i = 0; i < maxRetries; i++) {
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
                return;
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) { break; }
            }
        }
    }
}