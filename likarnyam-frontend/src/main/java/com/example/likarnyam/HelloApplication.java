package com.example.likarnyam;

import com.example.likarnyam.session.UserSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        if (UserSession.getInstance().loadSavedToken()) {
            // Токен есть — сразу на Home
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/fxml/home.fxml"))
            );
            primaryStage.setTitle("Likarnyam — Medical Portal");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.setResizable(true);
        } else {
            // Токена нет — экран логина
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/fxml/login.fxml"))
            );
            primaryStage.setTitle("Likarnyam — Medical Portal");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.setResizable(false);
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}