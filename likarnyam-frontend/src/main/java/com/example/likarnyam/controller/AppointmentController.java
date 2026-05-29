package com.example.likarnyam.controller;

import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.ScheduleApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentController {

    @FXML private Label patientNameHeader;
    @FXML private Label monthLabel;
    @FXML private VBox miniCalendarGrid;
    @FXML private FlowPane slotsContainer;
    @FXML private Label noSlotsLabel;
    @FXML private TextField reasonField;
    @FXML private TextArea notesField;
    @FXML private Button bookBtn;
    @FXML private Label mainTitleLabel;


    private Long editAppointmentId = null; // Если null -> создаем. Если есть ID -> обновляем.
    private Long patientId;
    private String patientName;
    private LocalDate selectedDate = null;
    private LocalTime selectedSlot = null;

    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    // Вызывается из PatientCardController
    public void setPatient(Long id, String name) {
        this.patientId = id;
        this.patientName = name;
        patientNameHeader.setText("Patient: " + name);
        buildMiniCalendar();
    }

    public void setAppointmentForEdit(Long appointmentId, String patientName,
                                      LocalDateTime currentDateTime, String reason, String notes) {
        this.editAppointmentId = appointmentId;
        this.patientNameHeader.setText("Editing: " + patientName);
        this.bookBtn.setText("Save Changes");
        this.mainTitleLabel.setText("Edit Appointment");

        // Заполняем текстовые поля
        this.reasonField.setText(reason != null ? reason : "");
        this.notesField.setText(notes != null ? notes : "");

        // Устанавливаем текущую дату и время из приёма
        this.currentYear = currentDateTime.getYear();
        this.currentMonth = currentDateTime.getMonthValue();
        this.selectedDate = currentDateTime.toLocalDate();
        this.selectedSlot = currentDateTime.toLocalTime();

        buildMiniCalendar();
        updateBookButton(); // Разблокируем кнопку, т.к. данные уже заполнены
    }

    @FXML
    public void initialize() {
        bookBtn.setDisable(true);
    }

    private void buildMiniCalendar() {
        LocalDate first = LocalDate.of(currentYear, currentMonth, 1);
        monthLabel.setText(first.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        miniCalendarGrid.getChildren().clear();
        int startDay = first.getDayOfWeek().getValue();
        LocalDate today = LocalDate.now();

        new Thread(() -> {
            try {
                JsonNode calDays = ScheduleApiClient.getCalendar(currentYear, currentMonth);
                Platform.runLater(() -> {
                    GridPane grid = new GridPane();
                    grid.setHgap(4); grid.setVgap(4);
                    grid.setMaxWidth(Double.MAX_VALUE);

                    for (int i = 0; i < 7; i++) {
                        ColumnConstraints col = new ColumnConstraints();
                        col.setPercentWidth(100.0 / 7); col.setHgrow(Priority.ALWAYS);
                        grid.getColumnConstraints().add(col);
                    }

                    int col = startDay - 1;
                    int row = 0;

                    for (JsonNode dayNode : calDays) {
                        int dayNum = dayNode.get("day").asInt();
                        boolean isWorking = dayNode.get("workingDay").asBoolean();
                        boolean isToday = dayNode.get("today").asBoolean();
                        LocalDate date = LocalDate.of(currentYear, currentMonth, dayNum);
                        boolean isPast = date.isBefore(today);

                        Label cell = new Label(String.valueOf(dayNum));
                        cell.setMaxWidth(Double.MAX_VALUE);
                        cell.getStyleClass().add("mini-cal-cell");

                        if (selectedDate != null && date.equals(selectedDate)) {
                            cell.getStyleClass().add("mini-cal-selected");
                            selectedDateCell = cell;
                            loadSlots(date);
                        } else if (isPast) {
                            cell.getStyleClass().add("mini-cal-past");
                        } else if (isToday) {
                            cell.getStyleClass().add("mini-cal-today");
                        } else if (isWorking) {
                            cell.getStyleClass().add("mini-cal-working");
                            cell.setOnMouseClicked(e -> selectDate(date, cell));
                        } else {
                            cell.getStyleClass().add("mini-cal-off");
                        }

                        grid.add(cell, col, row);
                        col++;
                        if (col == 7) { col = 0; row++; }
                    }
                    miniCalendarGrid.getChildren().add(grid);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Label selectedDateCell = null;

    private void selectDate(LocalDate date, Label cell) {
        if (selectedDateCell != null) {
            selectedDateCell.getStyleClass().remove("mini-cal-selected");
        }
        selectedDate = date;
        selectedDateCell = cell;
        selectedSlot = null;

        cell.getStyleClass().add("mini-cal-selected");
        loadSlots(date);
        updateBookButton();
    }

    private void loadSlots(LocalDate date) {
        slotsContainer.getChildren().clear();
        noSlotsLabel.setText("Loading slots...");

        new Thread(() -> {
            try {
                JsonNode slots = ScheduleApiClient.getAvailableSlots(date);
                Platform.runLater(() -> {
                    slotsContainer.getChildren().clear();
                    if (slots.size() == 0 && editAppointmentId == null) {
                        noSlotsLabel.setText("No available slots for this day");
                        noSlotsLabel.setVisible(true);
                    } else {
                        noSlotsLabel.setVisible(false);
                        java.util.List<LocalTime> availableTimes = new java.util.ArrayList<>();
                        for (JsonNode slot : slots) {
                            availableTimes.add(LocalTime.parse(slot.asText().substring(0, 5), DateTimeFormatter.ofPattern("HH:mm")));
                        }
                        if (editAppointmentId != null && selectedDate != null && selectedSlot != null) {
                            if (!availableTimes.contains(selectedSlot)) availableTimes.add(selectedSlot);
                        }
                        java.util.Collections.sort(availableTimes);

                        for (LocalTime time : availableTimes) {
                            Button slotBtn = new Button(time.toString());
                            slotBtn.getStyleClass().add("mini-slot-btn");

                            if (selectedSlot != null && time.equals(selectedSlot)) {
                                slotBtn.getStyleClass().add("mini-slot-selected");
                                selectedSlotBtn = slotBtn;
                            }
                            slotBtn.setOnAction(e -> selectSlot(time, slotBtn));
                            slotsContainer.getChildren().add(slotBtn);
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> noSlotsLabel.setText("Failed to load slots"));
            }
        }).start();
    }

    private Button selectedSlotBtn = null;

    private void selectSlot(LocalTime time, Button btn) {
        if (selectedSlotBtn != null) {
            selectedSlotBtn.getStyleClass().remove("mini-slot-selected");
        }
        selectedSlot = time;
        selectedSlotBtn = btn;
        btn.getStyleClass().add("mini-slot-selected");
        updateBookButton();
    }

    private void updateBookButton() {
        bookBtn.setDisable(selectedDate == null || selectedSlot == null
                || reasonField.getText().trim().isEmpty());
    }

    @FXML
    public void handleBook() {
        if (selectedDate == null || selectedSlot == null) return;
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            reasonField.setStyle(
                    reasonField.getStyle() + "-fx-border-color: #e53e3e;"
            );
            return;
        }

        bookBtn.setDisable(true);
        bookBtn.setText(editAppointmentId == null ? "Booking..." : "Saving...");

        LocalDateTime appointmentAt = LocalDateTime.of(selectedDate, selectedSlot);

        new Thread(() -> {
            try {
                // ВЕТВЛЕНИЕ: Создаем или Обновляем?
                if (editAppointmentId == null) {
                    AppointmentApiClient.createAppointment(
                            patientId, appointmentAt, reason, notesField.getText().trim()
                    );
                } else {
                    AppointmentApiClient.updateAppointment(
                            editAppointmentId, appointmentAt, reason, notesField.getText().trim()
                    );
                }

                Platform.runLater(() -> {
                    // Закрываем окно
                    Stage stage = (Stage) bookBtn.getScene().getWindow();
                    stage.close();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    bookBtn.setDisable(false);
                    bookBtn.setText(editAppointmentId == null ? "Book Appointment" : "Save Changes");
                    noSlotsLabel.setText("Failed to save: " + e.getMessage());
                    noSlotsLabel.setVisible(true);
                });
            }
        }).start();
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) bookBtn.getScene().getWindow();
        stage.close();
    }

    @FXML public void prevMonth() {
        if (currentMonth == 1) { currentMonth = 12; currentYear--; }
        else currentMonth--;
        buildMiniCalendar();
    }

    @FXML public void nextMonth() {
        if (currentMonth == 12) { currentMonth = 1; currentYear++; }
        else currentMonth++;
        buildMiniCalendar();
    }
}