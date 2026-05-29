package com.example.likarnyam.controller;

import com.example.likarnyam.client.EventApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EventsListController {

    @FXML private VBox eventsContainer;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;

    private JsonNode allEvents = null;

    @FXML
    public void initialize() {
        loadEvents();
    }

    private void loadEvents() {
        Platform.runLater(() -> FxUtils.showLoading(eventsContainer, "Loading events..."));

        new Thread(() -> {
            try {
                JsonNode events = EventApiClient.getAllEvents();
                Platform.runLater(() -> {
                    allEvents = events;
                    displayEvents(events);
                });
            } catch (Exception e) {
                Platform.runLater(() -> FxUtils.showEmpty(eventsContainer, "Failed to load events"));
                e.printStackTrace();
            }
        }).start();
    }

    private void displayEvents(JsonNode events) {
        eventsContainer.getChildren().clear();
        String search = searchField.getText().trim().toLowerCase();

        int count = 0;
        for (JsonNode event : events) {
            String title = event.get("title").asText();

            if (!search.isEmpty() && !title.toLowerCase().contains(search)) {
                continue;
            }

            eventsContainer.getChildren().add(createRow(event));
            count++;
        }

        totalLabel.setText(count + " events");
        if (count == 0) {
            FxUtils.showEmpty(eventsContainer, "No events found");
        }
    }

    private HBox createRow(JsonNode event) {
        Long eventId = event.get("id").asLong();
        String title = event.get("title").asText();
        String eventAt = event.get("eventAt").asText();
        String location = event.has("location") && !event.get("location").isNull() ? event.get("location").asText() : "—";
        String type = event.has("eventType") && !event.get("eventType").isNull() ? event.get("eventType").asText() : "General";
        String desc = event.has("description") && !event.get("description").isNull() ? event.get("description").asText() : "—";

        String date = eventAt.substring(0, 10);
        String time = eventAt.substring(11, 16);

        Label avatar = new Label(String.valueOf(title.charAt(0)).toUpperCase());
        avatar.getStyleClass().add("event-avatar"); // ✅

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("apt-name-label"); // ✅ переиспользуем
        titleLabel.setWrapText(true);
        HBox titleCell = new HBox(10, avatar, titleLabel);
        titleCell.setMinWidth(180);
        titleCell.setMaxWidth(180);
        titleCell.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date + "\n" + time);
        dateLabel.getStyleClass().add("apt-text-label"); // ✅
        dateLabel.setMinWidth(140);
        dateLabel.setMaxWidth(140);

        Label locationLabel = new Label(location);
        locationLabel.getStyleClass().add("apt-text-label"); // ✅
        locationLabel.setMinWidth(160);
        locationLabel.setMaxWidth(160);
        locationLabel.setWrapText(true);

        Label typeBadge = new Label(type);
        typeBadge.getStyleClass().add("event-type-badge"); // ✅
        HBox typeCell = new HBox(typeBadge);
        typeCell.setMinWidth(110);
        typeCell.setMaxWidth(110);
        typeCell.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("apt-notes-label"); // ✅
        descLabel.setMinWidth(100);
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setWrapText(true);
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-edit"); // ✅
        editBtn.setMinWidth(60);
        editBtn.setOnAction(e -> openEventModal(event));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete"); // ✅
        deleteBtn.setMinWidth(65);
        deleteBtn.setOnAction(e -> confirmAndDelete(eventId, title));

        HBox actionButtons = new HBox(8, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setMinWidth(130);

        HBox row = new HBox(10, titleCell, dateLabel, locationLabel, typeCell, descLabel, actionButtons);
        row.getStyleClass().add("patient-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 20 10 10;");

        return row;
    }

    private void openEventModal(JsonNode event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event-form.fxml"));
            Parent root = loader.load();

            EventFormController controller = loader.getController();
            controller.setEventData(event);

            javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

            FxUtils.applyTheme(wrapper); // ✅ на wrapper, не на root

            Scene scene = new Scene(wrapper);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setTitle(event == null ? "New Event" : "Edit Event");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setOnHidden(e -> loadEvents());
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void confirmAndDelete(Long eventId, String title) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(eventsContainer.getScene().getWindow()); // ✅

        VBox root = new VBox(15);
        root.getStyleClass().add("dialog-root"); // ✅
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Delete Event");
        titleLabel.getStyleClass().add("dialog-title"); // ✅

        Label msg = new Label("Are you sure you want to delete '" + title + "'?\nThis action cannot be undone.");
        msg.getStyleClass().add("dialog-msg"); // ✅
        msg.setWrapText(true);
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-dialog-cancel"); // ✅
        cancelBtn.setMinWidth(80);
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyleClass().add("btn-delete"); // ✅
        confirmBtn.setMinWidth(80);
        confirmBtn.setOnAction(e -> {
            dialog.close();
            executeDelete(eventId);
        });

        HBox buttons = new HBox(12, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER);
        root.getChildren().addAll(titleLabel, msg, buttons);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/css.css").toExternalForm());
        FxUtils.applyTheme(root); // ✅

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handleNewEvent() {
        openEventModal(null);
    }

    private void executeDelete(Long id) {
        new Thread(() -> {
            try {
                EventApiClient.deleteEvent(id);
                Platform.runLater(this::loadEvents);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        if (allEvents != null) {
            displayEvents(allEvents);
        }
    }

    // --- Навигация (стандартная для вашего проекта) ---
    @FXML private void navigateHome() { FxUtils.navigateFullscreen(eventsContainer, "/fxml/home.fxml"); }
    @FXML private void navigatePatients() { FxUtils.navigateFullscreen(eventsContainer, "/fxml/patient-list.fxml"); }
    @FXML private void navigateSchedule() { FxUtils.navigateFullscreen(eventsContainer, "/fxml/schedule.fxml"); }
    @FXML private void navigateAppointments() { FxUtils.navigateFullscreen(eventsContainer, "/fxml/appointments.fxml"); }
    @FXML private void navigateSettings() { FxUtils.navigateFullscreen(eventsContainer, "/fxml/settings.fxml"); }
    @FXML private void handleLogout() { javafx.application.Platform.exit(); }
}