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

        Label themeLabel = new Label("Theme");
        themeLabel.getStyleClass().add("settings-field-label");

        HBox themeBox = new HBox(10);
        Button lightBtn = new Button("☀ Light");
        Button darkBtn = new Button("🌙 Dark");

        Runnable updateBtnStyles = () -> {
            lightBtn.getStyleClass().setAll(FxUtils.isDarkMode ? "theme-btn" : "theme-btn-active-light");
            darkBtn.getStyleClass().setAll(FxUtils.isDarkMode ? "theme-btn-active-dark" : "theme-btn");
        };
        updateBtnStyles.run();

        lightBtn.setOnAction(e -> {
            FxUtils.isDarkMode = false;
            FxUtils.applyTheme(settingsContent.getScene().getRoot());
            updateBtnStyles.run();
        });
        darkBtn.setOnAction(e -> {
            FxUtils.isDarkMode = true;
            FxUtils.applyTheme(settingsContent.getScene().getRoot());
            updateBtnStyles.run();
        });
        themeBox.getChildren().addAll(lightBtn, darkBtn);

        Label fontLabel = new Label("Font Size");
        fontLabel.getStyleClass().add("settings-field-label");

        HBox fontBox = new HBox(10);
        String[] sizes = {"Small", "Medium", "Large"};
        ToggleGroup fontGroup = new ToggleGroup();

        for (String size : sizes) {
            ToggleButton btn = new ToggleButton(size);
            btn.setToggleGroup(fontGroup);
            boolean isSelected = size.equals("Medium");
            btn.setSelected(isSelected);
            btn.getStyleClass().setAll(isSelected ? "font-size-btn-active" : "font-size-btn"); // ✅
            btn.selectedProperty().addListener((obs, wasSelected, nowSelected) -> {
                btn.getStyleClass().setAll(nowSelected ? "font-size-btn-active" : "font-size-btn");
            });
            fontBox.getChildren().add(btn);
        }

        Label notifLabel = new Label("Notifications");
        notifLabel.getStyleClass().add("settings-field-label");

        HBox notifBox = new HBox(10);
        notifBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox notifCheck = new CheckBox("Enable notifications");
        notifCheck.setSelected(true);
        notifBox.getChildren().add(notifCheck);

        settingsContent.getChildren().addAll(
                title, sep, themeLabel, themeBox, fontLabel, fontBox, notifLabel, notifBox
        );
    }

    // ── Schedule ────────────────────────────────────────
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
        info.getStyleClass().add("settings-spec-label"); // переиспользуем muted стиль
        info.setWrapText(true);

        Button goToSchedule = new Button("Open Schedule →");
        goToSchedule.getStyleClass().add("settings-save-btn");
        goToSchedule.setOnAction(e ->
                FxUtils.navigateFullscreen(settingsContent, "/fxml/schedule.fxml")
        );

        Label scheduleTitle = new Label("Current Schedule");
        scheduleTitle.getStyleClass().add("settings-field-label");

        String[][] schedule = {
                {"Monday",    "09:00 — 17:00"},
                {"Tuesday",   "09:00 — 17:00"},
                {"Wednesday", "09:00 — 17:00"},
                {"Thursday",  "09:00 — 17:00"},
                {"Friday",    "09:00 — 14:00"},
                {"Saturday",  "Day off"},
                {"Sunday",    "Day off"}
        };

        VBox scheduleBox = new VBox(8);
        for (String[] day : schedule) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("schedule-row"); // ✅

            Label dayLabel = new Label(day[0]);
            dayLabel.getStyleClass().add("schedule-day-label"); // ✅

            Label timeLabel = new Label(day[1]);
            timeLabel.getStyleClass().add(
                    day[1].equals("Day off") ? "schedule-time-off" : "schedule-time-working" // ✅
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