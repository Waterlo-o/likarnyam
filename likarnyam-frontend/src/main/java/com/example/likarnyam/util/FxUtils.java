package com.example.likarnyam.util;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    public static void navigateFullscreen(Node node, String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(FxUtils.class.getResource(fxmlPath));
            applyTheme(newRoot);

            Stage stage = (Stage) node.getScene().getWindow();
            Parent oldRoot = stage.getScene().getRoot();

            double width = stage.getWidth();
            double height = stage.getHeight();

            // Ищем центральные карточки, чтобы анимировать только их
            Node oldContent = oldRoot.lookup(".main-content");
            Node newContent = newRoot.lookup(".main-content");

            if (oldContent != null && newContent != null) {
                // 1. Старая вкладка улетает направо (на ширину экрана)
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), oldContent);
                slideOut.setByX(width);
                slideOut.setInterpolator(Interpolator.EASE_IN);

                slideOut.setOnFinished(e -> {
                    // Подменяем экраны, когда старый улетел
                    stage.getScene().setRoot(newRoot);
                    stage.setWidth(width);
                    stage.setHeight(height);

                    // Прячем новую вкладку за правый край экрана
                    newContent.setTranslateX(width);

                    // 2. Новая вкладка вылетает справа на своё место
                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), newContent);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);
                    slideIn.play();
                });

                slideOut.play();
            } else {
                // Если вдруг класс .main-content не найден на каком-то экране (например, логин)
                stage.getScene().setRoot(newRoot);
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
}