package com.example.likarnyam;

import com.example.likarnyam.session.UserSession;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();
        double width = Math.min(1200, screenBounds.getWidth() - 200);
        double height = Math.min(800, screenBounds.getHeight() - 150);

        // Показываем splash screen
        Parent splash = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/fxml/loading.fxml"))
        );
        primaryStage.setTitle("Likarnyam — Medical Portal");
        primaryStage.setScene(new Scene(splash, width, height));
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();

        ProgressBar progressBar = (ProgressBar) splash.lookup("#progressBar");
        Label statusLabel = (Label) splash.lookup("#statusLabel");

        // В фоне ждём бэкенд и грузим данные
        new Thread(() -> {
            try {
                // Шаг 1 — ждём бэкенд
                Platform.runLater(() -> {
                    statusLabel.setText("Connecting to server...");
                    progressBar.setProgress(0.2);
                });

                waitForBackend(10);

                // Шаг 2 — проверяем токен
                Platform.runLater(() -> {
                    statusLabel.setText("Checking session...");
                    progressBar.setProgress(0.6);
                });

                Thread.sleep(300);

                boolean hasToken = UserSession.getInstance().loadSavedToken();

                // Шаг 3 — загружаем нужный экран
                Platform.runLater(() -> {
                    statusLabel.setText("Loading...");
                    progressBar.setProgress(1.0);
                });

                Thread.sleep(300);

                Platform.runLater(() -> {
                    try {
                        String fxmlPath = hasToken
                                ? "/fxml/home.fxml"
                                : "/fxml/login.fxml";

                        Parent root = FXMLLoader.load(
                                Objects.requireNonNull(
                                        getClass().getResource(fxmlPath)
                                )
                        );

                        primaryStage.getScene().setRoot(root);

                        if (!hasToken) {
                            primaryStage.setWidth(800);
                            primaryStage.setHeight(500);
                            primaryStage.centerOnScreen();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Failed to connect"));
                e.printStackTrace();
            }
        }).start();
    }

    private void waitForBackend(int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                        .GET()
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
                return; // бэкенд отвечает
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}