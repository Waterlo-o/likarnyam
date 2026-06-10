package com.example.likarnyam.util;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.example.likarnyam.session.UserSession;

public class FxUtils {

    public static boolean isDarkMode = false;

    public static void applyTheme(Parent root) {
        if (isDarkMode) {
            if (!root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }

    public static void navigate(Node node, String fxmlPath, int width, int height) {
        try {
            Parent root = FXMLLoader.load(FxUtils.class.getResource(fxmlPath));
            applyTheme(root);

            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleAuthExpired(Node anyNode) {
        Platform.runLater(() -> {
            UserSession.getInstance().logout();
            Stage stage = (Stage) anyNode.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            try {
                Parent root = FXMLLoader.load(FxUtils.class.getResource("/fxml/login.fxml"));
                applyTheme(root);
                stage.setScene(new Scene(root, width, height));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void navigateFullscreen(Node node, String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(FxUtils.class.getResource(fxmlPath));
            applyTheme(newRoot);

            Stage stage = (Stage) node.getScene().getWindow();
            Parent oldRoot = stage.getScene().getRoot();

            double width = stage.getWidth();
            double height = stage.getHeight();

            boolean animationsEnabled = UserSession.getInstance().isAnimationsEnabled();

            Node oldContent = oldRoot.lookup(".main-content");
            Node newContent = newRoot.lookup(".main-content");

            if (animationsEnabled && oldContent != null && newContent != null) {
                javafx.animation.TranslateTransition slideOut =
                        new javafx.animation.TranslateTransition(
                                javafx.util.Duration.millis(300), oldContent);
                slideOut.setByX(width);
                slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);

                slideOut.setOnFinished(e -> {
                    stage.getScene().setRoot(newRoot);
                    stage.setWidth(width);
                    stage.setHeight(height);

// Восстанавливаем перетаскивание
                    javafx.scene.Scene scene = stage.getScene();
                    final double[] offset = {-1, 0};
                    scene.setOnMousePressed(ev -> {
                        if (ev.getSceneY() < 40) {
                            offset[0] = ev.getScreenX() - stage.getX();
                            offset[1] = ev.getScreenY() - stage.getY();
                        } else {
                            offset[0] = -1;
                        }
                    });
                    scene.setOnMouseDragged(ev -> {
                        if (offset[0] >= 0 && ev.getSceneY() < 40) {
                            stage.setX(ev.getScreenX() - offset[0]);
                            stage.setY(ev.getScreenY() - offset[1]);
                        }
                    });

                    newContent.setTranslateX(width);
                    javafx.animation.TranslateTransition slideIn =
                            new javafx.animation.TranslateTransition(
                                    javafx.util.Duration.millis(300), newContent);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                    slideIn.play();
                });
                slideOut.play();
            } else {
                stage.getScene().setRoot(newRoot);
                stage.setWidth(width);
                stage.setHeight(height);

// Восстанавливаем перетаскивание
                javafx.scene.Scene scene = stage.getScene();
                final double[] offset = {-1, 0};
                scene.setOnMousePressed(ev -> {
                    if (ev.getSceneY() < 40) {
                        offset[0] = ev.getScreenX() - stage.getX();
                        offset[1] = ev.getScreenY() - stage.getY();
                    } else {
                        offset[0] = -1;
                    }
                });
                scene.setOnMouseDragged(ev -> {
                    if (offset[0] >= 0 && ev.getSceneY() < 40) {
                        stage.setX(ev.getScreenX() - offset[0]);
                        stage.setY(ev.getScreenY() - offset[1]);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Navigation error: " + e.getMessage());
        }
    }
    public static void showLoading(VBox container, String message) {
        container.getChildren().clear();
        VBox loadingBox = new VBox(12);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.setPrefHeight(200);

        Label spinner = new Label("⟳");
        spinner.setStyle("-fx-font-size: 32px; -fx-text-fill: #64B5F6;");

        javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(Duration.seconds(1), spinner);
        rotate.setByAngle(360);
        rotate.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        Label loadingLabel = new Label(message);
        loadingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0aec0;");

        loadingBox.getChildren().addAll(spinner, loadingLabel);
        container.getChildren().add(loadingBox);
    }

    public static void showEmpty(VBox container, String message) {
        container.getChildren().clear();
        VBox emptyBox = new VBox(8);
        emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
        emptyBox.setPrefHeight(150);

        Label icon = new Label("○");
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #cbd5e0;");

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0aec0;");

        emptyBox.getChildren().addAll(icon, label);
        container.getChildren().add(emptyBox);
    }
    public static String formatTime(LocalTime time) {
        if (time == null) return "";

        boolean is12Hour = "12h".equals(UserSession.getInstance().getTimeFormat());

        if (is12Hour) {
            return time.format(DateTimeFormatter.ofPattern("hh:mm a")); //  02:30 PM
        } else {
            return time.format(DateTimeFormatter.ofPattern("HH:mm")); //  14:30
        }
    }


    public static String formatTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return "";

        try {
            // 1. Убираем дату, если есть
            String rawTime = timeStr.contains("T") ? timeStr.split("T")[1] : timeStr;

            // 2. Берем только первые 5 символов (HH:mm), чтобы избавиться от секунд
            if (rawTime.length() > 5) {
                rawTime = rawTime.substring(0, 5); // Делает "14:30" из "14:30:00"
            }

            java.time.LocalTime time = java.time.LocalTime.parse(rawTime);

            // 3. Проверяем настройки пользователя
            boolean is12Hour = "12h".equals(com.example.likarnyam.session.UserSession.getInstance().getTimeFormat());

            return is12Hour ? time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                    : time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            // Если что-то не так, возвращаем хотя бы "HH:mm"
            return timeStr.substring(0, 5);
        }
    }
}