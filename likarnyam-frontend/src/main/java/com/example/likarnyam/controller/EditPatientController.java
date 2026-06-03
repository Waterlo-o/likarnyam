package com.example.likarnyam.controller;

import com.example.likarnyam.client.AllergyApiClient;
import com.example.likarnyam.client.PatientApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EditPatientController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private ComboBox<String> bloodTypeComboBox;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private TextField allergySearchField;
    @FXML private FlowPane selectedAllergiesPane;
    @FXML private VBox allergyCategoriesContainer;

    private PatientListController parentController;
    private Long currentPatientId;

    private final Map<Long, JsonNode> allAllergies = new LinkedHashMap<>();
    private final Set<Long> selectedAllergyIds = new LinkedHashSet<>();

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("MALE", "FEMALE", "OTHER");
        bloodTypeComboBox.getItems().addAll("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");

        if (allergySearchField != null) {
            allergySearchField.textProperty().addListener((obs, oldVal, newVal) ->
                    renderAllergies(newVal.trim().toLowerCase()));
        }

        birthDatePicker.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                Platform.runLater(() -> {
                    for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                        if (window.isShowing() && window.getScene() != null) {
                            javafx.scene.Parent root = window.getScene().getRoot();
                            if (root.getStyleClass().contains("popup")) {
                                root.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                            }
                        }
                    }
                });
            }
        });

        loadAllergies();
    }

    private void loadAllergies() {
        new Thread(() -> {
            try {
                JsonNode allergies = AllergyApiClient.getAll();
                Platform.runLater(() -> {
                    allAllergies.clear();
                    for (JsonNode a : allergies) {
                        allAllergies.put(a.get("id").asLong(), a);
                    }
                    refreshSelectedAllergies();
                    renderAllergies("");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderAllergies(String filter) {
        if (allergyCategoriesContainer == null) return;
        allergyCategoriesContainer.getChildren().clear();

        FlowPane pane = new FlowPane();
        pane.setHgap(6);
        pane.setVgap(6);

        for (JsonNode a : allAllergies.values()) {
            String name = a.get("name").asText().toLowerCase();
            if (!filter.isEmpty() && !name.contains(filter)) continue;
            long id = a.get("id").asLong();
            pane.getChildren().add(buildAllergyTag(a, id));
        }

        allergyCategoriesContainer.getChildren().add(pane);
    }

    private HBox buildAllergyTag(JsonNode a, long id) {
        boolean isDark = isDarkTheme();
        boolean isSelected = selectedAllergyIds.contains(id);

        String bg     = isSelected ? (isDark ? "#1A2E4A" : "#EBF4FF") : (isDark ? "#2D2D2D" : "#F1EFE8");
        String border = isSelected ? "#64B5F6" : "#B4B2A9";
        String text   = isSelected ? (isDark ? "#90C8F0" : "#0C447C") : (isDark ? "#C8C6BE" : "#444441");

        HBox tag = new HBox(5);
        tag.setAlignment(Pos.CENTER_LEFT);
        tag.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; " +
                        "-fx-border-width: 1; -fx-padding: 4 10; -fx-cursor: hand;",
                bg, border
        ));

        try {
            FontIcon icon = new FontIcon(a.get("icon").asText());
            icon.setIconSize(13);
            icon.setIconColor(javafx.scene.paint.Color.web(text));
            tag.getChildren().add(icon);
        } catch (Exception ignored) {}

        Label nameLabel = new Label(a.get("name").asText());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + text + ";");
        tag.getChildren().add(nameLabel);

        tag.setOnMouseClicked(e -> toggleAllergy(id));
        tag.setCursor(javafx.scene.Cursor.HAND);
        return tag;
    }

    private void toggleAllergy(long id) {
        if (selectedAllergyIds.contains(id)) {
            selectedAllergyIds.remove(id);
        } else {
            selectedAllergyIds.add(id);
        }
        refreshSelectedAllergies();
        renderAllergies(allergySearchField != null
                ? allergySearchField.getText().trim().toLowerCase() : "");
    }

    private void refreshSelectedAllergies() {
        if (selectedAllergiesPane == null) return;
        selectedAllergiesPane.getChildren().clear();

        boolean isDark = isDarkTheme();
        String bg     = isDark ? "#1A2E4A" : "#EBF4FF";
        String border = "#64B5F6";
        String text   = isDark ? "#90C8F0" : "#0C447C";

        for (Long id : selectedAllergyIds) {
            JsonNode a = allAllergies.get(id);
            if (a == null) continue;

            HBox tag = new HBox(5);
            tag.setAlignment(Pos.CENTER_LEFT);
            tag.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: %s; " +
                            "-fx-border-radius: 20; -fx-background-radius: 20; " +
                            "-fx-border-width: 1; -fx-padding: 4 10;",
                    bg, border
            ));

            try {
                FontIcon icon = new FontIcon(a.get("icon").asText());
                icon.setIconSize(12);
                icon.setIconColor(javafx.scene.paint.Color.web(text));
                tag.getChildren().add(icon);
            } catch (Exception ignored) {}

            Label name = new Label(a.get("name").asText());
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: " + text + ";");

            Label remove = new Label("×");
            remove.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; " +
                    "-fx-text-fill: " + border + "; -fx-padding: 0 0 0 4; -fx-cursor: hand;");
            remove.setOnMouseClicked(e -> toggleAllergy(id));

            tag.getChildren().addAll(name, remove);
            selectedAllergiesPane.getChildren().add(tag);
        }
    }

    private boolean isDarkTheme() {
        try {
            String theme = com.example.likarnyam.session.UserSession.getInstance().getTheme();
            return "dark".equalsIgnoreCase(theme);
        } catch (Exception e) {
            return false;
        }
    }

    public void setParentController(PatientListController controller) {
        this.parentController = controller;
    }

    public void setPatientData(Long id, String firstName, String lastName, String phone,
                               String email, String birthDateStr, String gender,
                               String bloodType) {
        this.currentPatientId = id;
        firstNameField.setText(firstName);
        lastNameField.setText(lastName);
        phoneField.setText(phone);
        emailField.setText(email);

        if (gender != null) genderComboBox.setValue(gender);
        if (bloodType != null) bloodTypeComboBox.setValue(bloodType);

        if (birthDateStr != null && !birthDateStr.isEmpty() && !birthDateStr.equals("null")) {
            try {
                birthDatePicker.setValue(LocalDate.parse(birthDateStr));
            } catch (Exception ignored) {}
        }
    }

    public void setPatientAllergies(List<Long> allergyIds) {
        if (allergyIds != null) {
            selectedAllergyIds.addAll(allergyIds);
        }
        refreshSelectedAllergies();
    }

    @FXML private void handleCancel() { closeDialog(); }

    @FXML
    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String phone     = phoneField.getText().trim();
        String email     = emailField.getText() != null ? emailField.getText().trim() : "";
        String gender    = genderComboBox.getValue();
        String bloodType = bloodTypeComboBox.getValue();
        String birthDate = birthDatePicker.getValue() != null
                ? birthDatePicker.getValue().format(DateTimeFormatter.ISO_DATE) : "";

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            showError("First Name, Last Name, and Phone are required.");
            return;
        }

        saveBtn.setDisable(true);
        saveBtn.setText("Updating...");

        List<Long> allergyIds = new ArrayList<>(selectedAllergyIds);

        new Thread(() -> {
            try {
                PatientApiClient.updatePatient(currentPatientId, firstName, lastName,
                        phone, email, birthDate, gender, bloodType, allergyIds);
                Platform.runLater(() -> {
                    if (parentController != null) parentController.loadPatients("");
                    closeDialog();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Update failed: " + e.getMessage());
                    saveBtn.setDisable(false);
                    saveBtn.setText("Update Patient");
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}