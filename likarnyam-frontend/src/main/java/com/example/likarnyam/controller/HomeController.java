package com.example.likarnyam.controller;

import com.example.likarnyam.client.DoctorApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class HomeController {

    @FXML private TextField searchField;
    @FXML private Label totalVisitsLabel;
    @FXML private Label doctorNameLabel;
    @FXML private Text greetingText;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                JsonNode doctor = DoctorApiClient.getMe();

                String firstName = doctor.get("firstName").asText();
                String lastName = doctor.get("lastName").asText();

                Platform.runLater(() -> {
                    if (doctorNameLabel != null) {
                        doctorNameLabel.setText("Dr. " + lastName);
                    }
                    if (greetingText != null) {
                        greetingText.setText("Dr. " + firstName + "!");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}