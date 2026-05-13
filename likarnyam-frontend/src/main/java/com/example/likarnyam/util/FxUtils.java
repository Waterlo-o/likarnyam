package com.example.likarnyam.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class FxUtils {

    // Переключить экран
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
            System.out.println("Navigation error: " + e.getMessage());
        }
    }

    // Переключить на полноэкранный режим (для основных экранов)
    public static void navigateFullscreen(Node node, String fxmlPath) {
        navigate(node, fxmlPath, 1200, 800);
    }
}