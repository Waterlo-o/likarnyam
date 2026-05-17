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

        // Загружаем данные календаря чтобы знать рабочие дни
        new Thread(() -> {
            try {
                JsonNode calDays = ScheduleApiClient.getCalendar(currentYear, currentMonth);
                Platform.runLater(() -> {
                    GridPane grid = new GridPane();
                    grid.setHgap(4);
                    grid.setVgap(4);
                    grid.setMaxWidth(Double.MAX_VALUE);

                    for (int i = 0; i < 7; i++) {
                        ColumnConstraints col = new ColumnConstraints();
                        col.setPercentWidth(100.0 / 7);
                        col.setHgrow(Priority.ALWAYS);
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
                        cell.setAlignment(Pos.CENTER);
                        cell.setPrefHeight(32);

                        if (isPast) {
                            cell.setStyle(
                                    "-fx-text-fill: #cbd5e0; -fx-font-size: 12px;"
                            );
                        } else if (isToday) {
                            cell.setStyle(
                                    "-fx-background-color: #64B5F6;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-background-radius: 8;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-font-size: 12px;"
                            );
                        } else if (isWorking) {
                            cell.setStyle(
                                    "-fx-background-color: #F0F7FF;" +
                                            "-fx-text-fill: #2d3748;" +
                                            "-fx-background-radius: 8;" +
                                            "-fx-font-size: 12px;" +
                                            "-fx-cursor: hand;"
                            );
                            // Клик на рабочий день
                            cell.setOnMouseClicked(e -> selectDate(date, cell));
                            cell.setOnMouseEntered(e -> {
                                if (!date.equals(selectedDate)) {
                                    cell.setStyle(
                                            "-fx-background-color: #BEE3F8;" +
                                                    "-fx-text-fill: #2d3748;" +
                                                    "-fx-background-radius: 8;" +
                                                    "-fx-font-size: 12px;" +
                                                    "-fx-cursor: hand;"
                                    );
                                }
                            });
                            cell.setOnMouseExited(e -> {
                                if (!date.equals(selectedDate)) {
                                    cell.setStyle(
                                            "-fx-background-color: #F0F7FF;" +
                                                    "-fx-text-fill: #2d3748;" +
                                                    "-fx-background-radius: 8;" +
                                                    "-fx-font-size: 12px;" +
                                                    "-fx-cursor: hand;"
                                    );
                                }
                            });
                        } else {
                            cell.setStyle(
                                    "-fx-text-fill: #a0aec0; -fx-font-size: 12px;"
                            );
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
        // Сбрасываем предыдущий выбор
        if (selectedDateCell != null) {
            selectedDateCell.setStyle(
                    "-fx-background-color: #F0F7FF;" +
                            "-fx-text-fill: #2d3748;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-size: 12px;" +
                            "-fx-cursor: hand;"
            );
        }

        selectedDate = date;
        selectedDateCell = cell;
        selectedSlot = null;

        // Выделяем выбранный день
        cell.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );

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
                    if (slots.size() == 0) {
                        noSlotsLabel.setText("No available slots for this day");
                        noSlotsLabel.setVisible(true);
                    } else {
                        noSlotsLabel.setVisible(false);
                        for (JsonNode slot : slots) {
                            String timeStr = slot.asText().substring(0, 5);
                            Button slotBtn = new Button(timeStr);
                            slotBtn.setStyle(
                                    "-fx-background-color: #EBF4FF;" +
                                            "-fx-text-fill: #2b6cb0;" +
                                            "-fx-background-radius: 8;" +
                                            "-fx-border-color: #bee3f8;" +
                                            "-fx-border-radius: 8;" +
                                            "-fx-padding: 6 12 6 12;" +
                                            "-fx-cursor: hand;" +
                                            "-fx-font-size: 12px;"
                            );
                            slotBtn.setOnAction(e -> selectSlot(
                                    LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")),
                                    slotBtn
                            ));
                            slotsContainer.getChildren().add(slotBtn);
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        noSlotsLabel.setText("Failed to load slots")
                );
            }
        }).start();
    }

    private Button selectedSlotBtn = null;

    private void selectSlot(LocalTime time, Button btn) {
        if (selectedSlotBtn != null) {
            selectedSlotBtn.setStyle(
                    "-fx-background-color: #EBF4FF;" +
                            "-fx-text-fill: #2b6cb0;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #bee3f8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-padding: 6 12 6 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12px;"
            );
        }
        selectedSlot = time;
        selectedSlotBtn = btn;
        btn.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #1976D2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );
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
        bookBtn.setText("Booking...");

        LocalDateTime appointmentAt = LocalDateTime.of(selectedDate, selectedSlot);

        new Thread(() -> {
            try {
                AppointmentApiClient.createAppointment(
                        patientId, appointmentAt, reason,
                        notesField.getText().trim()
                );
                Platform.runLater(() -> {
                    // Закрываем окно
                    Stage stage = (Stage) bookBtn.getScene().getWindow();
                    stage.close();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    bookBtn.setDisable(false);
                    bookBtn.setText("Book Appointment");
                    noSlotsLabel.setText("Failed to book: " + e.getMessage());
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