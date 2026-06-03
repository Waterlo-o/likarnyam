package com.example.likarnyam.controller;

import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.DiseaseApiClient;
import com.example.likarnyam.client.ScheduleApiClient;
import com.example.likarnyam.client.SymptomApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @FXML private TextField symptomSearchField;
    @FXML private FlowPane selectedSymptomsPane;
    @FXML private VBox symptomCategoriesContainer;
    @FXML private TextField diagnosisSearchField;
    @FXML private Label selectedDiagnosisLabel;
    @FXML private VBox diagnosisListContainer;

    private Long editAppointmentId = null;
    private Long patientId;
    private String patientName;
    private LocalDate selectedDate = null;
    private LocalTime selectedSlot = null;
    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    private final Map<Long, JsonNode> allSymptoms = new LinkedHashMap<>();
    private final Set<Long> selectedSymptomIds = new LinkedHashSet<>();

    private final Map<Long, JsonNode> allDiseases = new LinkedHashMap<>();
    private Long selectedDiseaseId = null;

    public void setPatient(Long id, String name) {
        this.patientId = id;
        this.patientName = name;
        patientNameHeader.setText("Patient: " + name);
        buildMiniCalendar();
        loadSymptoms();
        loadDiseases();
    }

    public void setAppointmentForEdit(Long appointmentId, String patientName,
                                      LocalDateTime currentDateTime, String reason,
                                      String notes, List<Long> existingSymptomIds,
                                      Long diseaseId, String diseaseName) {
        this.editAppointmentId = appointmentId;
        this.selectedDiseaseId = diseaseId;
        this.patientNameHeader.setText("Editing: " + patientName);
        this.bookBtn.setText("Save Changes");
        this.mainTitleLabel.setText("Edit Appointment");
        this.reasonField.setText(reason != null ? reason : "");
        this.notesField.setText(notes != null ? notes : "");
        this.currentYear = currentDateTime.getYear();
        this.currentMonth = currentDateTime.getMonthValue();
        this.selectedDate = currentDateTime.toLocalDate();
        this.selectedSlot = currentDateTime.toLocalTime();

        if (existingSymptomIds != null) selectedSymptomIds.addAll(existingSymptomIds);

        buildMiniCalendar();
        loadSymptoms();
        loadDiseases();
        updateBookButton();
    }

    public void setExistingDisease(Long diseaseId, String diseaseName) {
        this.selectedDiseaseId = diseaseId;
        if (selectedDiagnosisLabel != null && diseaseName != null) {
            selectedDiagnosisLabel.setText("✓ " + diseaseName);
            selectedDiagnosisLabel.setStyle(
                    "-fx-text-fill: #38a169; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
        }
    }

    @FXML
    public void initialize() {
        bookBtn.setDisable(true);
        if (symptomSearchField != null) {
            symptomSearchField.textProperty().addListener((obs, oldVal, newVal) ->
                    renderSymptomCategories(newVal.trim().toLowerCase()));
        }
        if (diagnosisSearchField != null) {
            diagnosisSearchField.textProperty().addListener((obs, oldVal, newVal) ->
                    renderDiseases(newVal.trim().toLowerCase()));
        }
    }

    // ── ДИАГНОЗ ───────────────────────────────────────────────

    private void loadDiseases() {
        new Thread(() -> {
            try {
                JsonNode diseases = DiseaseApiClient.getMy();
                Platform.runLater(() -> {
                    allDiseases.clear();
                    for (JsonNode d : diseases) {
                        allDiseases.put(d.get("id").asLong(), d);
                    }
                    renderDiseases("");
                    // Если уже выбран диагноз — обновляем label
                    if (selectedDiseaseId != null && allDiseases.containsKey(selectedDiseaseId)) {
                        String name = allDiseases.get(selectedDiseaseId).get("name").asText();
                        if (selectedDiagnosisLabel != null) {
                            selectedDiagnosisLabel.setText("✓ " + name);
                            selectedDiagnosisLabel.setStyle(
                                    "-fx-text-fill: #38a169; -fx-font-size: 12px; -fx-font-weight: bold;"
                            );
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderDiseases(String filter) {
        if (diagnosisListContainer == null) return;
        diagnosisListContainer.getChildren().clear();

        for (JsonNode d : allDiseases.values()) {
            String name = d.get("name").asText();
            if (!filter.isEmpty() && !name.toLowerCase().contains(filter)) continue;

            long id = d.get("id").asLong();
            boolean isSelected = id == (selectedDiseaseId != null ? selectedDiseaseId : -1);

            boolean isDark = isDarkTheme();
            String bg     = isSelected ? (isDark ? "#0A1E18" : "#F0FFF4") : (isDark ? "#2D2D2D" : "#F7FAFC");
            String border = isSelected ? "#38a169" : (isDark ? "#444" : "#e2e8f0");
            String text   = isSelected ? "#38a169" : (isDark ? "#C8C6BE" : "#2d3748");

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: %s; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8; " +
                            "-fx-border-width: 1; -fx-padding: 8 12; -fx-cursor: hand;",
                    bg, border
            ));

            VBox info = new VBox(2);
            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + text + ";");

            String icdCode = d.has("icdCode") && !d.get("icdCode").isNull()
                    ? d.get("icdCode").asText() : "";
            if (!icdCode.isEmpty()) {
                Label icdLabel = new Label("ICD: " + icdCode);
                icdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");
                info.getChildren().addAll(nameLabel, icdLabel);
            } else {
                info.getChildren().add(nameLabel);
            }

            if (isSelected) {
                Label check = new Label("✓");
                check.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold; -fx-font-size: 14px;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(info, spacer, check);
            } else {
                row.getChildren().add(info);
            }

            row.setOnMouseClicked(e -> selectDisease(id, name));
            diagnosisListContainer.getChildren().add(row);

            final String finalBg = bg;
            final String finalBorder = border;

            row.setOnMouseEntered(e -> {
                if (id != (selectedDiseaseId != null ? selectedDiseaseId : -1)) {
                    boolean dark = isDarkTheme();
                    row.setStyle(String.format(
                            "-fx-background-color: %s; -fx-border-color: %s; " +
                                    "-fx-border-radius: 8; -fx-background-radius: 8; " +
                                    "-fx-border-width: 1; -fx-padding: 8 12; -fx-cursor: hand;",
                            dark ? "#3A3A3A" : "#EDF2F7",
                            dark ? "#555" : "#cbd5e0"
                    ));
                }
            });

            row.setOnMouseExited(e -> {
                if (id != (selectedDiseaseId != null ? selectedDiseaseId : -1)) {
                    row.setStyle(String.format(
                            "-fx-background-color: %s; -fx-border-color: %s; " +
                                    "-fx-border-radius: 8; -fx-background-radius: 8; " +
                                    "-fx-border-width: 1; -fx-padding: 8 12; -fx-cursor: hand;",
                            finalBg, finalBorder
                    ));
                }
            });

        }
    }

    private void selectDisease(long id, String name) {
        if (selectedDiseaseId != null && selectedDiseaseId == id) {
            // Снимаем выбор
            selectedDiseaseId = null;
            if (selectedDiagnosisLabel != null) {
                selectedDiagnosisLabel.setText("No diagnosis selected");
                selectedDiagnosisLabel.setStyle(
                        "-fx-text-fill: #a0aec0; -fx-font-size: 12px;"
                );
            }
        } else {
            selectedDiseaseId = id;
            if (selectedDiagnosisLabel != null) {
                selectedDiagnosisLabel.setText("✓ " + name);
                selectedDiagnosisLabel.setStyle(
                        "-fx-text-fill: #38a169; -fx-font-size: 12px; -fx-font-weight: bold;"
                );
            }
        }
        renderDiseases(diagnosisSearchField != null
                ? diagnosisSearchField.getText().trim().toLowerCase() : "");
    }

    // ── СИМПТОМЫ ──────────────────────────────────────────────

    private void loadSymptoms() {
        new Thread(() -> {
            try {
                JsonNode symptoms = SymptomApiClient.getAll();
                Platform.runLater(() -> {
                    allSymptoms.clear();
                    for (JsonNode s : symptoms) {
                        allSymptoms.put(s.get("id").asLong(), s);
                    }
                    refreshSelectedSymptoms();
                    renderSymptomCategories("");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderSymptomCategories(String filter) {
        if (symptomCategoriesContainer == null) return;
        symptomCategoriesContainer.getChildren().clear();

        Map<String, List<JsonNode>> byCategory = new LinkedHashMap<>();
        for (JsonNode s : allSymptoms.values()) {
            String name = s.get("name").asText().toLowerCase();
            if (!filter.isEmpty() && !name.contains(filter)) continue;
            String cat = s.get("category").asText();
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<String, List<JsonNode>> entry : byCategory.entrySet()) {
            Label catLabel = new Label(entry.getKey());
            catLabel.getStyleClass().add("symptom-category-label");
            symptomCategoriesContainer.getChildren().add(catLabel);

            FlowPane pane = new FlowPane();
            pane.setHgap(6);
            pane.setVgap(6);
            pane.getStyleClass().add("symptom-flow");

            for (JsonNode s : entry.getValue()) {
                long id = s.get("id").asLong();
                pane.getChildren().add(buildSymptomTag(s, id, false));
            }
            symptomCategoriesContainer.getChildren().add(pane);
        }
    }

    private HBox buildSymptomTag(JsonNode s, long id, boolean isSelected) {
        HBox tag = new HBox(5);
        tag.setAlignment(Pos.CENTER_LEFT);

        String category = s.get("category").asText().toLowerCase().replace(" ", "-");
        boolean isDark = isDarkTheme();

        String bg, border, textColor;
        switch (category) {
            case "respiratory"      -> { bg = isDark ? "#1A2A3A" : "#E6F1FB"; border = "#85B7EB"; textColor = isDark ? "#90C8F0" : "#0C447C"; }
            case "cardiovascular"   -> { bg = isDark ? "#3A1A1A" : "#FCEBEB"; border = "#F09595"; textColor = isDark ? "#F0A0A0" : "#791F1F"; }
            case "neurological"     -> { bg = isDark ? "#1E1A3A" : "#EEEDFE"; border = "#AFA9EC"; textColor = isDark ? "#C0BAFF" : "#3C3489"; }
            case "gastrointestinal" -> { bg = isDark ? "#2A1E08" : "#FAEEDA"; border = "#EF9F27"; textColor = isDark ? "#F0B060" : "#633806"; }
            case "musculoskeletal"  -> { bg = isDark ? "#0A1E18" : "#E1F5EE"; border = "#5DCAA5"; textColor = isDark ? "#70D4B0" : "#085041"; }
            case "skin"             -> { bg = isDark ? "#2A0A18" : "#FBEAF0"; border = "#ED93B1"; textColor = isDark ? "#F0A0C0" : "#72243E"; }
            case "ent"              -> { bg = isDark ? "#0A1E08" : "#EAF3DE"; border = "#97C459";  textColor = isDark ? "#A8D470" : "#27500A"; }
            case "urological"       -> { bg = isDark ? "#0A1A2A" : "#EAF3FB"; border = "#7EC8E3"; textColor = isDark ? "#90D4F0" : "#0A4A6E"; }
            case "psychological"    -> { bg = isDark ? "#1A0A2A" : "#F3EAFB"; border = "#C49AE3"; textColor = isDark ? "#D0A8F0" : "#4A1A72"; }
            default                 -> { bg = isDark ? "#2D2D2D" : "#F1EFE8"; border = "#B4B2A9"; textColor = isDark ? "#C8C6BE" : "#444441"; }
        }

        final String finalBg = bg;
        final String finalBorder = border;
        final String finalTextColor = textColor;

        tag.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; " +
                        "-fx-border-width: 1; -fx-padding: 4 10; -fx-cursor: hand;",
                bg, border
        ));

        try {
            FontIcon icon = new FontIcon(s.get("icon").asText());
            icon.setIconSize(13);
            icon.setIconColor(javafx.scene.paint.Color.web(finalTextColor));
            tag.getChildren().add(icon);
        } catch (Exception ignored) {}

        Label nameLabel = new Label(s.get("name").asText());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + finalTextColor + ";");
        tag.getChildren().add(nameLabel);

        tag.setOnMouseClicked(e -> toggleSymptom(id));
        tag.setCursor(javafx.scene.Cursor.HAND);

        tag.setOnMouseEntered(e -> tag.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; " +
                        "-fx-border-width: 1; -fx-padding: 4 10; -fx-cursor: hand; -fx-opacity: 0.75;",
                finalBg, finalBorder
        )));
        tag.setOnMouseExited(e -> tag.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; " +
                        "-fx-border-width: 1; -fx-padding: 4 10; -fx-cursor: hand; -fx-opacity: 1.0;",
                finalBg, finalBorder
        )));

        return tag;
    }

    private void refreshSelectedSymptoms() {
        if (selectedSymptomsPane == null) return;
        selectedSymptomsPane.getChildren().clear();

        for (Long id : selectedSymptomIds) {
            JsonNode s = allSymptoms.get(id);
            if (s == null) continue;

            HBox tag = buildSymptomTag(s, id, false);

            Label remove = new Label("×");
            remove.setStyle(
                    "-fx-font-size: 13px; -fx-font-weight: bold; " +
                            "-fx-text-fill: #718096; -fx-padding: 0 0 0 4; -fx-cursor: hand;"
            );
            remove.setOnMouseClicked(e -> toggleSymptom(id));
            tag.getChildren().add(remove);

            tag.setOnMouseClicked(null);
            remove.setOnMouseClicked(e -> toggleSymptom(id));

            selectedSymptomsPane.getChildren().add(tag);
        }
    }

    private void toggleSymptom(long id) {
        if (selectedSymptomIds.contains(id)) {
            selectedSymptomIds.remove(id);
        } else {
            selectedSymptomIds.add(id);
        }
        refreshSelectedSymptoms();
        renderSymptomCategories(
                symptomSearchField != null ? symptomSearchField.getText().trim().toLowerCase() : ""
        );
    }

    private boolean isDarkTheme() {
        try {
            String theme = com.example.likarnyam.session.UserSession.getInstance().getTheme();
            return "dark".equalsIgnoreCase(theme);
        } catch (Exception e) {
            return false;
        }
    }

    // ── КАЛЕНДАРЬ ─────────────────────────────────────────────

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
        if (selectedDateCell != null)
            selectedDateCell.getStyleClass().remove("mini-cal-selected");
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
                        List<LocalTime> availableTimes = new ArrayList<>();
                        for (JsonNode slot : slots)
                            availableTimes.add(LocalTime.parse(
                                    slot.asText().substring(0, 5),
                                    DateTimeFormatter.ofPattern("HH:mm")));

                        if (editAppointmentId != null && selectedDate != null && selectedSlot != null)
                            if (!availableTimes.contains(selectedSlot))
                                availableTimes.add(selectedSlot);

                        Collections.sort(availableTimes);

                        for (LocalTime time : availableTimes) {
                            String displayTime = FxUtils.formatTime(time.toString());
                            Button slotBtn = new Button(displayTime);
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
        if (selectedSlotBtn != null)
            selectedSlotBtn.getStyleClass().remove("mini-slot-selected");
        selectedSlot = time;
        selectedSlotBtn = btn;
        btn.getStyleClass().add("mini-slot-selected");
        updateBookButton();
    }

    private void updateBookButton() {
        bookBtn.setDisable(selectedDate == null || selectedSlot == null
                || reasonField.getText().trim().isEmpty());
    }

    // ── BOOKING ───────────────────────────────────────────────

    @FXML
    public void handleBook() {
        if (selectedDate == null || selectedSlot == null) return;
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            reasonField.setStyle(reasonField.getStyle() + "-fx-border-color: #e53e3e;");
            return;
        }

        bookBtn.setDisable(true);
        bookBtn.setText(editAppointmentId == null ? "Booking..." : "Saving...");

        LocalDateTime appointmentAt = LocalDateTime.of(selectedDate, selectedSlot);
        List<Long> symptomIds = new ArrayList<>(selectedSymptomIds);

        new Thread(() -> {
            try {
                if (editAppointmentId == null) {
                    AppointmentApiClient.createAppointment(
                            patientId, appointmentAt, reason,
                            notesField.getText().trim(), symptomIds, selectedDiseaseId
                    );
                } else {
                    AppointmentApiClient.updateAppointment(
                            editAppointmentId, appointmentAt, reason,
                            notesField.getText().trim(), symptomIds, selectedDiseaseId
                    );
                }
                Platform.runLater(() -> {
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