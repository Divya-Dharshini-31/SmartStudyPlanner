package com.smartstudy.controller;

import com.smartstudy.model.Task;
import com.smartstudy.storage.JsonStorage;
import com.smartstudy.utils.ProductivityCalculator;
import com.smartstudy.utils.ReminderService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private Label lblScore;
    @FXML private Label lblStreak;
    @FXML private VBox chartsBox;

    private final String storagePath =
            System.getProperty("user.home") + "/.smartstudy_tasks.json";

    private final JsonStorage storage = new JsonStorage(storagePath);
    private List<Task> tasks = new ArrayList<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private ReminderService reminderService;

    @FXML
    public void initialize() {
        tasks = storage.loadTasks();
        idGen.set(tasks.stream().mapToInt(Task::getId).max().orElse(0));
        updateStats();
        buildCharts();

        reminderService = new ReminderService(this::getTasks);
        reminderService.startEvery(10, 60);
    }

    @FXML
    private void onAddTask() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/add_task.fxml"));
            Scene scene = new Scene(loader.load());

            AddTaskController controller = loader.getController();
            Stage dialog = new Stage();

            controller.setOnTaskCreated(task -> addTaskFromDialog(task));
            controller.setStage(dialog);

            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Task");
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (IOException e) {
            showError("Failed to open Add Task window");
            e.printStackTrace();
        }
    }

    private void addTaskFromDialog(Task task) {
        task.setId(idGen.incrementAndGet());
        tasks.add(task);
        storage.saveTasks(tasks);
        updateStats();
        buildCharts();
    }

    @FXML
    private void onViewTasks() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/tasks.fxml"));
            Scene scene = new Scene(loader.load());

            TasksController controller = loader.getController();
            controller.setTasksList(tasks);
            controller.setOnChangeListener(this::onTasksChanged);

            Stage dialog = new Stage();
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("All Tasks");
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (IOException e) {
            showError("Failed to open Tasks window");
            e.printStackTrace();
        }
    }

    private void onTasksChanged(List<Task> changed) {
        tasks = changed;
        storage.saveTasks(tasks);
        updateStats();
        buildCharts();
    }

    private void updateStats() {
        lblScore.setText(ProductivityCalculator.calculateScore(tasks) + "%");
        lblStreak.setText(calcCurrentStreak() + " days");
    }

    private int calcCurrentStreak() {
        Set<LocalDate> completed =
                tasks.stream()
                        .filter(Task::isCompleted)
                        .map(Task::getDeadline)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        int streak = 0;
        while (completed.contains(today.minusDays(streak))) streak++;
        return streak;
    }

    private void buildCharts() {
        chartsBox.getChildren().clear();

        if (tasks.isEmpty()) {
            chartsBox.getChildren().add(new Label("No tasks yet."));
            return;
        }

        Map<String, Long> bySubject =
                tasks.stream().collect(Collectors.groupingBy(
                        t -> t.getSubject() == null || t.getSubject().isBlank()
                                ? "Uncategorized"
                                : t.getSubject(),
                        Collectors.counting()
                ));

        PieChart pie = new PieChart();
        pie.setTitle("Tasks by Subject");
        bySubject.forEach((k, v) ->
                pie.getData().add(new PieChart.Data(k, v)));

        chartsBox.getChildren().add(pie);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
