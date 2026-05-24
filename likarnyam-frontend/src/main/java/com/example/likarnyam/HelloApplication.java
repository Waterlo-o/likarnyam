package com.example.likarnyam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Вычисляем размер окна один раз для всего приложения
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1200, screenBounds.getWidth() - 200);
        double height = Math.min(800, screenBounds.getHeight() - 150);

        // Загружаем интерфейс (FXML сам вызовет LoadingController)
        Parent splash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/loading.fxml")));

        primaryStage.setTitle("Likarnyam — Medical Portal");
        primaryStage.setScene(new Scene(splash, width, height));
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}