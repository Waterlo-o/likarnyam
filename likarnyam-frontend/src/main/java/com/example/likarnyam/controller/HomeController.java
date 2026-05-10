package com.example.likarnyam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HomeController {

    @FXML
    private TextField searchField;

    @FXML
    private Label totalVisitsLabel;

    @FXML
    public void initialize() {
        // Метод вызывается автоматически после загрузки интерфейса
        totalVisitsLabel.setText("104");
    }

    @FXML
    public void handleSearch() {
        System.out.println("Searching for: " + searchField.getText());
    }
}