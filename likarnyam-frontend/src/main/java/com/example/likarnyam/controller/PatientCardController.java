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
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class PatientCardController {

    @FXML private Label patientNameLabel;
    @FXML private Label patientAvatarLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private FlowPane allergiesPane;
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

                    // Аллергии тегами
                    allergiesPane.getChildren().clear();
                    JsonNode allergies = patient.has("allergies") ? patient.get("allergies") : null;
                    if (allergies == null || allergies.size() == 0) {
                        Label none = new Label("—");
                        none.setStyle("-fx-text-fill: #888;");
                        allergiesPane.getChildren().add(none);
                    } else {
                        boolean isDark = com.example.likarnyam.session.UserSession.getInstance()
                                .getTheme() != null &&
                                com.example.likarnyam.session.UserSession.getInstance()
                                        .getTheme().equalsIgnoreCase("dark");
                        String bg     = isDark ? "#1A2E4A" : "#EBF4FF";
                        String border = "#64B5F6";
                        String text   = isDark ? "#90C8F0" : "#0C447C";

                        for (JsonNode a : allergies) {
                            HBox tag = new HBox(5);
                            tag.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            tag.setStyle(String.format(
                                    "-fx-background-color: %s; -fx-border-color: %s; " +
                                            "-fx-border-radius: 20; -fx-background-radius: 20; " +
                                            "-fx-border-width: 1; -fx-padding: 3 8;",
                                    bg, border
                            ));

                            try {
                                org.kordamp.ikonli.javafx.FontIcon icon =
                                        new org.kordamp.ikonli.javafx.FontIcon(
                                                a.get("icon").asText());
                                icon.setIconSize(11);
                                icon.setIconColor(javafx.scene.paint.Color.web(text));
                                tag.getChildren().add(icon);
                            } catch (Exception ignored) {}

                            Label name = new Label(a.get("name").asText());
                            name.setStyle("-fx-font-size: 11px; -fx-text-fill: " + text + ";");
                            tag.getChildren().add(name);

                            allergiesPane.getChildren().add(tag);
                        }
                    }

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
        String fullRaw = visit.get("appointmentAt").asText();
        String date = fullRaw.substring(0, 10);
        String time = FxUtils.formatTime(fullRaw);
        String reason = visit.get("reason").asText();
        String status = visit.get("status").asText();
        String notes = getValue(visit, "notes");

        // Статус
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().addAll(
                "history-status-badge",
                status.equals("COMPLETED") ? "history-status-completed" : "history-status-other"
        );

        // Шапка
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

        // Симптомы
        JsonNode symptoms = visit.has("symptoms") ? visit.get("symptoms") : null;
        if (symptoms != null && symptoms.size() > 0) {
            javafx.scene.layout.FlowPane symptomsPane =
                    new javafx.scene.layout.FlowPane();
            symptomsPane.setHgap(5);
            symptomsPane.setVgap(5);
            symptomsPane.setPadding(new javafx.geometry.Insets(4, 0, 0, 0));

            for (JsonNode s : symptoms) {
                String category = s.get("category").asText().toLowerCase().replace(" ", "-");
                boolean isDark = com.example.likarnyam.session.UserSession.getInstance()
                        .getTheme() != null &&
                        com.example.likarnyam.session.UserSession.getInstance()
                                .getTheme().equalsIgnoreCase("dark");

                String bg, border, textColor;
                switch (category) {
                    case "respiratory"      -> { bg = isDark ? "#1A2A3A" : "#E6F1FB"; border = "#85B7EB"; textColor = isDark ? "#90C8F0" : "#0C447C"; }
                    case "cardiovascular"   -> { bg = isDark ? "#3A1A1A" : "#FCEBEB"; border = "#F09595"; textColor = isDark ? "#F0A0A0" : "#791F1F"; }
                    case "neurological"     -> { bg = isDark ? "#1E1A3A" : "#EEEDFE"; border = "#AFA9EC"; textColor = isDark ? "#C0BAFF" : "#3C3489"; }
                    case "gastrointestinal" -> { bg = isDark ? "#2A1E08" : "#FAEEDA"; border = "#EF9F27"; textColor = isDark ? "#F0B060" : "#633806"; }
                    case "musculoskeletal"  -> { bg = isDark ? "#0A1E18" : "#E1F5EE"; border = "#5DCAA5"; textColor = isDark ? "#70D4B0" : "#085041"; }
                    case "skin"             -> { bg = isDark ? "#2A0A18" : "#FBEAF0"; border = "#ED93B1"; textColor = isDark ? "#F0A0C0" : "#72243E"; }
                    case "ent"              -> { bg = isDark ? "#0A1E08" : "#EAF3DE"; border = "#97C459";  textColor = isDark ? "#A8D470" : "#27500A"; }
                    case "urological"       -> { bg = isDark ? "#0A1A2A" : "#EAF3FB"; border = "#7EC8E3"; textColor = isDark ? "#90D4F0" : "#0A4A6E"; }
                    case "psychological"    -> { bg = isDark ? "#1A0A2A" : "#F3EAFB"; border = "#C49AE3"; textColor = isDark ? "#D0A8F0" : "#4A1A72"; }
                    default                 -> { bg = isDark ? "#2D2D2D" : "#F1EFE8"; border = "#B4B2A9"; textColor = isDark ? "#C8C6BE" : "#444441"; }
                }

                HBox tag = new HBox(4);
                tag.setAlignment(Pos.CENTER_LEFT);
                tag.setStyle(String.format(
                        "-fx-background-color: %s; -fx-border-color: %s; " +
                                "-fx-border-radius: 12; -fx-background-radius: 12; " +
                                "-fx-border-width: 1; -fx-padding: 3 8;",
                        bg, border
                ));

                try {
                    org.kordamp.ikonli.javafx.FontIcon icon =
                            new org.kordamp.ikonli.javafx.FontIcon(s.get("icon").asText());
                    icon.setIconSize(11);
                    icon.setIconColor(javafx.scene.paint.Color.web(textColor));
                    tag.getChildren().add(icon);
                } catch (Exception ignored) {}

                Label nameLabel = new Label(s.get("name").asText());
                nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + textColor + ";");
                tag.getChildren().add(nameLabel);

                symptomsPane.getChildren().add(tag);
            }

            Label symptomsTitle = new Label("Symptoms:");
            symptomsTitle.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #718096; -fx-font-weight: bold;"
            );
            card.getChildren().addAll(symptomsTitle, symptomsPane);
        }

        // Диагноз
        JsonNode disease = visit.has("disease") && !visit.get("disease").isNull()
                ? visit.get("disease") : null;
        if (disease != null) {
            boolean isDark = com.example.likarnyam.session.UserSession.getInstance()
                    .getTheme() != null &&
                    com.example.likarnyam.session.UserSession.getInstance()
                            .getTheme().equalsIgnoreCase("dark");

            String bg     = isDark ? "#0A1E18" : "#F0FFF4";
            String border = "#38a169";
            String text   = "#38a169";

            HBox diagnosisTag = new HBox(6);
            diagnosisTag.setAlignment(Pos.CENTER_LEFT);
            diagnosisTag.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: %s; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8; " +
                            "-fx-border-width: 1; -fx-padding: 5 10;",
                    bg, border
            ));

            Label diagIcon = new Label("⊕");
            diagIcon.setStyle("-fx-text-fill: " + text + "; -fx-font-size: 12px;");

            String icdCode = disease.has("icdCode") && !disease.get("icdCode").isNull()
                    ? " · ICD: " + disease.get("icdCode").asText() : "";

            Label diagName = new Label(disease.get("name").asText() + icdCode);
            diagName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + text + ";");

            diagnosisTag.getChildren().addAll(diagIcon, diagName);

            Label diagTitle = new Label("Diagnosis:");
            diagTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-font-weight: bold;");

            card.getChildren().addAll(diagTitle, diagnosisTag);
        }

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

    @FXML
    private void handleEditPatient() {
        try {
            // СТАЛО (используем твой родной getById):
            com.fasterxml.jackson.databind.JsonNode patient = com.example.likarnyam.client.PatientApiClient.getById(this.patientId);

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/edit-patient-dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            com.example.likarnyam.util.FxUtils.applyTheme(root);

            com.example.likarnyam.controller.EditPatientController dialogController = loader.getController();
            dialogController.setParentController(null);

             Long id = patient.get("id").asLong();
            String firstName = patient.get("firstName").asText();
            String lastName = patient.get("lastName").asText();

            String phone = patient.hasNonNull("phone") && !patient.get("phone").asText().equals("null") ? patient.get("phone").asText() : "";
            String email = patient.hasNonNull("email") && !patient.get("email").asText().equals("null") ? patient.get("email").asText() : "";
            String dob = patient.hasNonNull("dateOfBirth") && !patient.get("dateOfBirth").asText().equals("null") ? patient.get("dateOfBirth").asText() : "";
            String gender = patient.hasNonNull("gender") && !patient.get("gender").asText().equals("null") ? patient.get("gender").asText() : null;
            String bloodType = patient.hasNonNull("bloodType") && !patient.get("bloodType").asText().equals("null") ? patient.get("bloodType").asText() : null;

            dialogController.setPatientData(id, firstName, lastName, phone, email, dob, gender, bloodType);

            List<Long> allergyIds = new java.util.ArrayList<>();
            if (patient.has("allergies") && patient.get("allergies").isArray()) {
                for (JsonNode a : patient.get("allergies")) {
                    allergyIds.add(a.get("id").asLong());
                }
            }
            dialogController.setPatientAllergies(allergyIds);

             javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/css.css").toExternalForm());
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            stage.setScene(scene);
            stage.sizeToScene();

            stage.showAndWait();

            loadPatientData();

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