package com.example.likarnyam.controller;


import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.DoctorApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;import com.example.likarnyam.client.ScheduleApiClient;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    @FXML private Label calMonthLabel;
    @FXML private VBox homeCalendarGrid;

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

        loadHomeCalendar();
    }

    private void loadHomeCalendar() {
        LocalDate now = LocalDate.now();
        calMonthLabel.setText(
                now.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );

        new Thread(() -> {
            try {
                JsonNode days = ScheduleApiClient.getCalendar(
                        now.getYear(), now.getMonthValue()
                );
                Platform.runLater(() -> buildHomeCalendar(days));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void buildHomeCalendar(JsonNode days) {
        homeCalendarGrid.getChildren().clear();

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();

        GridPane grid = new GridPane();
        grid.setHgap(3);
        grid.setVgap(3);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        int col = startDayOfWeek - 1;
        int row = 0;

        // Дни предыдущего месяца
        LocalDate prevMonth = firstDay.minusMonths(1);
        int daysInPrevMonth = prevMonth.lengthOfMonth();
        for (int i = startDayOfWeek - 1; i > 0; i--) {
            int prevDay = daysInPrevMonth - i + 1;
            Label cell = createHomeCalCell(
                    String.valueOf(prevDay), false, false, 0, true
            );
            grid.add(cell, startDayOfWeek - i - 1, 0);
        }

        // Дни текущего месяца
        for (JsonNode day : days) {
            int dayNum = day.get("day").asInt();
            boolean isWorking = day.get("workingDay").asBoolean();
            boolean isToday = day.get("today").asBoolean();
            int aptCount = day.get("appointmentCount").asInt();

            Label cell = createHomeCalCell(
                    String.valueOf(dayNum), isWorking, isToday, aptCount, false
            );

            // Клик → Schedule
            if (isWorking) {
                cell.setOnMouseClicked(e ->
                        FxUtils.navigateFullscreen(homeCalendarGrid, "/fxml/schedule.fxml")
                );
            }

            grid.add(cell, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        // Дни следующего месяца
        int nextDay = 1;
        while (col < 7 && col > 0) {
            Label cell = createHomeCalCell(
                    String.valueOf(nextDay), false, false, 0, true
            );
            grid.add(cell, col, row);
            col++;
            nextDay++;
        }

        homeCalendarGrid.getChildren().add(grid);

        // Мини статистика снизу
        addCalendarFooter(days);
    }

    private Label createHomeCalCell(String text, boolean isWorking,
                                    boolean isToday, int aptCount,
                                    boolean isAdjacent) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setAlignment(Pos.CENTER);
        cell.setPrefHeight(26);

        if (isAdjacent) {
            cell.setStyle("-fx-text-fill: #cbd5e0; -fx-font-size: 11px;");
            return cell;
        }

        if (isToday) {
            cell.setStyle(
                    "-fx-background-color: #64B5F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11px;" +
                            "-fx-cursor: hand;"
            );
        } else if (isWorking && aptCount > 0) {
            String intensity = aptCount >= 5 ? "#FED7D7" : aptCount >= 3 ? "#BEE3F8" : "#EBF4FF";
            String textColor = aptCount >= 5 ? "#c53030" : "#2b6cb0";
            cell.setStyle(
                    "-fx-background-color: " + intensity + ";" +
                            "-fx-text-fill: " + textColor + ";" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
            // Tooltip с количеством
            Tooltip tooltip = new Tooltip(aptCount + " appointment" + (aptCount > 1 ? "s" : ""));
            tooltip.setStyle("-fx-font-size: 11px;");
            Tooltip.install(cell, tooltip);
        } else if (isWorking) {
            cell.setStyle(
                    "-fx-background-color: #F0FFF4;" +
                            "-fx-text-fill: #276749;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-cursor: hand;"
            );
        } else {
            cell.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");
        }

        return cell;
    }

    private void addCalendarFooter(JsonNode days) {
        int totalApts = 0;
        int totalSlots = 0;

        for (JsonNode day : days) {
            totalApts += day.get("appointmentCount").asInt();
            // Считаем рабочие дни — в каждом ~16 слотов (8 часов / 30 мин)
            if (day.get("workingDay").asBoolean()) {
                totalSlots += 16;
            }
        }

        int freeSlots = totalSlots - totalApts;

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(8, 0, 15, 0));

        // Статистика
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER);

        VBox aptsBox = createStatBox(
                String.valueOf(totalApts),
                "booked",
                "#EBF4FF", "#2b6cb0"
        );

        VBox freeBox = createStatBox(
                String.valueOf(freeSlots),
                "available",
                "#F0FFF4", "#276749"
        );

        HBox.setHgrow(aptsBox, Priority.ALWAYS);
        HBox.setHgrow(freeBox, Priority.ALWAYS);
        stats.getChildren().addAll(aptsBox, freeBox);

        homeCalendarGrid.getChildren().addAll(sep, stats);
    }

    private VBox createStatBox(String number, String label,
                               String bg, String fg) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 12 8 12;"
        );

        Label numLabel = new Label(number);
        numLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";"
        );

        Label textLabel = new Label(label);
        textLabel.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: " + fg + "; -fx-opacity: 0.8;"
        );

        box.getChildren().addAll(numLabel, textLabel);
        return box;
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
    @FXML
    private void navigateHome() {
        // Уже на Home — ничего не делаем
    }

    @FXML
    private void navigatePatients() {
        FxUtils.navigateFullscreen(totalVisitsLabel, "/fxml/patient-list.fxml");
    }

    @FXML
    private void navigateSchedule() {
        FxUtils.navigateFullscreen(totalVisitsLabel, "/fxml/schedule.fxml");
    }

    @FXML
    private void navigateMessages() {
        // TODO
        System.out.println("Messages — coming soon");
    }

    @FXML
    private void navigateSettings() {
        // TODO Sprint 3
        System.out.println("Settings — coming soon");
    }

    @FXML
    private void handleLogout() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void navigateAppointments() {
        // TODO Sprint 2
        System.out.println("Appointments — coming soon");
    }
}