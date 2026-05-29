package com.example.likarnyam.controller;

import com.example.likarnyam.client.PatientApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PatientCardController {

    @FXML private Label patientNameLabel;
    @FXML private Label patientAvatarLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label allergiesLabel;
    @FXML private VBox historyContainer;

    private Long patientId;

    // Вызывается из PatientListController перед показом экрана
    public void setPatientId(Long id) {
        this.patientId = id;
        loadPatientData();
    }

    private void loadPatientData() {
        new Thread(() -> {
            try {
                JsonNode patient = PatientApiClient.getById(patientId);
                JsonNode history = PatientApiClient.getPatientHistory(patientId);

                Platform.runLater(() -> {
                    // Основные данные
                    String firstName = patient.get("firstName").asText();
                    String lastName = patient.get("lastName").asText();

                    patientNameLabel.setText(firstName + " " + lastName);
                    patientAvatarLabel.setText(
                            firstName.substring(0, 1) + lastName.substring(0, 1)
                    );

                    dobLabel.setText(getValue(patient, "dateOfBirth"));
                    genderLabel.setText(getValue(patient, "gender"));
                    bloodTypeLabel.setText(getValue(patient, "bloodType"));
                    phoneLabel.setText(getValue(patient, "phone"));
                    emailLabel.setText(getValue(patient, "email"));
                    allergiesLabel.setText(getValue(patient, "allergies"));

                    // История визитов
                    historyContainer.getChildren().clear();
                    if (history.size() == 0) {
                        Label empty = new Label("No visit history");
                        empty.getStyleClass().add("history-empty-label");
                        historyContainer.getChildren().add(empty);
                    } else {
                        for (JsonNode visit : history) {
                            historyContainer.getChildren().add(createHistoryCard(visit));
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createHistoryCard(JsonNode visit) {
        String date = visit.get("appointmentAt").asText().substring(0, 10);
        String time = visit.get("appointmentAt").asText().substring(11, 16);
        String reason = visit.get("reason").asText();
        String status = visit.get("status").asText();
        String notes = getValue(visit, "notes");

        // Статус с классом вместо setStyle
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().addAll(
                "history-status-badge",
                status.equals("COMPLETED") ? "history-status-completed" : "history-status-other"
        );

        // Шапка карточки
        Label dateLabel = new Label(date + " at " + time);
        dateLabel.getStyleClass().add("history-date-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, dateLabel, spacer, statusLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // Причина
        Label reasonLabel = new Label("Reason: " + reason);
        reasonLabel.getStyleClass().add("history-reason-label");

        // Заметки
        Label notesLabel = new Label("Notes: " + notes);
        notesLabel.getStyleClass().add("history-notes-label");
        notesLabel.setWrapText(true);

        VBox card = new VBox(8, header, reasonLabel, notesLabel);
        card.getStyleClass().add("history-card");

        return card;
    }

    // Вспомогательный метод — возвращает значение или "—"
    private String getValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).asText().equals("null")) {
            return node.get(field).asText();
        }
        return "—";
    }

    @FXML
    private void handleNewAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/new-appointment.fxml")
            );
            Parent root = loader.load();
            AppointmentController controller = loader.getController();
            controller.setPatient(patientId,
                    patientNameLabel.getText());

            FxUtils.applyTheme(root);

            javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setTitle("New Appointment");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void navigateHome() {
        FxUtils.navigateFullscreen(patientNameLabel, "/fxml/home.fxml");
    }
    @FXML private void navigatePatients() {
        FxUtils.navigateFullscreen(patientNameLabel, "/fxml/patient-list.fxml");
    }
    @FXML
    private void navigateSchedule() {
        FxUtils.navigateFullscreen(patientNameLabel, "/fxml/schedule.fxml");
    }
    @FXML
    private void navigateSettings() {
        FxUtils.navigateFullscreen(patientNameLabel, "/fxml/settings.fxml");
    }

    @FXML
    private void navigateAppointments() {
        FxUtils.navigateFullscreen(patientNameLabel, "/fxml/appointments.fxml");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}