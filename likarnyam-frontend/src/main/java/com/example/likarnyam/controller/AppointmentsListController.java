package com.example.likarnyam.controller;

import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.util.List;

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
        Platform.runLater(() ->
                FxUtils.showLoading(appointmentsContainer, "Loading appointments...")
        );

        new Thread(() -> {
            try {
                JsonNode appointments = AppointmentApiClient.getAllAppointments(status);
                Platform.runLater(() -> {
                    allAppointments = appointments;
                    displayAppointments(appointments);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        FxUtils.showEmpty(appointmentsContainer, "Failed to load appointments")
                );
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
        Long appointmentId = apt.get("id").asLong();
        String firstName = apt.get("patientFirstName").asText();
        String lastName = apt.get("patientLastName").asText();
        String aptAt = FxUtils.formatTime(apt.get("appointmentAt").asText());
        String reason = apt.get("reason").asText();
        String status = apt.get("status").asText();
        String notes = apt.has("notes") && !apt.get("notes").asText().equals("null") ? apt.get("notes").asText() : "—";

        String fullRaw = apt.get("appointmentAt").asText(); // "2026-05-27T14:30:00"
        String date = fullRaw.substring(0, 10);            // Берем дату из "сырого" JSON
        String time = FxUtils.formatTime(fullRaw);

        // Аватар (Используем класс из списка пациентов!)
        Label avatar = new Label(String.valueOf(firstName.charAt(0)) + String.valueOf(lastName.charAt(0)));
        avatar.getStyleClass().add("patient-avatar");
        avatar.setMinWidth(32); avatar.setMinHeight(32); avatar.setMaxWidth(32); avatar.setMaxHeight(32);

        // Имя
        Label nameLabel = new Label(firstName + " " + lastName);
        nameLabel.getStyleClass().add("apt-name-label");
        HBox nameCell = new HBox(8, avatar, nameLabel);
        nameCell.setMinWidth(180); nameCell.setMaxWidth(180); nameCell.setAlignment(Pos.CENTER_LEFT);

        // Дата
        Label dateLabel = new Label(date + "\n" + time);
        dateLabel.getStyleClass().add("apt-text-label");
        dateLabel.setMinWidth(140); dateLabel.setMaxWidth(140);

        // Причина
        Label reasonLabel = new Label(reason);
        reasonLabel.getStyleClass().add("apt-text-label");
        reasonLabel.setMinWidth(160); reasonLabel.setMaxWidth(160); reasonLabel.setWrapText(true);

        // Статус бейдж
        String badgeText = switch (status) {
            case "COMPLETED" -> "✓ Completed";
            case "CANCELLED" -> "✕ Cancelled";
            case "NO_SHOW" -> "? No Show";
            default -> "● Scheduled";
        };
        Label statusBadge = new Label(badgeText);
        // Добавляем общий класс и класс конкретного статуса
        statusBadge.getStyleClass().addAll("apt-status-badge", "apt-status-" + status.toLowerCase());
        HBox statusCell = new HBox(statusBadge);
        statusCell.setMinWidth(110); statusCell.setMaxWidth(110); statusCell.setAlignment(Pos.CENTER_LEFT);

        // Заметки
        Label notesLabel = new Label(notes);
        notesLabel.getStyleClass().add("apt-notes-label");
        notesLabel.setMinWidth(100); notesLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(notesLabel, Priority.ALWAYS);

        // КНОПКИ ДЕЙСТВИЙ
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setMinWidth(60);
        editBtn.setOnAction(e -> openEditWindow(apt));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setMinWidth(65);
        deleteBtn.setOnAction(e -> confirmAndDelete(appointmentId, firstName + " " + lastName));

        HBox actionButtons = new HBox(8, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setMinWidth(130);

        HBox row = new HBox(10, nameCell, dateLabel, reasonLabel, statusCell, notesLabel, actionButtons);
        row.getStyleClass().add("patient-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 20 10 10;"); // Отступы можно оставить тут, они не мешают цвету

        return row;
    }

    private void openEditWindow(JsonNode apt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new-appointment.fxml"));
            Parent root = loader.load();

            AppointmentController controller = loader.getController();

            Long id = apt.get("id").asLong();
            String patientName = apt.get("patientFirstName").asText() + " "
                    + apt.get("patientLastName").asText();
            LocalDateTime dateTime = LocalDateTime.parse(apt.get("appointmentAt").asText());
            String reason = apt.get("reason").asText();
            String notes = apt.has("notes") && !apt.get("notes").asText().equals("null")
                    ? apt.get("notes").asText() : null;

            // Собираем symptomIds из ответа
            List<Long> symptomIds = new java.util.ArrayList<>();
            if (apt.has("symptoms") && apt.get("symptoms").isArray()) {
                for (JsonNode s : apt.get("symptoms")) {
                    symptomIds.add(s.get("id").asLong());
                }
            }

            Long diseaseId = null;
            String diseaseName = null;
            if (apt.has("disease") && !apt.get("disease").isNull()) {
                diseaseId = apt.get("disease").get("id").asLong();
                diseaseName = apt.get("disease").get("name").asText();
            }

            controller.setAppointmentForEdit(id, patientName, dateTime, reason, notes,
                    symptomIds, diseaseId, diseaseName);

            FxUtils.applyTheme(root);

            StackPane wrapper = new StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

            Scene scene = new Scene(wrapper);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setTitle("Edit Appointment");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setOnHidden(e -> loadAppointments(currentFilter));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void confirmAndDelete(Long appointmentId, String patientName) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(appointmentsContainer.getScene().getWindow());

        VBox root = new VBox(15);
        root.getStyleClass().add("dialog-root");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Delete Appointment");
        title.getStyleClass().add("dialog-title");

        Label msg = new Label("Are you sure you want to delete the appointment for " + patientName + "?");
        msg.getStyleClass().add("dialog-msg");
        msg.setWrapText(true);
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-dialog-cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyleClass().add("btn-delete");
        confirmBtn.setOnAction(e -> { dialog.close(); executeDelete(appointmentId); });

        HBox buttons = new HBox(12, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, msg, buttons);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ✅ Грузим CSS напрямую по пути
        String css = getClass().getResource("/css/css.css").toExternalForm();
        scene.getStylesheets().add(css);

        // ✅ Применяем тему если нужно (тёмная/светлая)
        FxUtils.applyTheme(root);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void executeDelete(Long id) {
        new Thread(() -> {
            try {
                AppointmentApiClient.deleteAppointment(id);
                Platform.runLater(() -> loadAppointments(currentFilter)); // Обновляем таблицу
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Фильтры
    private void setActiveFilter(Button active) {
        // Убираем активный класс у всех, ставим базовый
        List<Button> buttons = List.of(filterAll, filterScheduled, filterCompleted, filterCancelled, filterNoShow);
        for (Button btn : buttons) {
            btn.getStyleClass().remove("filter-btn-active");
            if (!btn.getStyleClass().contains("filter-btn")) {
                btn.getStyleClass().add("filter-btn");
            }
        }
        // Ставим активный класс на нужную
        active.getStyleClass().remove("filter-btn");
        active.getStyleClass().add("filter-btn-active");
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

    @FXML
    private void handleClose() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                appointmentsContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                appointmentsContainer.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                appointmentsContainer.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
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
    @FXML
    private void navigateSettings() {
        FxUtils.navigateFullscreen(appointmentsContainer, "/fxml/settings.fxml");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}