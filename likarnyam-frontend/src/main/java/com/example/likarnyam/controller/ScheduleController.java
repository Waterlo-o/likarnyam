package com.example.likarnyam.controller;

import com.example.likarnyam.client.AppointmentApiClient;
import com.example.likarnyam.client.ScheduleApiClient;
import com.example.likarnyam.util.FxUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.stage.Popup;
import javafx.scene.control.ScrollPane;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ScheduleController {

    @FXML private Label monthLabel;
    @FXML private VBox calendarGrid;
    @FXML private VBox dayDetailPanel;


    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    @FXML
    public void initialize() {
        loadCalendar();
    }

    private Map<Integer, VBox> dayCells = new HashMap<>();

    private void loadCalendar() {
        LocalDate date = LocalDate.of(currentYear, currentMonth, 1);
        monthLabel.setText(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        selectedCell = null;
        dayCells.clear();

        new Thread(() -> {
            try {
                JsonNode days = ScheduleApiClient.getCalendar(currentYear, currentMonth);
                Platform.runLater(() -> buildCalendar(days));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void buildCalendar(JsonNode days) {
        calendarGrid.getChildren().clear();
        calendarGrid.setMaxWidth(Double.MAX_VALUE);
        currentDays = days;
        selectedCell = null;
        dayCells.clear();

        LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();

        // Используем GridPane вместо HBox — гарантирует равные колонки
        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setHgap(5);
        grid.setVgap(5);

        // 7 равных колонок
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        int col = startDayOfWeek - 1;
        int row = 0;

        // Дни предыдущего месяца
        LocalDate prevMonthDate = firstDay.minusMonths(1);
        int daysInPrevMonth = prevMonthDate.lengthOfMonth();
        for (int i = startDayOfWeek - 1; i > 0; i--) {
            int prevDay = daysInPrevMonth - i + 1;
            VBox cell = createAdjacentDayCell(prevDay, false);
            grid.add(cell, startDayOfWeek - i - 1, 0);
        }

        // Дни текущего месяца
        for (JsonNode day : days) {
            int dayNum = day.get("day").asInt();
            boolean isWorking = day.get("workingDay").asBoolean();
            boolean isToday = day.get("today").asBoolean();
            int aptCount = day.get("appointmentCount").asInt();

            VBox cell = createDayCell(dayNum, isWorking, isToday, aptCount, day);
            grid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }

        // Дни следующего месяца
        int nextDay = 1;
        while (col < 7 && col > 0) {
            VBox cell = createAdjacentDayCell(nextDay, true);
            grid.add(cell, col, row);
            col++;
            nextDay++;
        }

        calendarGrid.getChildren().add(grid);

        if (pendingOpenDay > 0 && currentDays != null) {
            final int dayToOpen = pendingOpenDay;
            pendingOpenDay = -1;
            Platform.runLater(() -> {
                for (JsonNode day : currentDays) {
                    if (day.get("day").asInt() == dayToOpen) {
                        boolean isWorking = day.get("workingDay").asBoolean();
                        int aptCount = day.get("appointmentCount").asInt();

                        // Визуально выделяем ячейку
                        VBox cell = dayCells.get(dayToOpen);
                        if (cell != null) {
                            selectedCellStyle = cell.getStyle();
                            selectedCell = cell;
                            cell.setStyle(cell.getStyle() +
                                    "-fx-border-color: #2196F3;" +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.3), 8, 0, 0, 2);"
                            );
                        }

                        showDayDetail(dayToOpen, isWorking, aptCount, day);
                        break;
                    }
                }
            });
        }
    }

    private JsonNode currentDays = null;

    private int pendingOpenDay = -1;

    private void openDayAfterNavigation(int dayNum) {
        pendingOpenDay = dayNum;
    }

    private VBox createAdjacentDayCell(int dayNum, boolean isNext) {
        Label numberLabel = new Label(String.valueOf(dayNum));
        numberLabel.setStyle(
                "-fx-text-fill: #cbd5e0; -fx-font-size: 15px; -fx-font-weight: bold;"
        );

        VBox cell = new VBox(numberLabel);
        cell.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #edf2f7;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;"
        );
        cell.setMinHeight(80);
        cell.setMaxHeight(80);
        cell.setPrefHeight(80);

        cell.setOnMouseClicked(e -> {
            // Переключаем месяц и открываем нужный день
            if (isNext) {
                nextMonth();
            } else {
                prevMonth();
            }
            // После переключения ищем нужный день и открываем
            openDayAfterNavigation(dayNum);
        });

        cell.setOnMouseEntered(e -> cell.setStyle(
                "-fx-background-color: #EBF4FF;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #64B5F6;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;"
        ));
        cell.setOnMouseExited(e -> cell.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #edf2f7;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;"
        ));

        return cell;
    }


    private VBox createEmptyCell() {
        VBox empty = new VBox();
        empty.getStyleClass().addAll("cal-day", "cal-day-empty");
        empty.setMinHeight(80);
        empty.setMaxHeight(80);
        empty.setPrefHeight(80);
        empty.setMaxWidth(Double.MAX_VALUE);
        empty.setMinWidth(0);
        HBox.setHgrow(empty, Priority.ALWAYS);
        return empty;
    }

    private VBox selectedCell = null;
    private String selectedCellStyle = "";

    private VBox createDayCell(int dayNum, boolean isWorking,
                               boolean isToday, int aptCount, JsonNode day) {
        Label numberLabel = new Label(String.valueOf(dayNum));
        numberLabel.getStyleClass().add("cal-day-number");

        // Точки приёмов — фиксированный размер контейнера
        HBox dots = new HBox(3);
        dots.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        dots.setPrefHeight(14);
        dots.setMinHeight(14);
        dots.setMaxHeight(14);
        dots.setClip(new javafx.scene.shape.Rectangle(200, 14));

        if (aptCount > 0 && !isToday) {
            int maxDots = Math.min(aptCount, 3);
            for (int i = 0; i < maxDots; i++) {
                Label dot = new Label();
                dot.getStyleClass().add("cal-apt-dot");
                dots.getChildren().add(dot);
            }
            if (aptCount > 3) {
                Label plus = new Label("+" + (aptCount - 3));
                plus.setStyle("-fx-font-size: 9px; -fx-text-fill: #4299e1; -fx-font-weight: bold;");
                dots.getChildren().add(plus);
            }
        } else if (aptCount > 0 && isToday) {
            Label countLabel = new Label(aptCount + " apt");
            countLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
            dots.getChildren().add(countLabel);
        }

        VBox cell = new VBox(4, numberLabel, dots);
        cell.getStyleClass().add("cal-day");
        cell.setMinWidth(0);
        cell.setMinHeight(80);
        cell.setMaxHeight(80);
        cell.setPrefHeight(80);
        cell.setPadding(new Insets(10));

        if (isToday) {
            cell.getStyleClass().add("cal-day-today");
            numberLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        } else if (isWorking) {
            // Цвет по загруженности
            if (aptCount == 0) {
                cell.setStyle(
                        "-fx-background-color: #F0FFF4;" + // зелёный — свободно
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #c6f6d5;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10;"
                );
            } else if (aptCount <= 3) {
                cell.setStyle(
                        "-fx-background-color: #EBF4FF;" + // синий — средне
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #bee3f8;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10;"
                );
            } else {
                cell.setStyle(
                        "-fx-background-color: #FFF5F5;" + // красный — загружено
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #fed7d7;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10;"
                );
            }
        } else {
            // Выходной
            cell.setStyle(
                    "-fx-background-color: #fafafa;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #edf2f7;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-padding: 10;"
            );
            numberLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 15px;");
        }

        cell.setOnMouseClicked(e -> {
            // Берём актуальные данные из currentDays
            if (currentDays != null) {
                for (JsonNode d : currentDays) {
                    if (d.get("day").asInt() == dayNum) {
                        boolean actualWorking = d.get("workingDay").asBoolean();
                        int actualAptCount = d.get("appointmentCount").asInt();
                        showDayDetail(dayNum, actualWorking, actualAptCount, d);
                        break;
                    }
                }
            }
            // Выделение ячейки
            if (selectedCell != null) {
                selectedCell.setStyle(selectedCellStyle);
            }
            selectedCellStyle = cell.getStyle();
            selectedCell = cell;
            cell.setStyle(cell.getStyle() +
                    "-fx-border-color: #2196F3;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.3), 8, 0, 0, 2);"
            );
        });

        dayCells.put(dayNum, cell);
        return cell;
    }

    private void showDayDetail(int dayNum, boolean isWorking,
                               int aptCount, JsonNode day) {
        dayDetailPanel.getChildren().clear();
        dayDetailPanel.setPadding(new Insets(20));

        LocalDate date = LocalDate.of(currentYear, currentMonth, dayNum);
        String dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));

        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"
        );
        dateLabel.setWrapText(true);

        Label statusLabel = new Label(isWorking ? "  Working day  " : "  Day off  ");
        statusLabel.setStyle(isWorking
                ? "-fx-background-color: #c6f6d5; -fx-text-fill: #276749; " +
                "-fx-background-radius: 20; -fx-padding: 4 12 4 12; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-background-color: #fed7d7; -fx-text-fill: #c53030; " +
                "-fx-background-radius: 20; -fx-padding: 4 12 4 12; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;"
        );

        dayDetailPanel.getChildren().addAll(dateLabel, statusLabel);

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(8, 0, 8, 0));
        dayDetailPanel.getChildren().add(sep);

        if (!isWorking) {
            VBox offBox = new VBox(8);
            offBox.setAlignment(javafx.geometry.Pos.CENTER);
            offBox.setPadding(new Insets(20, 0, 0, 0));
            Label icon = new Label("-");
            icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #cbd5e0;");
            Label offLabel = new Label("Day off");
            offLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px; -fx-font-weight: bold;");
            Label subLabel = new Label("No appointments scheduled");
            subLabel.setStyle("-fx-text-fill: #cbd5e0; -fx-font-size: 12px;");
            offBox.getChildren().addAll(icon, offLabel, subLabel);
            dayDetailPanel.getChildren().add(offBox);
            return;
        }

        if (aptCount == 0) {
            VBox freeBox = new VBox(8);
            freeBox.setAlignment(javafx.geometry.Pos.CENTER);
            freeBox.setPadding(new Insets(20, 0, 0, 0));
            Label icon = new Label("○");
            icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #68d391;");
            Label freeLabel = new Label("All clear!");
            freeLabel.setStyle("-fx-text-fill: #38a169; -fx-font-size: 14px; -fx-font-weight: bold;");
            Label subLabel = new Label("No appointments\nAll slots available");
            subLabel.setStyle("-fx-text-fill: #68d391; -fx-font-size: 12px;");
            subLabel.setWrapText(true);
            freeBox.getChildren().addAll(icon, freeLabel, subLabel);
            dayDetailPanel.getChildren().add(freeBox);
            return;
        }

        // Заголовок
        HBox aptHeader = new HBox();
        aptHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label aptsTitle = new Label("Schedule");
        aptsTitle.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #4a5568;"
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countBadge = new Label(aptCount + " appointments");
        countBadge.setStyle(
                "-fx-background-color: #ebf8ff; -fx-text-fill: #2b6cb0; " +
                        "-fx-background-radius: 10; -fx-padding: 2 8 2 8; -fx-font-size: 11px;"
        );
        aptHeader.getChildren().addAll(aptsTitle, spacer, countBadge);
        VBox.setMargin(aptHeader, new Insets(0, 0, 10, 0));
        dayDetailPanel.getChildren().add(aptHeader);

        // ScrollPane для карточек
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("patient-scroll");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox scrollContent = new VBox(8);
        scrollContent.setPadding(new Insets(0, 5, 0, 0));

        JsonNode appointments = day.get("appointments");
        if (appointments != null) {
            for (JsonNode apt : appointments) {
                String timeStr = apt.get("time").asText();
                String patientName = apt.get("patientName").asText();
                String reason = apt.get("reason").asText();
                String aptStatus = apt.has("status") ? apt.get("status").asText() : "SCHEDULED";

                // Цвет полоски по статусу
                String stripeColor = switch (aptStatus) {
                    case "COMPLETED" -> "#38a169";
                    case "CANCELLED" -> "#e53e3e";
                    case "NO_SHOW" -> "#d69e2e";
                    default -> "#64B5F6";
                };

                VBox stripe = new VBox();
                stripe.setStyle(
                        "-fx-background-color: " + stripeColor + ";" +
                                "-fx-background-radius: 3;" +
                                "-fx-min-width: 4; -fx-max-width: 4; -fx-min-height: 40;"
                );

                VBox info = new VBox(3);
                Label timeLabel = new Label(timeStr);
                timeLabel.setStyle(
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3748;"
                );
                Label nameLabel = new Label(patientName);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
                Label reasonLabel = new Label(reason);
                reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
                info.getChildren().addAll(timeLabel, nameLabel, reasonLabel);

                if (!aptStatus.equals("SCHEDULED")) {
                    String badgeText = switch (aptStatus) {
                        case "COMPLETED" -> "✓ Completed";
                        case "CANCELLED" -> "✕ Cancelled";
                        case "NO_SHOW" -> "? No Show";
                        default -> aptStatus;
                    };
                    String badgeStyle = switch (aptStatus) {
                        case "COMPLETED" -> "-fx-background-color: #c6f6d5; -fx-text-fill: #276749;";
                        case "CANCELLED" -> "-fx-background-color: #fed7d7; -fx-text-fill: #c53030;";
                        case "NO_SHOW" -> "-fx-background-color: #fefcbf; -fx-text-fill: #744210;";
                        default -> "-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568;";
                    };

                    Label statusBadge = new Label(badgeText);
                    statusBadge.setStyle(
                            badgeStyle +
                                    "-fx-background-radius: 6; -fx-padding: 2 8 2 8;" +
                                    "-fx-font-size: 10px; -fx-font-weight: bold;"
                    );
                    info.getChildren().add(statusBadge);

                    stripe.setStyle(
                            "-fx-background-color: " + switch (aptStatus) {
                                case "COMPLETED" -> "#38a169";
                                case "CANCELLED" -> "#e53e3e";
                                case "NO_SHOW" -> "#d69e2e";
                                default -> "#64B5F6";
                            } + ";" +
                                    "-fx-background-radius: 3;" +
                                    "-fx-min-width: 4; -fx-max-width: 4; -fx-min-height: 40;"
                    );
                }

                HBox card = new HBox(12);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #e2e8f0;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-width: 1;" +
                                "-fx-padding: 10 14 10 14;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);"
                );
                card.getChildren().addAll(stripe, info);

                card.setOnMouseClicked(e -> showAppointmentPopup(apt, card, card));
                card.setOnMouseEntered(e -> card.setStyle(
                        "-fx-background-color: #EBF4FF;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #64B5F6;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-width: 1;" +
                                "-fx-padding: 10 14 10 14;" +
                                "-fx-cursor: hand;"
                ));
                card.setOnMouseExited(e -> card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #e2e8f0;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-width: 1;" +
                                "-fx-padding: 10 14 10 14;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);"
                ));

                scrollContent.getChildren().add(card);
            }
        }

        scrollPane.setContent(scrollContent);
        dayDetailPanel.getChildren().add(scrollPane);
    }

    private void showAppointmentPopup(JsonNode apt, HBox anchor, HBox card) {
        String timeStr = apt.get("time").asText();
        String patientName = apt.get("patientName").asText();
        String reason = apt.get("reason").asText();
        Long aptId = apt.get("appointmentId").asLong();

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox content = new VBox(12);
        content.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 20, 0, 0, 8);"
        );
        content.setPrefWidth(340);

        Label title = new Label("Appointment Details");
        title.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"
        );

        Separator sep = new Separator();

        HBox timeRow = createPopupRow("Time", timeStr);
        HBox patientRow = createPopupRow("Patient", patientName);
        HBox reasonRow = createPopupRow("Reason", reason);

        Separator sep2 = new Separator();

        // Статус кнопки
        Label statusTitle = new Label("Change Status");
        statusTitle.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #718096; -fx-font-weight: bold;"
        );

        HBox statusButtons = new HBox(8);
        statusButtons.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button completedBtn = new Button("✓ Completed");
        completedBtn.setStyle(
                "-fx-background-color: #c6f6d5;" +
                        "-fx-text-fill: #276749;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );

        Button noShowBtn = new Button("? No Show");
        noShowBtn.setStyle(
                "-fx-background-color: #fefcbf;" +
                        "-fx-text-fill: #744210;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );

        Button cancelBtn = new Button("✕ Cancelled");
        cancelBtn.setStyle(
                "-fx-background-color: #fed7d7;" +
                        "-fx-text-fill: #c53030;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );

        completedBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        noShowBtn.setMaxWidth(Double.MAX_VALUE);

        // Результат действия
        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 12px;");

        completedBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    VBox info = (VBox) card.getChildren().get(1);
                    boolean alreadyCompleted = info.getChildren().size() >= 4 &&
                            ((Label) info.getChildren().get(3)).getText().contains("Completed");

                    String newStatus = alreadyCompleted ? "SCHEDULED" : "COMPLETED";
                    AppointmentApiClient.updateStatus(aptId, newStatus);

                    Platform.runLater(() -> {
                        if (newStatus.equals("SCHEDULED")) {
                            resetCardVisual(card);
                        } else {
                            updateCardVisual(card, "COMPLETED");
                        }
                        popup.hide();
                        // Перезагружаем данные календаря в фоне
                        reloadCalendarData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> resultLabel.setText("Failed to update"));
                }
            }).start();
        });

        noShowBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    VBox info = (VBox) card.getChildren().get(1);
                    boolean alreadyNoShow = info.getChildren().size() >= 4 &&
                            ((Label) info.getChildren().get(3)).getText().contains("No Show");

                    String newStatus = alreadyNoShow ? "SCHEDULED" : "NO_SHOW";
                    System.out.println("Setting status to: " + newStatus);
                    AppointmentApiClient.updateStatus(aptId, newStatus);

                    Platform.runLater(() -> {
                        if (newStatus.equals("SCHEDULED")) {
                            resetCardVisual(card);
                        } else {
                            updateCardVisual(card, "NO_SHOW");
                        }
                        popup.hide();
                        reloadCalendarData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> resultLabel.setText("Failed to update"));
                }
            }).start();
        });

        cancelBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    VBox info = (VBox) card.getChildren().get(1);
                    boolean alreadyCancelled = info.getChildren().size() >= 4 &&
                            ((Label) info.getChildren().get(3)).getText().contains("Cancelled");

                    String newStatus = alreadyCancelled ? "SCHEDULED" : "CANCELLED";
                    AppointmentApiClient.updateStatus(aptId, newStatus);

                    Platform.runLater(() -> {
                        if (newStatus.equals("SCHEDULED")) {
                            resetCardVisual(card);
                        } else {
                            updateCardVisual(card, "CANCELLED");
                        }
                        popup.hide();
                        reloadCalendarData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> resultLabel.setText("Failed to update"));
                }
            }).start();
        });

        statusButtons.getChildren().addAll(completedBtn, noShowBtn, cancelBtn );

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #f7fafc;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 20 6 20;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.hide());

        content.getChildren().add(title);
        content.getChildren().add(sep);
        content.getChildren().add(timeRow);
        content.getChildren().add(patientRow);
        content.getChildren().add(reasonRow);
        content.getChildren().add(sep2);
        content.getChildren().add(statusTitle);
        content.getChildren().add(statusButtons);
        content.getChildren().add(resultLabel);
        content.getChildren().add(closeBtn);

        popup.getContent().add(content);

        javafx.stage.Window window = anchor.getScene().getWindow();
        double centerX = window.getX() + window.getWidth() / 2 - 160;
        double centerY = window.getY() + window.getHeight() / 2 - 200;
        popup.show(anchor, centerX, centerY);
    }

    private void resetCardVisual(HBox card) {
        VBox stripe = (VBox) card.getChildren().get(0);
        VBox info = (VBox) card.getChildren().get(1);

        // Возвращаем синюю полоску
        stripe.setStyle(
                "-fx-background-color: #64B5F6;" +
                        "-fx-background-radius: 3;" +
                        "-fx-min-width: 4; -fx-max-width: 4; -fx-min-height: 40;"
        );

        // Удаляем бейдж статуса
        if (info.getChildren().size() >= 4) {
            info.getChildren().remove(3);
        }
    }

    private HBox createPopupRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px; -fx-min-width: 80;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 13px; -fx-font-weight: bold;");
        valueNode.setWrapText(true);

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }
    private void updateCardVisual(HBox card, String status) {
        VBox stripe = (VBox) card.getChildren().get(0);
        VBox info = (VBox) card.getChildren().get(1);

        // Цвет полоски
        String stripeColor = switch (status) {
            case "COMPLETED" -> "#38a169";
            case "CANCELLED" -> "#e53e3e";
            case "NO_SHOW" -> "#d69e2e";
            default -> "#64B5F6";
        };
        stripe.setStyle(
                "-fx-background-color: " + stripeColor + ";" +
                        "-fx-background-radius: 3;" +
                        "-fx-min-width: 4; -fx-max-width: 4; -fx-min-height: 40;"
        );

        // Удаляем старый бейдж
        if (info.getChildren().size() >= 4) {
            info.getChildren().remove(3);
        }

        // Текст и цвет бейджа
        String badgeText = switch (status) {
            case "COMPLETED" -> "✓ Completed";
            case "CANCELLED" -> "✕ Cancelled";
            case "NO_SHOW" -> "? No Show";
            default -> "";
        };
        String badgeStyle = switch (status) {
            case "COMPLETED" -> "-fx-background-color: #c6f6d5; -fx-text-fill: #276749;";
            case "CANCELLED" -> "-fx-background-color: #fed7d7; -fx-text-fill: #c53030;";
            case "NO_SHOW" -> "-fx-background-color: #fefcbf; -fx-text-fill: #744210;";
            default -> "";
        };

        if (!badgeText.isEmpty()) {
            Label statusBadge = new Label(badgeText);
            statusBadge.setStyle(
                    badgeStyle +
                            "-fx-background-radius: 6; -fx-padding: 2 8 2 8;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;"
            );
            info.getChildren().add(statusBadge);
        }
    }
    private void reloadCalendarData() {
        new Thread(() -> {
            try {
                JsonNode days = ScheduleApiClient.getCalendar(currentYear, currentMonth);
                Platform.runLater(() -> {
                    currentDays = days;
                    // Обновляем только точки на ячейках без полной перестройки
                    updateDotCounts(days);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateDotCounts(JsonNode days) {
        for (JsonNode day : days) {
            int dayNum = day.get("day").asInt();
            int aptCount = day.get("appointmentCount").asInt();
            VBox cell = dayCells.get(dayNum);
            if (cell != null && cell.getChildren().size() >= 2) {
                // Обновляем HBox с точками
                HBox dots = (HBox) cell.getChildren().get(1);
                dots.getChildren().clear();

                if (aptCount > 0) {
                    boolean isToday = day.get("today").asBoolean();
                    int maxDots = Math.min(aptCount, 3);
                    for (int i = 0; i < maxDots; i++) {
                        Label dot = new Label();
                        dot.getStyleClass().add(isToday ? "cal-apt-dot-today" : "cal-apt-dot");
                        dots.getChildren().add(dot);
                    }
                    if (aptCount > 3) {
                        Label plus = new Label("+" + (aptCount - 3));
                        plus.setStyle("-fx-font-size: 9px; -fx-text-fill: " +
                                (isToday ? "white" : "#4299e1") + "; -fx-font-weight: bold;");
                        dots.getChildren().add(plus);
                    }
                }
            }
        }
    }

    @FXML
    private void prevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
        } else {
            currentMonth--;
        }
        loadCalendar();
    }

    @FXML
    private void nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
        } else {
            currentMonth++;
        }
        loadCalendar();
    }



    @FXML private void navigateHome() {
        FxUtils.navigateFullscreen(calendarGrid, "/fxml/home.fxml");
    }
    @FXML private void navigateSchedule() { }
    @FXML private void navigatePatients() {
        FxUtils.navigateFullscreen(calendarGrid, "/fxml/patient-list.fxml");
    }
    @FXML private void navigateAppointments() {
        System.out.println("Appointments — coming soon");
    }
    @FXML private void navigateSettings() {
        System.out.println("Settings — coming soon");
    }
    @FXML private void handleLogout() {
        javafx.application.Platform.exit();
    }
}