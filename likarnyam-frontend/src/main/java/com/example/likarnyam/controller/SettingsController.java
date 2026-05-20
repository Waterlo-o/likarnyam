package com.example.likarnyam.controller;

import com.example.likarnyam.client.ApiClient;
import com.example.likarnyam.client.DoctorApiClient;
import com.example.likarnyam.session.UserSession;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

    @FXML
    public void showProfile() {
        setActiveNav(btnProfile);
        settingsContent.getChildren().clear();

        Label title = new Label("Profile");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        // Аватар
        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER_LEFT);

        String initials = doctorData != null
                ? doctorData.get("firstName").asText().substring(0, 1) +
                doctorData.get("lastName").asText().substring(0, 1)
                : "DR";

        Label avatar = new Label(initials);
        avatar.setStyle(
                "-fx-background-color: #64B5F6;" +
                        "-fx-background-radius: 40;" +
                        "-fx-min-width: 80; -fx-min-height: 80;" +
                        "-fx-max-width: 80; -fx-max-height: 80;" +
                        "-fx-alignment: center;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        VBox avatarInfo = new VBox(4);
        String fullName = doctorData != null
                ? "Dr. " + doctorData.get("firstName").asText() + " " +
                doctorData.get("lastName").asText()
                : "Dr. Kim";
        String spec = doctorData != null &&
                !doctorData.get("specialization").asText().equals("null")
                ? doctorData.get("specialization").asText() : "—";

        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label specLabel = new Label(spec);
        specLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        avatarInfo.getChildren().addAll(nameLabel, specLabel);
        avatarBox.getChildren().addAll(avatar, avatarInfo);

        // Поля
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

        // Делаем email нередактируемым
        TextField emailField = (TextField) emailBox.getChildren().get(1);
        emailField.setDisable(true);

        fields.getChildren().addAll(firstNameBox, lastNameBox, phoneBox, emailBox);

        // Результат
        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 12px;");

        // Кнопка сохранить
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
                        saveBtn.setStyle(
                                "-fx-background-color: #38a169;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-padding: 10 30 10 30;" +
                                        "-fx-cursor: hand;"
                        );
                        resultLabel.setText("Profile updated successfully");
                        resultLabel.setStyle("-fx-text-fill: #38a169;");

                        // Обновляем аватар и имя
                        nameLabel.setText("Dr. " + fn.getText() + " " + ln.getText());
                        avatar.setText(
                                fn.getText().substring(0, 1) + ln.getText().substring(0, 1)
                        );
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save Changes");
                        resultLabel.setText("Failed to save");
                        resultLabel.setStyle("-fx-text-fill: #e53e3e;");
                    });
                }
            }).start();
        });

        settingsContent.getChildren().addAll(
                title, sep, avatarBox, fields, resultLabel, saveBtn
        );
    }

    // ── Appearance ─────────────────────────────────────
    @FXML
    public void showAppearance() {
        setActiveNav(btnAppearance);
        settingsContent.getChildren().clear();

        Label title = new Label("Appearance");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        // Тема
        Label themeLabel = new Label("Theme");
        themeLabel.getStyleClass().add("settings-field-label");

        HBox themeBox = new HBox(10);
        Button lightBtn = new Button("☀ Light");
        Button darkBtn = new Button("🌙 Dark");

        lightBtn.setStyle(
                "-fx-background-color: #64B5F6; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-padding: 8 20 8 20;" +
                        "-fx-font-weight: bold; -fx-cursor: hand;"
        );
        darkBtn.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-padding: 8 20 8 20; -fx-cursor: hand;"
        );

        lightBtn.setOnAction(e -> {
            lightBtn.setStyle(
                    "-fx-background-color: #64B5F6; -fx-text-fill: white;" +
                            "-fx-background-radius: 10; -fx-padding: 8 20 8 20;" +
                            "-fx-font-weight: bold; -fx-cursor: hand;"
            );
            darkBtn.setStyle(
                    "-fx-background-color: white; -fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 10; -fx-background-radius: 10;" +
                            "-fx-padding: 8 20 8 20; -fx-cursor: hand;"
            );
        });

        darkBtn.setOnAction(e -> {
            darkBtn.setStyle(
                    "-fx-background-color: #2d3748; -fx-text-fill: white;" +
                            "-fx-background-radius: 10; -fx-padding: 8 20 8 20;" +
                            "-fx-font-weight: bold; -fx-cursor: hand;"
            );
            lightBtn.setStyle(
                    "-fx-background-color: white; -fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 10; -fx-background-radius: 10;" +
                            "-fx-padding: 8 20 8 20; -fx-cursor: hand;"
            );
        });

        themeBox.getChildren().addAll(lightBtn, darkBtn);

        // Размер шрифта
        Label fontLabel = new Label("Font Size");
        fontLabel.getStyleClass().add("settings-field-label");

        HBox fontBox = new HBox(10);
        String[] sizes = {"Small", "Medium", "Large"};
        ToggleGroup fontGroup = new ToggleGroup();

        for (String size : sizes) {
            ToggleButton btn = new ToggleButton(size);
            btn.setToggleGroup(fontGroup);
            btn.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 6 16 6 16;" +
                            "-fx-cursor: hand;"
            );
            if (size.equals("Medium")) {
                btn.setSelected(true);
                btn.setStyle(
                        "-fx-background-color: #64B5F6;" +
                                "-fx-text-fill: white;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 6 16 6 16;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                );
            }
            fontBox.getChildren().add(btn);
        }

        // Уведомления
        Label notifLabel = new Label("Notifications");
        notifLabel.getStyleClass().add("settings-field-label");

        HBox notifBox = new HBox(10);
        notifBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox notifCheck = new CheckBox("Enable notifications");
        notifCheck.setSelected(true);
        notifCheck.setStyle("-fx-font-size: 13px;");
        notifBox.getChildren().add(notifCheck);

        Label comingSoon = new Label("Dark theme — coming in next update");
        comingSoon.setStyle(
                "-fx-text-fill: #a0aec0; -fx-font-size: 12px; -fx-font-style: italic;"
        );

        settingsContent.getChildren().addAll(
                title, sep, themeLabel, themeBox,
                fontLabel, fontBox,
                notifLabel, notifBox,
                comingSoon
        );
    }

    // ── Working Hours ───────────────────────────────────
    @FXML
    public void showSchedule() {
        setActiveNav(btnSchedule);
        settingsContent.getChildren().clear();

        Label title = new Label("Working Hours");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        Label info = new Label(
                "Your current schedule is set in the Schedule screen.\n" +
                        "Click below to manage your working hours."
        );
        info.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px;");
        info.setWrapText(true);

        Button goToSchedule = new Button("Open Schedule →");
        goToSchedule.getStyleClass().add("settings-save-btn");
        goToSchedule.setOnAction(e ->
                FxUtils.navigateFullscreen(settingsContent, "/fxml/schedule.fxml")
        );

        // Текущее расписание
        Label scheduleTitle = new Label("Current Schedule");
        scheduleTitle.getStyleClass().add("settings-field-label");

        String[][] schedule = {
                {"Monday", "09:00 — 17:00"},
                {"Tuesday", "09:00 — 17:00"},
                {"Wednesday", "09:00 — 17:00"},
                {"Thursday", "09:00 — 17:00"},
                {"Friday", "09:00 — 14:00"},
                {"Saturday", "Day off"},
                {"Sunday", "Day off"}
        };

        VBox scheduleBox = new VBox(8);
        for (String[] day : schedule) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: #f7fafc;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 12 8 12;"
            );
            Label dayLabel = new Label(day[0]);
            dayLabel.setStyle(
                    "-fx-font-weight: bold; -fx-min-width: 100; -fx-font-size: 13px;"
            );
            Label timeLabel = new Label(day[1]);
            timeLabel.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: " +
                            (day[1].equals("Day off") ? "#a0aec0" : "#276749") + ";"
            );
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(dayLabel, spacer, timeLabel);
            scheduleBox.getChildren().add(row);
        }

        settingsContent.getChildren().addAll(
                title, sep, info, goToSchedule, scheduleTitle, scheduleBox
        );
    }

    // ── Account ─────────────────────────────────────────
    @FXML
    public void showAccount() {
        setActiveNav(btnAccount);
        settingsContent.getChildren().clear();

        Label title = new Label("Account");
        title.getStyleClass().add("settings-section-title");

        Separator sep = new Separator();

        // Смена пароля
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
        passResult.setStyle("-fx-font-size: 12px;");

        Button changePassBtn = new Button("Change Password");
        changePassBtn.getStyleClass().add("settings-save-btn");

        changePassBtn.setOnAction(e -> {
            String current = currentPass.getText();
            String newP = newPass.getText();
            String confirm = confirmPass.getText();

            if (current.isEmpty() || newP.isEmpty()) {
                passResult.setText("Please fill all fields");
                passResult.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
                return;
            }

            if (!newP.equals(confirm)) {
                passResult.setText("Passwords don't match");
                passResult.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
                return;
            }

            if (newP.length() < 6) {
                passResult.setText("Password must be at least 6 characters");
                passResult.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
                return;
            }

            changePassBtn.setDisable(true);
            changePassBtn.setText("Saving...");

            new Thread(() -> {
                try {
                    ApiClient.changePassword(current, newP);
                    Platform.runLater(() -> {
                        passResult.setText("Password changed successfully ✓");
                        passResult.setStyle(
                                "-fx-text-fill: #38a169; -fx-font-size: 12px;"
                        );
                        changePassBtn.setText("Change Password");
                        changePassBtn.setDisable(false);
                        currentPass.clear();
                        newPass.clear();
                        confirmPass.clear();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        passResult.setText("Invalid current password");
                        passResult.setStyle(
                                "-fx-text-fill: #e53e3e; -fx-font-size: 12px;"
                        );
                        changePassBtn.setText("Change Password");
                        changePassBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Separator sep2 = new Separator();

        // Danger zone
        Label dangerTitle = new Label("Danger Zone");
        dangerTitle.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c53030;"
        );

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle(
                "-fx-background-color: #fed7d7;" +
                        "-fx-text-fill: #c53030;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 30 10 30;" +
                        "-fx-cursor: hand;"
        );

        logoutBtn.setOnAction(e -> {
            UserSession.getInstance().logout();
            FxUtils.navigate(settingsContent, "/fxml/login.fxml", 800, 500);
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