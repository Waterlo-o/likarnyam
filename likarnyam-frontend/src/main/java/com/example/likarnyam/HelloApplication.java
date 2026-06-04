package com.example.likarnyam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class HelloApplication extends Application {

    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width  = Math.min(1200, screenBounds.getWidth() - 200);
        double height = Math.min(800, screenBounds.getHeight() - 150);

        Parent splash = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/fxml/loading.fxml")));

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Likarnyam — Medical Portal");

        Scene scene = new Scene(splash, width, height);

        // Перетаскивание окна за верхнюю зону
        scene.setOnMousePressed(e -> {
            if (e.getSceneY() < 40) {
                dragOffsetX = e.getScreenX() - primaryStage.getX();
                dragOffsetY = e.getScreenY() - primaryStage.getY();
            } else {
                dragOffsetX = -1;
            }
        });
        scene.setOnMouseDragged(e -> {
            if (dragOffsetX >= 0 && e.getSceneY() < 40) {
                primaryStage.setX(e.getScreenX() - dragOffsetX);
                primaryStage.setY(e.getScreenY() - dragOffsetY);
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}