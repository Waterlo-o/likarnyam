package com.example.likarnyam.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FxUtils {

    public static void navigate(Node node, String fxmlPath, int width, int height) {
        try {
            Parent root = FXMLLoader.load(
                    FxUtils.class.getResource(fxmlPath)
            );
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigateFullscreen(Node node, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    FxUtils.class.getResource(fxmlPath)
            );
            Stage stage = (Stage) node.getScene().getWindow();
            // Сохраняем текущий размер
            double width = stage.getWidth();
            double height = stage.getHeight();
            stage.getScene().setRoot(root);
            // Восстанавливаем размер
            stage.setWidth(width);
            stage.setHeight(height);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Navigation error: " + e.getMessage());
        }
    }

    // Показать loading спиннер в контейнере
    public static void showLoading(VBox container, String message) {
        container.getChildren().clear();

        VBox loadingBox = new VBox(12);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.setPrefHeight(200);

        // Анимированный спиннер через CSS
        Label spinner = new Label("⟳");
        spinner.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-text-fill: #64B5F6;"
        );

        // Анимация вращения
        javafx.animation.RotateTransition rotate =
                new javafx.animation.RotateTransition(
                        javafx.util.Duration.seconds(1), spinner
                );
        rotate.setByAngle(360);
        rotate.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();

        Label loadingLabel = new Label(message);
        loadingLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #a0aec0;"
        );

        loadingBox.getChildren().addAll(spinner, loadingLabel);
        container.getChildren().add(loadingBox);
    }

    // Показать пустое состояние
    public static void showEmpty(VBox container, String message) {
        container.getChildren().clear();

        VBox emptyBox = new VBox(8);
        emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
        emptyBox.setPrefHeight(150);

        Label icon = new Label("○");
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #cbd5e0;");

        Label label = new Label(message);
        label.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #a0aec0;"
        );

        emptyBox.getChildren().addAll(icon, label);
        container.getChildren().add(emptyBox);
    }
}