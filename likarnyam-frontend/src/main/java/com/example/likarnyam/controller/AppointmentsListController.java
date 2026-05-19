package com.example.likarnyam.controller;

import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AppointmentsListController {

    @FXML private VBox appointmentsContainer;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;
    @FXML private Button filterAll;
    @FXML private Button filterScheduled;
    @FXML private Button filterCompleted;
    @FXML private Button filterCancelled;
    @FXML private Button filterNoShow;

    private String currentFilter = null;
    private JsonNode allAppointments = null;

    @FXML
    public void initialize() {
        loadAppointments(null);
    }

    private void loadAppointments(String status) {
        new Thread(() -> {
            try {
                JsonNode appointments = AppointmentApiClient.getAllAppointments(status);
                Platform.runLater(() -> {
                    allAppointments = appointments;
                    displayAppointments(appointments);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayAppointments(JsonNode appointments) {
        appointmentsContainer.getChildren().clear();
        String search = searchField.getText().trim().toLowerCase();

        int count = 0;
        for (JsonNode apt : appointments) {
            String firstName = apt.get("patientFirstName").asText();
            String lastName = apt.get("patientLastName").asText();
            String fullName = firstName + " " + lastName;

            if (!search.isEmpty() &&
                    !fullName.toLowerCase().contains(search)) continue;

            appointmentsContainer.getChildren().add(createRow(apt));
            count++;
        }

        totalLabel.setText(count + " appointments");
    }

    private HBox createRow(JsonNode apt) {
        String firstName = apt.get("patientFirstName").asText();
        String lastName = apt.get("patientLastName").asText();
        String aptAt = apt.get("appointmentAt").asText();
        String reason = apt.get("reason").asText();
        String status = apt.get("status").asText();
        String notes = apt.has("notes") &&
                !apt.get("notes").asText().equals("null")
                ? apt.get("notes").asText() : "—";

        String date = aptAt.substring(0, 10);
        String time = aptAt.substring(11, 16);

        // Аватар
        Label avatar = new Label(
                String.valueOf(firstName.charAt(0)) +
                        String.valueOf(lastName.charAt(0))
        );
        avatar.setStyle(
                "-fx-background-color: #d6e4ff;" +
                        "-fx-background-radius: 16;" +
                        "-fx-min-width: 32; -fx-min-height: 32;" +
                        "-fx-max-width: 32; -fx-max-height: 32;" +
                        "-fx-alignment: center;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-text-fill: #4a90d9;"
        );

        // Имя
        Label nameLabel = new Label(firstName + " " + lastName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        HBox nameCell = new HBox(8, avatar, nameLabel);
        nameCell.setPrefWidth(200);
        nameCell.setAlignment(Pos.CENTER_LEFT);

        // Дата
        Label dateLabel = new Label(date + "\n" + time);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        dateLabel.setPrefWidth(160);

        // Причина
        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        reasonLabel.setPrefWidth(200);
        reasonLabel.setWrapText(true);

        // Статус бейдж
        String badgeText = switch (status) {
            case "COMPLETED" -> "✓ Completed";
            case "CANCELLED" -> "✕ Cancelled";
            case "NO_SHOW" -> "? No Show";
            default -> "● Scheduled";
        };
        String badgeBg = switch (status) {
            case "COMPLETED" -> "#c6f6d5";
            case "CANCELLED" -> "#fed7d7";
            case "NO_SHOW" -> "#fefcbf";
            default -> "#bee3f8";
        };
        String badgeFg = switch (status) {
            case "COMPLETED" -> "#276749";
            case "CANCELLED" -> "#c53030";
            case "NO_SHOW" -> "#744210";
            default -> "#2b6cb0";
        };

        Label statusBadge = new Label(badgeText);
        statusBadge.setStyle(
                "-fx-background-color: " + badgeBg + ";" +
                        "-fx-text-fill: " + badgeFg + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 4 10 4 10;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        HBox statusCell = new HBox(statusBadge);
        statusCell.setPrefWidth(120);
        statusCell.setAlignment(Pos.CENTER_LEFT);

        // Заметки
        Label notesLabel = new Label(notes);
        notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
        notesLabel.setPrefWidth(200);
        notesLabel.setWrapText(true);

        HBox row = new HBox(nameCell, dateLabel, reasonLabel, statusCell, notesLabel);
        row.getStyleClass().add("patient-row");
        row.setAlignment(Pos.CENTER_LEFT);

        row.setOnMouseClicked(e -> System.out.println(
                "Clicked: " + firstName + " " + lastName
        ));

        return row;
    }

    // Фильтры
    private void setActiveFilter(Button active) {
        filterAll.getStyleClass().setAll("filter-btn");
        filterScheduled.getStyleClass().setAll("filter-btn");
        filterCompleted.getStyleClass().setAll("filter-btn");
        filterCancelled.getStyleClass().setAll("filter-btn");
        filterNoShow.getStyleClass().setAll("filter-btn");
        active.getStyleClass().setAll("filter-btn-active");
    }

    @FXML private void filterAll() {
        setActiveFilter(filterAll);
        currentFilter = null;
        loadAppointments(null);
    }

    @FXML private void filterScheduled() {
        setActiveFilter(filterScheduled);
        currentFilter = "SCHEDULED";
        loadAppointments("SCHEDULED");
    }

    @FXML private void filterCompleted() {
        setActiveFilter(filterCompleted);
        currentFilter = "COMPLETED";
        loadAppointments("COMPLETED");
    }

    @FXML private void filterCancelled() {
        setActiveFilter(filterCancelled);
        currentFilter = "CANCELLED";
        loadAppointments("CANCELLED");
    }

    @FXML private void filterNoShow() {
        setActiveFilter(filterNoShow);
        currentFilter = "NO_SHOW";
        loadAppointments("NO_SHOW");
    }

    @FXML private void handleSearch() {
        if (allAppointments != null) {
            displayAppointments(allAppointments);
        }
    }

    // Навигация
    @FXML private void navigateHome() {
        FxUtils.navigateFullscreen(appointmentsContainer, "/fxml/home.fxml");
    }
    @FXML private void navigatePatients() {
        FxUtils.navigateFullscreen(appointmentsContainer, "/fxml/patient-list.fxml");
    }
    @FXML private void navigateSchedule() {
        FxUtils.navigateFullscreen(appointmentsContainer, "/fxml/schedule.fxml");
    }
    @FXML private void navigateAppointments() { }
    @FXML private void navigateSettings() {
        System.out.println("Settings — coming soon");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}