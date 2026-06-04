package com.example.likarnyam.controller;


import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.DoctorApiClient;
import com.example.likarnyam.client.AuthExpiredException;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;import com.example.likarnyam.client.ScheduleApiClient;
import javafx.geometry.Pos;
import com.example.likarnyam.client.EventApiClient;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HomeController {


    @FXML private VBox upcomingContainer;
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
    @FXML private Text greetingPrefix;
    @FXML private BorderPane rootContainer;



    @FXML
    public void initialize() {
        new Thread(() -> {
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    System.out.println("Attempt " + (i + 1) + " to connect...");
                    JsonNode doctor = DoctorApiClient.getMe();
                    JsonNode appointments = AppointmentApiClient.getTodayAppointments();

                    String firstName = doctor.get("firstName").asText();
                    String lastName = doctor.get("lastName").asText();
                    int totalVisits = appointments.size();

                    if (doctor.has("theme")) {
                        UserSession.getInstance().setTheme(doctor.get("theme").asText());
                    }
                    if (doctor.has("timeFormat")) {
                        UserSession.getInstance().setTimeFormat(doctor.get("timeFormat").asText());
                    }
                    if (doctor.has("animationsEnabled")) {
                        UserSession.getInstance().setAnimationsEnabled(doctor.get("animationsEnabled").asBoolean());
                    };

                    Platform.runLater(() -> {

                        FxUtils.isDarkMode = "DARK".equals(UserSession.getInstance().getTheme());

                        if (rootContainer.getScene() != null) {
                            FxUtils.applyTheme(rootContainer.getScene().getRoot());
                        } else {
                            rootContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                                if (newScene != null) {
                                    FxUtils.applyTheme(newScene.getRoot());
                                }
                            });
                        }

                        int hour = java.time.LocalTime.now().getHour();
                        String greeting = hour < 12 ? "Good Morning "
                                : hour < 17 ? "Good Afternoon "
                                : "Good Evening ";
                        if (greetingPrefix != null) greetingPrefix.setText(greeting);

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

                        if (scheduledLabel != null) scheduledLabel.setText(String.valueOf(scheduled));
                        if (completedLabel != null) completedLabel.setText(String.valueOf(completed));

                        if (patientListContainer != null) {
                            patientListContainer.getChildren().clear();
                            currentAppointments = appointments;
                            updatePatientList(appointments);
                        }
                    });
                    break;

                } catch (AuthExpiredException e) {
                    handleAuthExpired();
                    break;
                } catch (Exception e) {
                    System.out.println("Attempt " + (i + 1) + " failed: " + e.getMessage());
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { break; }
                }
            }

            loadUpcomingEvents();

        }).start();
        loadHomeCalendar();
        startClock();

    }

    private void loadUpcomingEvents() {
        new Thread(() -> {
            try {
                JsonNode events = EventApiClient.getUpcoming();
                Platform.runLater(() -> {
                    upcomingContainer.getChildren().clear();
                    if (events.size() == 0) {
                        Label empty = new Label("No upcoming events");
                        empty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;");
                        upcomingContainer.getChildren().add(empty);
                        return;
                    }
                    int limit = Math.min(events.size(), 3);
                    for (int i = 0; i < limit; i++) {
                        JsonNode event = events.get(i);
                        upcomingContainer.getChildren().add(createEventCard(event));
                    }
                });
            } catch (AuthExpiredException e) {
                handleAuthExpired();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
            } catch (AuthExpiredException e) {
                handleAuthExpired();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private volatile boolean isLoggingOut = false;

    private void handleAuthExpired() {
        if (isLoggingOut) return;
        isLoggingOut = true;

        Platform.runLater(() -> {
            System.out.println("Session expired. Redirecting to login...");
            UserSession.getInstance().logout();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

                Stage stage = null;
                // Проверяем, прикрепился ли уже UI к сцене
                if (totalVisitsLabel != null && totalVisitsLabel.getScene() != null) {
                    stage = (Stage) totalVisitsLabel.getScene().getWindow();
                }
                // Если нет, берем первое активное окно приложения
                else if (!javafx.stage.Window.getWindows().isEmpty()) {
                    stage = (Stage) javafx.stage.Window.getWindows().get(0);
                }

                if (stage != null) {
                    stage.setScene(new Scene(root, 800, 500));
                    stage.setResizable(false);
                    stage.show();
                } else {
                    System.err.println("Error: Could not find main window for redirect.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private HBox createEventCard(JsonNode event) {
        String title = event.get("title").asText();
        String eventAt = event.get("eventAt").asText();
        String location = event.has("location") &&
                !event.get("location").asText().equals("null")
                ? event.get("location").asText() : "—";
        String type = event.has("eventType") ? event.get("eventType").asText() : "MEETING";
        String description = event.has("description") &&
                !event.get("description").asText().equals("null")
                ? event.get("description").asText() : "—";

        String date = eventAt.substring(0, 10);
        String time = FxUtils.formatTime(eventAt);

        String color = switch (type) {
            case "CONFERENCE" -> "#9F7AEA";
            case "TRAINING" -> "#38A169";
            default -> "#64B5F6";
        };

        VBox stripe = new VBox();
        stripe.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 3;" +
                        "-fx-min-width: 4; -fx-max-width: 4; -fx-min-height: 40;"
        );

        VBox info = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;"
        );
        titleLabel.setWrapText(true);

        String subText = date + " | " + time + " | " + location;
        Label subLabel = new Label(subText);
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        subLabel.setWrapText(true);

        info.getChildren().addAll(titleLabel, subLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox card = new HBox(10, stripe, info);
        card.getStyleClass().add("event-badge");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        // Клик → popup
        card.setOnMouseClicked(e -> showEventPopup(event, card));

        return card;
    }

    private void showEventPopup(JsonNode event, HBox anchor) {
        String title = event.get("title").asText();
        String eventAt = event.get("eventAt").asText();
        String location = event.has("location") &&
                !event.get("location").asText().equals("null")
                ? event.get("location").asText() : "—";
        String type = event.has("eventType") ? event.get("eventType").asText() : "MEETING";
        String description = event.has("description") &&
                !event.get("description").asText().equals("null")
                ? event.get("description").asText() : "—";

        String date = eventAt.substring(0, 10);
        String time = FxUtils.formatTime(eventAt);

        String typeLabel = switch (type) {
            case "CONFERENCE" -> "Conference";
            case "TRAINING"   -> "Training";
            default           -> "Meeting";
        };
        String locationCardClass = switch (type) {
            case "CONFERENCE" -> "popup-location-card-conference";
            case "TRAINING"   -> "popup-location-card-training";
            default           -> "popup-location-card-meeting";
        };

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox content = new VBox(0);
        content.getStyleClass().add("popup-content"); // ✅
        content.setPrefWidth(320);
        // CSS из текущей сцены
        content.getStylesheets().add(
                getClass().getResource("/css/css.css").toExternalForm()
        );
        if (FxUtils.isDarkMode) content.getStyleClass().add("dark-theme"); // ✅

        // Шапка
        VBox header = new VBox(8);
        header.getStyleClass().add("popup-event-header"); // ✅

        Label typeBadge = new Label(typeLabel.toUpperCase());
        typeBadge.getStyleClass().add("popup-type-badge"); // ✅

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("popup-event-title"); // ✅
        titleLabel.setWrapText(true);

        Label dateTimeLabel = new Label(date + "  •  " + time);
        dateTimeLabel.getStyleClass().add("popup-event-datetime"); // ✅

        header.getChildren().addAll(typeBadge, titleLabel, dateTimeLabel);

        // Тело
        VBox body = new VBox(0);
        body.getStyleClass().add("popup-body");

        if (!location.equals("—")) {
            HBox locationCard = new HBox(12);
            locationCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            locationCard.getStyleClass().addAll("popup-location-card", locationCardClass); // ✅

            VBox locationIcon = new VBox();
            locationIcon.getStyleClass().add("popup-location-icon"); // ✅
            Label locIconLabel = new Label("📍");
            locIconLabel.setStyle("-fx-font-size: 14px;");
            locationIcon.getChildren().add(locIconLabel);
            locationIcon.setAlignment(javafx.geometry.Pos.CENTER);

            VBox locInfo = new VBox(2);
            Label locTitle = new Label("Location");
            locTitle.getStyleClass().add("popup-location-title"); // ✅
            Label locValue = new Label(location);
            locValue.getStyleClass().add("popup-location-value"); // ✅
            locInfo.getChildren().addAll(locTitle, locValue);

            locationCard.getChildren().addAll(locationIcon, locInfo);
            body.getChildren().add(locationCard);
        }

        if (!description.equals("—")) {
            VBox descBox = new VBox(6);
            descBox.getStyleClass().add("popup-desc-box"); // ✅
            Label descTitle = new Label("Notes");
            descTitle.getStyleClass().add("popup-desc-title"); // ✅
            Label descValue = new Label(description);
            descValue.getStyleClass().add("popup-desc-value"); // ✅
            descValue.setWrapText(true);
            descBox.getChildren().addAll(descTitle, descValue);
            body.getChildren().add(descBox);
        }

        VBox btnBox = new VBox();
        btnBox.getStyleClass().add("popup-btn-box"); // ✅
        Button closeBtn = new Button("Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.getStyleClass().add("popup-close-btn"); // ✅
        closeBtn.setOnAction(ev -> popup.hide());
        btnBox.getChildren().add(closeBtn);
        body.getChildren().add(btnBox);

        content.getChildren().addAll(header, body);
        popup.getContent().add(content);

        javafx.stage.Window window = anchor.getScene().getWindow();
        double centerX = window.getX() + window.getWidth() / 2 - 160;
        double centerY = window.getY() + window.getHeight() / 2 - 160;
        popup.show(anchor, centerX, centerY);
    }

    private JsonNode currentAppointments = null;

    private void updatePatientList(JsonNode appointments) {
        patientListContainer.getChildren().clear();
        for (JsonNode appointment : appointments) {
            HBox item = createPatientListItem(appointment);
            patientListContainer.getChildren().add(item);
        }
    }


    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            // Показываем все приёмы
            if (currentAppointments != null) {
                updatePatientList(currentAppointments);
            }
            return;
        }

        // Фильтруем по имени пациента
        if (currentAppointments != null) {
            java.util.List<JsonNode> filtered = new java.util.ArrayList<>();
            for (JsonNode apt : currentAppointments) {
                String firstName = apt.get("patientFirstName").asText().toLowerCase();
                String lastName = apt.get("patientLastName").asText().toLowerCase();
                if (firstName.contains(query) || lastName.contains(query)) {
                    filtered.add(apt);
                }
            }
            patientListContainer.getChildren().clear();
            for (JsonNode apt : filtered) {
                patientListContainer.getChildren().add(createPatientListItem(apt));
            }
        }
    }

    private HBox createEventRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label l = new Label(label + ":");
        l.setStyle(
                "-fx-text-fill: #a0aec0; -fx-font-size: 12px; -fx-min-width: 65;"
        );
        Label v = new Label(value);
        v.setStyle(
                "-fx-text-fill: #2d3748; -fx-font-size: 12px; -fx-font-weight: bold;"
        );
        v.setWrapText(true);
        row.getChildren().addAll(l, v);
        return row;
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

    private Label createHomeCalCell(String text, boolean isWorking, boolean isToday, int aptCount, boolean isAdjacent) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setAlignment(Pos.CENTER);
        cell.setPrefHeight(26);
        cell.getStyleClass().add("cal-cell"); // Базовый класс

        if (isAdjacent) {
            cell.getStyleClass().add("cal-cell-adjacent");
        } else if (isToday) {
            cell.getStyleClass().add("cal-cell-today");
        } else if (isWorking) {
            if (aptCount >= 5) cell.getStyleClass().add("cal-cell-busy");
            else if (aptCount >= 3) cell.getStyleClass().add("cal-cell-medium");
            else if (aptCount > 0) cell.getStyleClass().add("cal-cell-light");
            else cell.getStyleClass().add("cal-cell-working");
        }
        return cell;
    }

    private void addCalendarFooter(JsonNode days) {
        int totalApts = 0;
        int totalSlots = 0;
        LocalDate today = LocalDate.now();

        for (JsonNode day : days) {
            LocalDate date = LocalDate.of(
                    today.getYear(), today.getMonthValue(),
                    day.get("day").asInt()
            );

            if (!date.isBefore(today)) {
                // Только будущие и сегодняшние
                totalApts += day.get("appointmentCount").asInt();
                if (day.get("workingDay").asBoolean()) {
                    totalSlots += 16;
                }
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

    private VBox createStatBox(String number, String label, String bg, String fg) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add(label.equals("booked") ? "stat-box-booked" : "stat-box-available");

        Label numLabel = new Label(number);
        numLabel.getStyleClass().add("stat-num");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-text");

        box.getChildren().addAll(numLabel, textLabel);
        return box;
    }

    private Timeline clockTimeline;

    private void startClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Получаем текущее время
            LocalTime now = LocalTime.now();

            // Используем наш универсальный форматтер!
            // Передаем LocalTime.toString() в наш formatTime
            String timeStr = FxUtils.formatTime(now.toString());

            if (clockLabel != null) {
                clockLabel.setText(timeStr);
            }
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private HBox createPatientListItem(JsonNode appointment) {
        String firstName = appointment.get("patientFirstName").asText();
        String lastName = appointment.get("patientLastName").asText();
        String reason = appointment.get("reason").asText();
        String time = FxUtils.formatTime(appointment.get("appointmentAt").asText());

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
        String time = FxUtils.formatTime(appointment.get("appointmentAt").asText());
        String notes = appointment.has("notes") ? appointment.get("notes").asText() : "No notes";


        consultationName.setText(firstName + " " + lastName);
        consultationInfo.setText("Appointment at " + time);
        consultationReason.setText(reason);
        consultationNotes.setText(notes);
    }

    @FXML
    private void handleClose() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                totalVisitsLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                totalVisitsLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                totalVisitsLabel.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
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
    private void handleLogout() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void navigateSettings() {
        FxUtils.navigateFullscreen(doctorNameLabel, "/fxml/settings.fxml");
    }

    @FXML
    private void navigateAppointments() {
        FxUtils.navigateFullscreen(doctorNameLabel, "/fxml/appointments.fxml");
    }
    @FXML
    private void navigateEvents() {
        FxUtils.navigateFullscreen(upcomingContainer, "/fxml/events.fxml");
    }
}