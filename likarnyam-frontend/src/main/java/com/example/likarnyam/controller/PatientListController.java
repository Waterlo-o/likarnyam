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
    public void loadPatients(String lastName) {
        // Показываем loading пока грузим
        Platform.runLater(() ->
                FxUtils.showLoading(patientTableContainer, "Loading patients...")
        );

        new Thread(() -> {
            try {
                JsonNode patients = lastName == null || lastName.isEmpty()
                        ? PatientApiClient.getAllPatients()
                        : PatientApiClient.searchByLastName(lastName);

                Platform.runLater(() -> {
                    patientTableContainer.getChildren().clear();
                    if (patients.size() == 0) {
                        FxUtils.showEmpty(patientTableContainer, "No patients found");
                        return;
                    }
                    for (JsonNode patient : patients) {
                        HBox row = createPatientRow(patient);
                        patientTableContainer.getChildren().add(row);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        FxUtils.showEmpty(patientTableContainer, "Failed to load patients")
                );
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
        Label avatar = new Label(firstName.substring(0, 1) + lastName.substring(0, 1));
        avatar.getStyleClass().add("patient-avatar");

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

        javafx.scene.control.Button editBtn = new javafx.scene.control.Button("Edit");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3182ce; -fx-cursor: hand; -fx-underline: true;");
        editBtn.setOnAction(e -> {
            e.consume();
            openEditDialog(patient);
        });

        HBox row = new HBox(nameCell, dobLabel, genderLabel, phoneLabel, bloodLabel);
        row.getStyleClass().add("patient-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // Клик на пациента → карточка (TODO)
        row.setOnMouseClicked(e -> {
            try {
                Long id = patient.get("id").asLong();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-card.fxml"));
                Parent root = loader.load();
                PatientCardController controller = loader.getController();
                controller.setPatientId(id);

                FxUtils.applyTheme(root);

                Stage stage = (Stage) patientTableContainer.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return row;
    }

    @FXML
    private void handleNewPatient() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/new-patient-dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            // Передаем ссылку на текущий контроллер, чтобы обновить таблицу после сохранения
            com.example.likarnyam.controller.NewPatientController dialogController = loader.getController();
            dialogController.setParentController(this);

            // Создаем новое всплывающее окно
            javafx.stage.Stage stage = new javafx.stage.Stage();

            // ВАЖНО: APPLICATION_MODAL блокирует главное окно, пока врач не заполнит форму
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            com.example.likarnyam.util.FxUtils.applyTheme(root);

            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            stage.setTitle("New Patient");

            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            // Подключаем наши стили к новому окну
            String css = getClass().getResource("/css/css.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setResizable(false);

            // ВАЖНО: Говорим окну сжаться до красивого размера формы (400px), а не на весь экран
            stage.sizeToScene();

            stage.showAndWait(); // Показываем окно
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditDialog(JsonNode patient) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/edit-patient-dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            com.example.likarnyam.util.FxUtils.applyTheme(root);

            EditPatientController dialogController = loader.getController();
            dialogController.setParentController(this);

            // Безопасно достаем данные из JSON
            Long id = patient.get("id").asLong();
            String firstName = patient.get("firstName").asText();
            String lastName = patient.get("lastName").asText();

            String phone = patient.hasNonNull("phone") && !patient.get("phone").asText().equals("null") ? patient.get("phone").asText() : "";
            String email = patient.hasNonNull("email") && !patient.get("email").asText().equals("null") ? patient.get("email").asText() : "";
            String dob = patient.hasNonNull("dateOfBirth") && !patient.get("dateOfBirth").asText().equals("null") ? patient.get("dateOfBirth").asText() : "";
            String gender = patient.hasNonNull("gender") && !patient.get("gender").asText().equals("null") ? patient.get("gender").asText() : null;
            String bloodType = patient.hasNonNull("bloodType") && !patient.get("bloodType").asText().equals("null") ? patient.get("bloodType").asText() : null;

            // ПРЕДЗАПОЛНЯЕМ ДАННЫЕ
            dialogController.setPatientData(id, firstName, lastName, phone, email, dob, gender, bloodType);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/css.css").toExternalForm());
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            stage.setScene(scene);
            stage.sizeToScene();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                patientTableContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                patientTableContainer.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                patientTableContainer.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
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
    private void navigateSettings() {
        FxUtils.navigateFullscreen(patientTableContainer, "/fxml/settings.fxml");
    }

    @FXML
    private void navigateAppointments() {
        FxUtils.navigateFullscreen(patientTableContainer, "/fxml/appointments.fxml");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}