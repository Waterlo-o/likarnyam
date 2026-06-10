package com.example.likarnyam.controller;

import com.example.likarnyam.client.ApiClient;
import com.example.likarnyam.client.DoctorApiClient;
import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private VBox settingsContent;
    @FXML private Button btnProfile;
    @FXML private Button btnAppearance;
    @FXML private Button btnSchedule;
    @FXML private Button btnAccount;

    private JsonNode doctorData;

    @FXML
    public void initialize() {
        loadDoctorData();
    }

    private void loadDoctorData() {
        new Thread(() -> {
            try {
                JsonNode doctor = DoctorApiClient.getMe();
                Platform.runLater(() -> {
                    doctorData = doctor;
                    showProfile();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ── Profile ─────────────────────────────────────────
    @FXML
    public void showProfile() {
        setActiveNav(btnProfile);
        settingsContent.getChildren().clear();

        Label title = new Label("Profile");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER_LEFT);

        String initials = doctorData != null
                ? doctorData.get("firstName").asText().substring(0, 1) +
                doctorData.get("lastName").asText().substring(0, 1)
                : "DR";

        Label avatar = new Label(initials);
        avatar.getStyleClass().add("settings-avatar"); // ✅ класс вместо setStyle

        VBox avatarInfo = new VBox(4);
        String fullName = doctorData != null
                ? "Dr. " + doctorData.get("firstName").asText() + " " +
                doctorData.get("lastName").asText()
                : "Dr. Kim";
        String spec = doctorData != null &&
                !doctorData.get("specialization").asText().equals("null")
                ? doctorData.get("specialization").asText() : "—";

        Label nameLabel = new Label(fullName);
        nameLabel.getStyleClass().add("settings-name-label"); // ✅

        Label specLabel = new Label(spec);
        specLabel.getStyleClass().add("settings-spec-label"); // ✅

        avatarInfo.getChildren().addAll(nameLabel, specLabel);
        avatarBox.getChildren().addAll(avatar, avatarInfo);

        VBox fields = new VBox(15);
        VBox firstNameBox = createField("First Name",
                doctorData != null ? doctorData.get("firstName").asText() : "");
        VBox lastNameBox = createField("Last Name",
                doctorData != null ? doctorData.get("lastName").asText() : "");
        VBox phoneBox = createField("Phone",
                doctorData != null && !doctorData.get("phone").asText().equals("null")
                        ? doctorData.get("phone").asText() : "");
        VBox emailBox = createField("Email",
                doctorData != null ? doctorData.get("email").asText() : "");

        TextField emailField = (TextField) emailBox.getChildren().get(1);
        emailField.setDisable(true);

        fields.getChildren().addAll(firstNameBox, lastNameBox, phoneBox, emailBox);

        Label resultLabel = new Label("");

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("settings-save-btn");
        saveBtn.setOnAction(e -> {
            TextField fn = (TextField) firstNameBox.getChildren().get(1);
            TextField ln = (TextField) lastNameBox.getChildren().get(1);
            TextField ph = (TextField) phoneBox.getChildren().get(1);

            saveBtn.setDisable(true);
            saveBtn.setText("Saving...");

            new Thread(() -> {
                try {
                    JsonNode updated = DoctorApiClient.updateProfile(
                            fn.getText(), ln.getText(), ph.getText()
                    );
                    Platform.runLater(() -> {
                        doctorData = updated;
                        saveBtn.setText("Saved ✓");
                        saveBtn.getStyleClass().setAll("settings-save-btn-success"); // ✅
                        resultLabel.getStyleClass().setAll("settings-result-success"); // ✅
                        resultLabel.setText("Profile updated successfully");
                        nameLabel.setText("Dr. " + fn.getText() + " " + ln.getText());
                        avatar.setText(fn.getText().substring(0, 1) + ln.getText().substring(0, 1));
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save Changes");
                        resultLabel.getStyleClass().setAll("settings-result-error"); // ✅
                        resultLabel.setText("Failed to save");
                    });
                }
            }).start();
        });

        settingsContent.getChildren().addAll(title, sep, avatarBox, fields, resultLabel, saveBtn);
    }

    // ── Appearance ─────────────────────────────────────
    @FXML
    public void showAppearance() {
        setActiveNav(btnAppearance);
        settingsContent.getChildren().clear();

        Label title = new Label("Appearance");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        // --- 1. THEME ---
        Label themeLabel = new Label("Theme");
        themeLabel.getStyleClass().add("settings-field-label");

        HBox themeBox = new HBox(10);
        Button lightBtn = new Button("☀ Light");
        Button darkBtn = new Button("🌙 Dark");

        Runnable updateThemeStyles = () -> {
            boolean isDark = "DARK".equals(UserSession.getInstance().getTheme());
            lightBtn.getStyleClass().setAll(isDark ? "toggle-btn" : "toggle-btn-active");
            darkBtn.getStyleClass().setAll(isDark ? "toggle-btn-active" : "toggle-btn");
        };
        updateThemeStyles.run();

        lightBtn.setOnAction(e -> {
            UserSession.getInstance().setTheme("LIGHT");
            FxUtils.isDarkMode = false;
            FxUtils.applyTheme(settingsContent.getScene().getRoot());
            updateThemeStyles.run();
            saveAppearanceSettings();
        });

        darkBtn.setOnAction(e -> {
            UserSession.getInstance().setTheme("DARK");
            FxUtils.isDarkMode = true;
            FxUtils.applyTheme(settingsContent.getScene().getRoot());
            updateThemeStyles.run();
            saveAppearanceSettings();
        });
        themeBox.getChildren().addAll(lightBtn, darkBtn);

        // --- 2. TIME FORMAT ---
        Label timeLabel = new Label("Time Format");
        timeLabel.getStyleClass().add("settings-field-label");

        HBox timeBox = new HBox(0);
        timeBox.getStyleClass().add("switcher-box");
        timeBox.setMaxWidth(Region.USE_PREF_SIZE);

        ToggleButton btn24h = new ToggleButton("24-hour (14:30)");
        ToggleButton btn12h = new ToggleButton("12-hour (02:30 PM)");

        HBox.setHgrow(btn24h, Priority.ALWAYS);
        HBox.setHgrow(btn12h, Priority.ALWAYS);

        btn24h.setPrefWidth(140);
        btn12h.setPrefWidth(140);

        ToggleGroup timeGroup = new ToggleGroup();
        btn24h.setToggleGroup(timeGroup);
        btn12h.setToggleGroup(timeGroup);

        btn24h.getStyleClass().add("switcher-btn-left");
        btn12h.getStyleClass().add("switcher-btn-right");

        if ("12h".equals(UserSession.getInstance().getTimeFormat())) {
            btn12h.setSelected(true);
        } else {
            btn24h.setSelected(true);
        }

        timeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
                return;
            }
            String format = newVal == btn12h ? "12h" : "24h";
            UserSession.getInstance().setTimeFormat(format);
            saveAppearanceSettings();
        });
        timeBox.getChildren().addAll(btn24h, btn12h);

        // --- 3. ANIMATIONS (Custom Switcher) ---
        Label animLabel = new Label("Animations");
        animLabel.getStyleClass().add("settings-field-label");

        HBox animBox = new HBox(15);
        animBox.setAlignment(Pos.CENTER_LEFT);

        Label animDesc = new Label("Enable UI transitions and animations");
        animDesc.getStyleClass().add("settings-desc-label");

        Pane switchBg = new Pane();
        switchBg.setPrefSize(44, 24);
        switchBg.setStyle("-fx-cursor: hand;");

        javafx.scene.shape.Circle switchDot = new javafx.scene.shape.Circle(10);
        switchDot.getStyleClass().add("switch-dot");
        switchDot.setFill(javafx.scene.paint.Color.WHITE);

        // ФИКСИРУЕМ базовую позицию (положение "Выкл")
        switchDot.setLayoutY(12);
        switchDot.setLayoutX(12);
        switchDot.setMouseTransparent(true);

        // 1. Устанавливаем начальное состояние (БЕЗ анимации, чтобы при открытии настроек тумблер уже стоял правильно)
        boolean initialOn = UserSession.getInstance().isAnimationsEnabled();
        switchDot.setTranslateX(initialOn ? 20 : 0); // 20 - это дистанция сдвига вправо
        switchBg.getStyleClass().setAll("switch-bg", initialOn ? "switch-bg-on" : "switch-bg-off");

        // 2. Обработка клика и плавная анимация
        switchBg.setOnMouseClicked(e -> {
            boolean current = UserSession.getInstance().isAnimationsEnabled();
            boolean newState = !current;
            UserSession.getInstance().setAnimationsEnabled(newState);

            // Меняем цвет фона мгновенно
            switchBg.getStyleClass().setAll("switch-bg", newState ? "switch-bg-on" : "switch-bg-off");

            // Запускаем плавный сдвиг кружочка
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(250), switchDot
            );
            tt.setToX(newState ? 20 : 0); // Двигаем на +20px вправо или возвращаем на 0
            tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH); // Плавный старт и торможение
            tt.play();

            saveAppearanceSettings();
        });

        switchBg.getChildren().add(switchDot);
        animBox.getChildren().addAll(switchBg, animDesc);

        // Добавляем все элементы на экран
        settingsContent.getChildren().addAll(
                title, sep,
                themeLabel, themeBox,
                timeLabel, timeBox,
                animLabel, animBox
        );
    }

    // Вспомогательный метод для сохранения настроек
    private void saveAppearanceSettings() {
        String theme = UserSession.getInstance().getTheme();
        String timeFormat = UserSession.getInstance().getTimeFormat();
        boolean animEnabled = UserSession.getInstance().isAnimationsEnabled();

        new Thread(() -> {
            try {
                DoctorApiClient.updateAppearance(theme, timeFormat, animEnabled);
            } catch (Exception ex) {
                ex.printStackTrace();
                // Можно добавить логику отображения ошибки сети
            }
        }).start();
    }

    // ── Schedule ────────────────────────────────────────
    @FXML
    public void showSchedule() {
        setActiveNav(btnSchedule);
        settingsContent.getChildren().clear();

        boolean isAdmin = UserSession.getInstance().isAdmin();

        if (isAdmin) {
            showAdminSchedule();
        } else {
            showDoctorSchedule();
        }
    }

    private void showDoctorSchedule() {
        Label title = new Label("Working Hours");
        title.getStyleClass().add("settings-section-title");
        settingsContent.getChildren().add(title);
        settingsContent.getChildren().add(new Separator());

        // Текущее расписание
        Label scheduleTitle = new Label("Current Schedule");
        scheduleTitle.getStyleClass().add("settings-field-label");

        VBox scheduleBox = new VBox(8);
        scheduleBox.setId("scheduleBox");

        // Загружаем реальное расписание
        new Thread(() -> {
            try {
                JsonNode schedules = com.example.likarnyam.client.ScheduleApiClient.getMySchedule();
                javafx.application.Platform.runLater(() -> {
                    VBox box = (VBox) settingsContent.lookup("#scheduleBox");
                    if (box == null) return;
                    box.getChildren().clear();

                    String[] dayNames = {"", "Monday", "Tuesday", "Wednesday",
                            "Thursday", "Friday", "Saturday", "Sunday"};

                    // Создаём map dayOfWeek -> schedule
                    java.util.Map<Integer, JsonNode> schedMap = new java.util.LinkedHashMap<>();
                    for (JsonNode s : schedules) {
                        schedMap.put(s.get("dayOfWeek").asInt(), s);
                    }

                    for (int i = 1; i <= 7; i++) {
                        HBox row = new HBox();
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.getStyleClass().add("schedule-row");

                        Label dayLabel = new Label(dayNames[i]);
                        dayLabel.getStyleClass().add("schedule-day-label");

                        String timeStr;
                        if (schedMap.containsKey(i)) {
                            JsonNode s = schedMap.get(i);
                            timeStr = s.get("startTime").asText().substring(0, 5) +
                                    " — " + s.get("endTime").asText().substring(0, 5);
                        } else {
                            timeStr = "Day off";
                        }

                        Label timeLabel = new Label(timeStr);
                        timeLabel.getStyleClass().add(
                                timeStr.equals("Day off") ? "schedule-time-off" : "schedule-time-working"
                        );

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        row.getChildren().addAll(dayLabel, spacer, timeLabel);
                        box.getChildren().add(row);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Форма запроса
        Label requestTitle = new Label("Request Schedule Change");
        requestTitle.getStyleClass().add("settings-field-label");

        // Переключатель типа запроса
        HBox typeBox = new HBox(8);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup typeGroup = new ToggleGroup();

        ToggleButton changeBtn = new ToggleButton("📅 Schedule Change");
        ToggleButton dayOffBtn = new ToggleButton("🏖 Day Off");

        changeBtn.setToggleGroup(typeGroup);
        dayOffBtn.setToggleGroup(typeGroup);
        changeBtn.setSelected(true);
        changeBtn.getStyleClass().add("toggle-btn-active");
        dayOffBtn.getStyleClass().add("toggle-btn");
        typeBox.getChildren().addAll(changeBtn, dayOffBtn);

        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
                return;
            }
            changeBtn.getStyleClass().setAll(newVal == changeBtn ? "toggle-btn-active" : "toggle-btn");
            dayOffBtn.getStyleClass().setAll(newVal == dayOffBtn ? "toggle-btn-active" : "toggle-btn");
        });

        // Поля для CHANGE
        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"
        );
        dayCombo.setPromptText("Select day...");
        dayCombo.getStyleClass().add("settings-input");
        dayCombo.setMaxWidth(Double.MAX_VALUE);

        HBox timeRow = new HBox(10);
        TextField startField = new TextField();
        startField.setPromptText("New start time (09:00)");
        startField.getStyleClass().add("settings-input");

        TextField endField = new TextField();
        endField.setPromptText("New end time (17:00)");
        endField.getStyleClass().add("settings-input");

        HBox.setHgrow(startField, Priority.ALWAYS);
        HBox.setHgrow(endField, Priority.ALWAYS);
        timeRow.getChildren().addAll(startField, endField);

        // Поля для DAY_OFF
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("Select date...");
        datePicker.getStyleClass().add("settings-input");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setVisible(false);
        datePicker.setManaged(false);

        // Переключение видимости полей
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            boolean isDayOff = newVal == dayOffBtn;

            dayCombo.setVisible(!isDayOff);
            dayCombo.setManaged(!isDayOff);

            timeRow.setVisible(!isDayOff);
            timeRow.setManaged(!isDayOff);

            datePicker.setVisible(isDayOff);
            datePicker.setManaged(isDayOff);
        });

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason...");
        reasonArea.setPrefHeight(70);
        reasonArea.getStyleClass().add("settings-input");

        Label requestResult = new Label("");

        Button submitBtn = new Button("Submit Request");
        submitBtn.getStyleClass().add("settings-save-btn");
        submitBtn.setOnAction(e -> {
            boolean isDayOff = typeGroup.getSelectedToggle() == dayOffBtn;

            if (isDayOff) {
                if (datePicker.getValue() == null) {
                    requestResult.getStyleClass().setAll("settings-result-error");
                    requestResult.setText("Please select a date");
                    return;
                }
            } else {
                if (dayCombo.getValue() == null) {
                    requestResult.getStyleClass().setAll("settings-result-error");
                    requestResult.setText("Please select a day");
                    return;
                }
            }

            submitBtn.setDisable(true);
            submitBtn.setText("Submitting...");

            String reason = reasonArea.getText().trim().isEmpty() ? null : reasonArea.getText().trim();

            new Thread(() -> {
                try {
                    if (isDayOff) {
                        com.example.likarnyam.client.ScheduleRequestApiClient.createDayOffRequest(
                                datePicker.getValue().toString(), reason);
                    } else {
                        int dayOfWeek = java.util.List.of(
                                "", "Monday", "Tuesday", "Wednesday",
                                "Thursday", "Friday", "Saturday", "Sunday"
                        ).indexOf(dayCombo.getValue());

                        String start = startField.getText().trim().isEmpty()
                                ? null : startField.getText().trim();
                        String end = endField.getText().trim().isEmpty()
                                ? null : endField.getText().trim();

                        com.example.likarnyam.client.ScheduleRequestApiClient
                                .createRequest(dayOfWeek, start, end, reason);
                    }

                    javafx.application.Platform.runLater(() -> {
                        requestResult.getStyleClass().setAll("settings-result-success");
                        requestResult.setText("Request submitted ✓");
                        submitBtn.setDisable(false);
                        submitBtn.setText("Submit Request");

                        dayCombo.setValue(null);
                        startField.clear();
                        endField.clear();
                        datePicker.setValue(null);
                        reasonArea.clear();

                        loadMyRequests(settingsContent);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        requestResult.getStyleClass().setAll("settings-result-error");
                        requestResult.setText("Failed: " + ex.getMessage());
                        submitBtn.setDisable(false);
                        submitBtn.setText("Submit Request");
                    });
                }
            }).start();
        });

        // Мои запросы
        Label myRequestsTitle = new Label("My Requests");
        myRequestsTitle.getStyleClass().add("settings-field-label");

        VBox myRequestsBox = new VBox(8);
        myRequestsBox.setId("myRequestsBox");

        settingsContent.getChildren().addAll(
                scheduleTitle, scheduleBox,
                new Separator(),
                requestTitle, typeBox, dayCombo, timeRow, datePicker,
                reasonArea, requestResult, submitBtn,
                new Separator(),
                myRequestsTitle, myRequestsBox
        );

        loadMyRequests(settingsContent);
    }

    private void loadMyRequests(VBox container) {
        new Thread(() -> {
            try {
                JsonNode requests = com.example.likarnyam.client.ScheduleRequestApiClient
                        .getMyRequests();
                javafx.application.Platform.runLater(() -> {
                    VBox box = (VBox) container.lookup("#myRequestsBox");
                    if (box == null) return;
                    box.getChildren().clear();

                    if (requests.size() == 0) {
                        Label none = new Label("No requests yet");
                        none.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
                        box.getChildren().add(none);
                        return;
                    }

                    for (JsonNode req : requests) {
                        String status = req.get("status").asText();
                        String statusColor = switch (status) {
                            case "APPROVED" -> "#38a169";
                            case "REJECTED" -> "#e53e3e";
                            default         -> "#d69e2e";
                        };

                        boolean isDark = UserSession.getInstance().isAdmin();
                        String rowBg = isDark ? "#f7fafc" : switch (status) {
                            case "APPROVED" -> "#f0fff4";
                            case "REJECTED" -> "#fff5f5";
                            default         -> "#fffff0";
                        };

                        HBox row = new HBox(10);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle(
                                "-fx-background-color: " + rowBg + ";" +
                                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                                        "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;"
                        );

                        VBox info = new VBox(3);
                        String requestType = req.has("requestType")
                                ? req.get("requestType").asText() : "CHANGE";
                        String dayInfo = "DAY_OFF".equals(requestType)
                                ? (req.has("requestedDate") && !req.get("requestedDate").isNull()
                                ? "🏖 Day Off · " + req.get("requestedDate").asText()
                                : "🏖 Day Off")
                                : req.get("dayName").asText();
                        Label dayLabel = new Label(dayInfo);
                        dayLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
                        info.getChildren().add(dayLabel);

                        if (!req.get("requestedStart").isNull() ||
                                !req.get("requestedEnd").isNull()) {
                            String timeStr = "";
                            if (!req.get("requestedStart").isNull())
                                timeStr += req.get("requestedStart").asText();
                            if (!req.get("requestedEnd").isNull())
                                timeStr += " — " + req.get("requestedEnd").asText();
                            Label timeLabel = new Label(timeStr);
                            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
                            info.getChildren().add(timeLabel);
                        }

                        if (req.has("reason") && !req.get("reason").isNull()) {
                            Label reasonLabel = new Label(req.get("reason").asText());
                            reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
                            reasonLabel.setWrapText(true);
                            info.getChildren().add(reasonLabel);
                        }

                        Label statusBadge = new Label(status);
                        statusBadge.setStyle(
                                "-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;" +
                                        "-fx-font-size: 11px; -fx-background-color: " + statusColor + "22;" +
                                        "-fx-background-radius: 10; -fx-padding: 3 8;"
                        );

                        // Кнопка Clear — только для APPROVED и REJECTED
                        Button clearBtn = new Button("✕");
                        clearBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #a0aec0;" +
                                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 6;"
                        );
                        clearBtn.setOnMouseEntered(e -> clearBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #e53e3e;" +
                                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 6;"
                        ));
                        clearBtn.setOnMouseExited(e -> clearBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #a0aec0;" +
                                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 6;"
                        ));
                        clearBtn.setVisible("APPROVED".equals(status) || "REJECTED".equals(status));
                        clearBtn.setManaged("APPROVED".equals(status) || "REJECTED".equals(status));
                        Long requestId = req.has("id") ? req.get("id").asLong() : null;

                        clearBtn.setOnAction(e -> {
                            box.getChildren().remove(row);
                            if (requestId != null) {
                                new Thread(() -> {
                                    try {
                                        com.example.likarnyam.client.ScheduleRequestApiClient.hideRequestForDoctor(requestId);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }).start();
                            }
                        });

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        row.getChildren().addAll(info, spacer, statusBadge, clearBtn);
                        box.getChildren().add(row);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void showAdminSchedule() {
        Label title = new Label("Schedule Requests");
        title.getStyleClass().add("settings-section-title");
        settingsContent.getChildren().addAll(title, new Separator());

        Label subtitle = new Label("Pending requests from doctors");
        subtitle.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
        settingsContent.getChildren().add(subtitle);

        VBox requestsBox = new VBox(10);
        requestsBox.setId("adminRequestsBox");
        settingsContent.getChildren().add(requestsBox);

        loadAdminRequests();
    }

    private void loadAdminRequests() {
        new Thread(() -> {
            try {
                JsonNode requests = com.example.likarnyam.client.ScheduleRequestApiClient
                        .getAllRequests();
                javafx.application.Platform.runLater(() -> {
                    VBox box = (VBox) settingsContent.lookup("#adminRequestsBox");
                    if (box == null) return;
                    box.getChildren().clear();

                    if (requests.size() == 0) {
                        Label none = new Label("No pending requests");
                        none.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
                        box.getChildren().add(none);
                        return;
                    }

                    for (JsonNode req : requests) {
                        String status = req.get("status").asText();
                        Long reqId = req.get("id").asLong();

                        VBox card = new VBox(8);
                        card.setStyle(
                                "-fx-background-color: #f7fafc; -fx-border-radius: 10;" +
                                        "-fx-background-radius: 10; -fx-border-color: #e2e8f0;" +
                                        "-fx-border-width: 1; -fx-padding: 12;"
                        );

                        Label doctorLabel = new Label("Dr. " + req.get("doctorName").asText());
                        doctorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                        String dayInfo = "DAY_OFF".equals(req.has("requestType")
                                ? req.get("requestType").asText() : "CHANGE")
                                ? (req.has("requestedDate") && !req.get("requestedDate").isNull()
                                ? "Day Off · " + req.get("requestedDate").asText() : "Day Off")
                                : req.get("dayName").asText();
                        Label dayLabel = new Label(dayInfo);
                        dayLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

                        String timeStr = "";
                        if (!req.get("requestedStart").isNull())
                            timeStr += req.get("requestedStart").asText();
                        if (!req.get("requestedEnd").isNull())
                            timeStr += " — " + req.get("requestedEnd").asText();

                        HBox infoRow = new HBox(10, doctorLabel, dayLabel);
                        if (!timeStr.isEmpty()) {
                            Label timeLabel = new Label(timeStr);
                            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64B5F6;");
                            infoRow.getChildren().add(timeLabel);
                        }

                        if (req.has("requestedDate") && !req.get("requestedDate").isNull()) {
                            Label dateLabel2 = new Label("📅 " + req.get("requestedDate").asText());
                            dateLabel2.setStyle("-fx-font-size: 12px; -fx-text-fill: #38a169; -fx-font-weight: bold;");
                            card.getChildren().add(dateLabel2);

                            // Предупреждение о существующих записях
                            if (req.has("existingAppointments") && req.get("existingAppointments").asLong() > 0) {
                                long count = req.get("existingAppointments").asLong();
                                Label warningLabel = new Label("⚠ " + count + " scheduled appointment"
                                        + (count > 1 ? "s" : "") + " on this day");
                                warningLabel.setStyle(
                                        "-fx-font-size: 11px; -fx-text-fill: #d69e2e; -fx-font-weight: bold;" +
                                                "-fx-background-color: #fefcbf; -fx-background-radius: 6;" +
                                                "-fx-padding: 3 8; -fx-border-radius: 6;"
                                );
                                card.getChildren().add(warningLabel);
                            }
                        }

                        if (!req.get("reason").isNull()) {
                            Label reasonLabel = new Label("\"" + req.get("reason").asText() + "\"");
                            reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
                            reasonLabel.setWrapText(true);
                            card.getChildren().add(reasonLabel);
                        }

                        card.getChildren().add(0, infoRow);

                        if ("PENDING".equals(status)) {
                            TextField commentField = new TextField();
                            commentField.setPromptText("Comment (optional)...");
                            commentField.getStyleClass().add("settings-input");

                            Button approveBtn = new Button("✓ Approve");
                            approveBtn.setStyle(
                                    "-fx-background-color: #38a169; -fx-text-fill: white;" +
                                            "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
                            );

                            Button rejectBtn = new Button("✕ Reject");
                            rejectBtn.setStyle(
                                    "-fx-background-color: #e53e3e; -fx-text-fill: white;" +
                                            "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
                            );

                            approveBtn.setOnAction(e -> reviewRequest(
                                    reqId, "APPROVED",
                                    commentField.getText().trim(), box));
                            rejectBtn.setOnAction(e -> reviewRequest(
                                    reqId, "REJECTED",
                                    commentField.getText().trim(), box));

                            HBox btnRow = new HBox(8, approveBtn, rejectBtn);
                            card.getChildren().addAll(commentField, btnRow);
                        } else {
                            String statusColor = "APPROVED".equals(status) ? "#38a169" : "#e53e3e";
                            Label statusLabel = new Label(status);
                            statusLabel.setStyle(
                                    "-fx-text-fill: " + statusColor + "; -fx-font-weight: bold; -fx-font-size: 11px;"
                            );

                            HBox statusRow = new HBox(8, statusLabel);
                            card.getChildren().add(statusRow);
                        }

                        box.getChildren().add(card);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void reviewRequest(Long id, String status, String comment, VBox box) {
        new Thread(() -> {
            try {
                com.example.likarnyam.client.ScheduleRequestApiClient
                        .reviewRequest(id, status, comment.isEmpty() ? null : comment);
                javafx.application.Platform.runLater(this::loadAdminRequests);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ── Account ─────────────────────────────────────────
    @FXML
    public void showAccount() {
        setActiveNav(btnAccount);
        settingsContent.getChildren().clear();

        Label title = new Label("Account");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        Label passTitle = new Label("Change Password");
        passTitle.getStyleClass().add("settings-field-label");

        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current password");
        currentPass.getStyleClass().add("settings-input");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");
        newPass.getStyleClass().add("settings-input");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        confirmPass.getStyleClass().add("settings-input");

        Label passResult = new Label("");

        Button changePassBtn = new Button("Change Password");
        changePassBtn.getStyleClass().add("settings-save-btn");

        changePassBtn.setOnAction(e -> {
            String current = currentPass.getText();
            String newP = newPass.getText();
            String confirm = confirmPass.getText();

            if (current.isEmpty() || newP.isEmpty()) {
                passResult.getStyleClass().setAll("settings-result-error"); // ✅
                passResult.setText("Please fill all fields");
                return;
            }
            if (!newP.equals(confirm)) {
                passResult.getStyleClass().setAll("settings-result-error");
                passResult.setText("Passwords don't match");
                return;
            }
            if (newP.length() < 6) {
                passResult.getStyleClass().setAll("settings-result-error");
                passResult.setText("Password must be at least 6 characters");
                return;
            }

            changePassBtn.setDisable(true);
            changePassBtn.setText("Saving...");

            new Thread(() -> {
                try {
                    ApiClient.changePassword(current, newP);
                    Platform.runLater(() -> {
                        passResult.getStyleClass().setAll("settings-result-success"); // ✅
                        passResult.setText("Password changed successfully ✓");
                        changePassBtn.setText("Change Password");
                        changePassBtn.setDisable(false);
                        currentPass.clear(); newPass.clear(); confirmPass.clear();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        passResult.getStyleClass().setAll("settings-result-error");
                        passResult.setText("Invalid current password");
                        changePassBtn.setText("Change Password");
                        changePassBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Separator sep2 = new Separator();

        Label dangerTitle = new Label("Danger Zone");
        dangerTitle.getStyleClass().add("danger-title"); // ✅

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-logout"); // ✅
        logoutBtn.setOnAction(e -> {
            UserSession.getInstance().logout();
            Stage stage = (Stage) settingsContent.getScene().getWindow();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(root);

                stage.setScene(scene);
                stage.setFullScreen(false); // Или true, если хочешь полный эксклюзив
                stage.centerOnScreen();
                stage.show();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        settingsContent.getChildren().addAll(
                title, sep,
                passTitle, currentPass, newPass, confirmPass,
                passResult, changePassBtn,
                sep2, dangerTitle, logoutBtn
        );
    }


    // Вспомогательные методы
    private VBox createField(String label, String value) {
        VBox box = new VBox(6);
        Label l = new Label(label);
        l.getStyleClass().add("settings-field-label");
        TextField field = new TextField(value);
        field.getStyleClass().add("settings-input");
        box.getChildren().addAll(l, field);
        return box;
    }

    private void setActiveNav(Button active) {
        btnProfile.getStyleClass().setAll("settings-nav");
        btnAppearance.getStyleClass().setAll("settings-nav");
        btnSchedule.getStyleClass().setAll("settings-nav");
        btnAccount.getStyleClass().setAll("settings-nav");
        active.getStyleClass().setAll("settings-nav-active");
    }


    @FXML
    private void handleClose() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                settingsContent.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                settingsContent.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        javafx.stage.Stage stage = (javafx.stage.Stage)
                settingsContent.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }


    // Навигация
    @FXML private void navigateHome() {
        FxUtils.navigateFullscreen(settingsContent, "/fxml/home.fxml");
    }
    @FXML private void navigatePatients() {
        FxUtils.navigateFullscreen(settingsContent, "/fxml/patient-list.fxml");
    }
    @FXML private void navigateSchedule() {
        FxUtils.navigateFullscreen(settingsContent, "/fxml/schedule.fxml");
    }
    @FXML private void navigateAppointments() {
        FxUtils.navigateFullscreen(settingsContent, "/fxml/appointments.fxml");
    }
    @FXML private void navigateSettings() { }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}