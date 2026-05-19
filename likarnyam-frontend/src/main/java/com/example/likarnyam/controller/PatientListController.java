package com.example.likarnyam.controller;

import com.example.likarnyam.client.PatientApiClient;
import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PatientListController {

    @FXML private TextField searchField;
    @FXML private VBox patientTableContainer;

    @FXML
    public void initialize() {
        loadPatients(null);
    }

    // Загрузка пациентов — если lastName null то все пациенты
    private void loadPatients(String lastName) {
        new Thread(() -> {
            try {
                JsonNode patients = lastName == null || lastName.isEmpty()
                        ? PatientApiClient.getAllPatients()
                        : PatientApiClient.searchByLastName(lastName);

                Platform.runLater(() -> {
                    patientTableContainer.getChildren().clear();
                    for (JsonNode patient : patients) {
                        HBox row = createPatientRow(patient);
                        patientTableContainer.getChildren().add(row);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Поиск при вводе текста
    @FXML
    private void handleSearch() {
        loadPatients(searchField.getText().trim());
    }

    // Создать строку таблицы
    private HBox createPatientRow(JsonNode patient) {
        String firstName = patient.get("firstName").asText();
        String lastName = patient.get("lastName").asText();
        String dob = patient.has("dateOfBirth") &&
                !patient.get("dateOfBirth").asText().equals("null")
                ? patient.get("dateOfBirth").asText() : "—";
        String gender = patient.has("gender") &&
                !patient.get("gender").asText().equals("null")
                ? patient.get("gender").asText() : "—";
        String phone = patient.has("phone") &&
                !patient.get("phone").asText().equals("null")
                ? patient.get("phone").asText() : "—";
        String bloodType = patient.has("bloodType") &&
                !patient.get("bloodType").asText().equals("null")
                ? patient.get("bloodType").asText() : "—";

        // Аватар с инициалами
        Label avatar = new Label(
                firstName.substring(0, 1) + lastName.substring(0, 1)
        );
        avatar.setStyle(
                "-fx-background-color: #d6e4ff;" +
                        "-fx-background-radius: 20;" +
                        "-fx-min-width: 36; -fx-min-height: 36;" +
                        "-fx-alignment: center;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4a90d9;"
        );

        Label nameLabel = new Label(firstName + " " + lastName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        HBox nameCell = new HBox(10, avatar, nameLabel);
        nameCell.setPrefWidth(250);
        nameCell.setAlignment(Pos.CENTER_LEFT);

        Label dobLabel = new Label(dob);
        dobLabel.setPrefWidth(150);

        Label genderLabel = new Label(gender);
        genderLabel.setPrefWidth(100);

        Label phoneLabel = new Label(phone);
        phoneLabel.setPrefWidth(180);

        Label bloodLabel = new Label(bloodType);
        bloodLabel.setPrefWidth(100);
        bloodLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e53e3e;");

        HBox row = new HBox(nameCell, dobLabel, genderLabel, phoneLabel, bloodLabel);
        row.getStyleClass().add("patient-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // Клик на пациента → карточка (TODO)
        row.setOnMouseClicked(e -> {
            try {
                Long id = patient.get("id").asLong();
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/patient-card.fxml")
                );
                Parent root = loader.load();
                PatientCardController controller = loader.getController();
                controller.setPatientId(id);
                Stage stage = (Stage) patientTableContainer.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return row;
    }

    // Навигация
    @FXML private void navigateHome() {
        FxUtils.navigateFullscreen(patientTableContainer, "/fxml/home.fxml");
    }
    @FXML private void navigatePatients() { }
    @FXML
    private void navigateSchedule() {
        FxUtils.navigateFullscreen(patientTableContainer, "/fxml/schedule.fxml");
    }
    @FXML
    private void navigateAppointments() {
        FxUtils.navigateFullscreen(patientTableContainer, "/fxml/appointments.fxml");
    }
    @FXML private void navigateSettings() {
        System.out.println("Settings — coming soon");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
    @FXML private void handleNewPatient() {
        System.out.println("New Patient — coming soon");
    }
}