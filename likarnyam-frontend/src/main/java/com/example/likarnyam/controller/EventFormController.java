package com.example.likarnyam.controller;

import com.example.likarnyam.client.EventApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EventFormController {

    @FXML private Label headerLabel;
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextArea descArea;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private Long currentEventId = null;

    @FXML
    public void initialize() {
        // Заполняем часы (00-23)
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d", i);
        }
        hourCombo.setItems(FXCollections.observableArrayList(hours));

        // Заполняем минуты с шагом в 15 минут
        minuteCombo.setItems(FXCollections.observableArrayList("00", "15", "30", "45"));

        // Типы событий
        typeCombo.setItems(FXCollections.observableArrayList(
                "General", "Meeting", "Conference", "Training", "Personal"
        ));
        typeCombo.setValue("General");
    }

    // Этот метод вызывается из EventsListController перед показом окна
    public void setEventData(JsonNode event) {
        if (event != null) {
            currentEventId = event.get("id").asLong();
            headerLabel.setText("Edit Event");
            saveBtn.setText("Update Event");

            titleField.setText(event.get("title").asText());

            if (event.has("location") && !event.get("location").isNull()) {
                locationField.setText(event.get("location").asText());
            }
            if (event.has("eventType") && !event.get("eventType").isNull()) {
                typeCombo.setValue(event.get("eventType").asText());
            }
            if (event.has("description") && !event.get("description").isNull()) {
                descArea.setText(event.get("description").asText());
            }

            // Парсинг даты и времени
            String eventAt = event.get("eventAt").asText(); // ISO формат: 2026-05-27T10:00:00
            LocalDateTime dateTime = LocalDateTime.parse(eventAt);
            datePicker.setValue(dateTime.toLocalDate());
            hourCombo.setValue(String.format("%02d", dateTime.getHour()));
            minuteCombo.setValue(String.format("%02d", dateTime.getMinute()));
        }
    }

    @FXML
    private void handleSave() {
        // Валидация
        String title = titleField.getText().trim();
        LocalDate date = datePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();

        if (title.isEmpty() || date == null || hour == null || minute == null) {
            showError("Title, Date, and Time are required.");
            return;
        }

        // Сборка данных
        String location = locationField.getText().trim();
        String type = typeCombo.getValue();
        String desc = descArea.getText().trim();

        // Формируем строку ISO-8601
        LocalTime time = LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute));
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        String eventAtIso = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        saveBtn.setDisable(true);
        saveBtn.setText("Saving...");
        errorLabel.setVisible(false);

        // Отправка на бэкенд в отдельном потоке
        new Thread(() -> {
            try {
                if (currentEventId == null) {
                    EventApiClient.createEvent(title, desc, eventAtIso, location, type);
                } else {
                    EventApiClient.updateEvent(currentEventId, title, desc, eventAtIso, location, type);
                }

                // Закрываем окно при успехе
                Platform.runLater(this::closeWindow);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to save event. Please try again.");
                    saveBtn.setDisable(false);
                    saveBtn.setText(currentEventId == null ? "Save Event" : "Update Event");
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showError(String message) {
        errorLabel.getStyleClass().setAll("settings-result-error");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
}