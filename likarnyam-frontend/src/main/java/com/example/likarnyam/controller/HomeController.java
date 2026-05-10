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

    @FXML
    public void initialize() {
        System.out.println("HomeController initialized!");
        new Thread(() -> {
            System.out.println("Thread started!");
            try {
                System.out.println("Fetching doctor...");
                JsonNode doctor = DoctorApiClient.getMe();
                System.out.println("Doctor fetched: " + doctor.toString());
                JsonNode appointments = AppointmentApiClient.getTodayAppointments();
                System.out.println("Appointments fetched: " + appointments.size());

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

                    if (patientListContainer != null) {
                        patientListContainer.getChildren().clear();
                        for (JsonNode appointment : appointments) {
                            HBox item = createPatientListItem(appointment);
                            patientListContainer.getChildren().add(item);
                        }
                    }
                });

                javafx.animation.Timeline clock = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1),
                                e -> {
                                    String time = java.time.LocalTime.now()
                                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                                    clockLabel.setText(time);
                                }
                        )
                );
                clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
                clock.play();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error loading home data: " + e.getMessage());
                Platform.runLater(() -> {
                    // Если токен просрочен — возвращаем на логин
                    if (e.getMessage() != null && e.getMessage().contains("403")) {
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
                    }
                });
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

        return item;
    }
}