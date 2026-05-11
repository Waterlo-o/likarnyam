package com.example.likarnyam.controller;


import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.DoctorApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {

    @FXML private TextField searchField;
    @FXML private Label totalVisitsLabel;
    @FXML private Label doctorNameLabel;
    @FXML private Text greetingText;
    @FXML private VBox patientListContainer;
    @FXML private Label clockLabel;
    @FXML private Label consultationName;
    @FXML private Label consultationInfo;
    @FXML private Label consultationReason;
    @FXML private Label consultationNotes;
    @FXML private Label scheduledLabel;
    @FXML private Label completedLabel;

    @FXML
    public void initialize() {
        System.out.println("HomeController initialized!");
        new Thread(() -> {
            // Пробуем подключиться до 5 раз с паузой 2 секунды
            int maxRetries = 10;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    System.out.println("Attempt " + (i + 1) + " to connect...");
                    JsonNode doctor = DoctorApiClient.getMe();
                    JsonNode appointments = AppointmentApiClient.getTodayAppointments();

                    String firstName = doctor.get("firstName").asText();
                    String lastName = doctor.get("lastName").asText();
                    int totalVisits = appointments.size();

                    Platform.runLater(() -> {
                        if (doctorNameLabel != null)
                            doctorNameLabel.setText("Dr. " + lastName);
                        if (greetingText != null)
                            greetingText.setText("Dr. " + firstName + "!");
                        if (totalVisitsLabel != null)
                            totalVisitsLabel.setText(String.valueOf(totalVisits));

                        int scheduled = 0;
                        int completed = 0;
                        for (JsonNode appointment : appointments) {
                            String status = appointment.get("status").asText();
                            if (status.equals("SCHEDULED")) scheduled++;
                            if (status.equals("COMPLETED")) completed++;
                        }

                        // Обновляем лейблы
                        if (scheduledLabel != null)
                            scheduledLabel.setText(String.valueOf(scheduled));
                        if (completedLabel != null)
                            completedLabel.setText(String.valueOf(completed));

                        if (patientListContainer != null) {
                            patientListContainer.getChildren().clear();
                            for (JsonNode appointment : appointments) {
                                HBox item = createPatientListItem(appointment);
                                patientListContainer.getChildren().add(item);
                            }
                        }
                    });
                    break; // успешно — выходим из цикла

                } catch (Exception e) {
                    System.out.println("Attempt " + (i + 1) + " failed: " + e.getMessage());
                    if (e.getMessage() != null && e.getMessage().equals("TOKEN_EXPIRED")) {
                        // Токен просрочен — на логин
                        Platform.runLater(() -> {
                            UserSession.getInstance().logout();
                            try {
                                Parent root = FXMLLoader.load(
                                        getClass().getResource("/fxml/login.fxml")
                                );
                                Stage stage = (Stage) totalVisitsLabel.getScene().getWindow();
                                stage.setScene(new Scene(root, 800, 500));
                                stage.setResizable(false);
                                stage.show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        break;
                    }
                    // Другая ошибка — ждём 2 секунды и пробуем снова
                    try { Thread.sleep(3000); } catch (InterruptedException ie) { break; }
                }
            }
        }).start();
    }

    private HBox createPatientListItem(JsonNode appointment) {
        String firstName = appointment.get("patientFirstName").asText();
        String lastName = appointment.get("patientLastName").asText();
        String reason = appointment.get("reason").asText();
        String time = appointment.get("appointmentAt").asText().substring(11, 16);

        Circle avatar = new Circle(20);
        avatar.setStyle("-fx-fill: #d6e4ff;");

        Label nameLabel = new Label(firstName + " " + lastName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-text-fill: #888;");
        VBox info = new VBox(nameLabel, reasonLabel);

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("time-badge-blue");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox item = new HBox(15, avatar, info, spacer, timeLabel);
        item.getStyleClass().add("list-item");
        item.setPadding(new Insets(5, 10, 5, 10));
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Клик на пациента → показываем детали
        item.setOnMouseClicked(e -> showConsultation(appointment));
        item.setStyle("-fx-cursor: hand;");

        return item;
    }
    private void showConsultation(JsonNode appointment) {
        String firstName = appointment.get("patientFirstName").asText();
        String lastName = appointment.get("patientLastName").asText();
        String reason = appointment.get("reason").asText();
        String time = appointment.get("appointmentAt").asText().substring(11, 16);
        String notes = appointment.has("notes") ? appointment.get("notes").asText() : "No notes";


        consultationName.setText(firstName + " " + lastName);
        consultationInfo.setText("Appointment at " + time);
        consultationReason.setText(reason);
        consultationNotes.setText(notes);
    }
}