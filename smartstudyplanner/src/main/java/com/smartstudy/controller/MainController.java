package com.smartstudy.controller;

import com.smartstudy.model.Task;
import com.smartstudy.storage.JsonStorage;
import com.smartstudy.utils.ProductivityCalculator;
import com.smartstudy.utils.ReminderService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainController {

    @FXML public BorderPane rootPane;
    @FXML private Label lblScore;
    @FXML private Label lblStreak;
    @FXML private VBox chartsBox;
    @FXML private Button btnAddTask;
    @FXML private Button btnViewTasks;

    private final String storagePath = System.getProperty("user.home") + "/.smartstudy_tasks.json";
    private final JsonStorage storage = new JsonStorage(storagePath);
    private List<Task> tasks = new ArrayList<>();
    private AtomicInteger idGen = new AtomicInteger(0);
    private ReminderService reminderService;

    @FXML
    public void initialize() {
        tasks = storage.loadTasks();
        int maxId = tasks.stream().mapToInt(Task::getId).max().orElse(0);
        idGen.set(maxId);
        updateStats();
        buildCharts();

        // Start reminder service (check every 60 seconds)
        reminderService = new ReminderService(this::getTasks);
        reminderService.startEvery(10, 60);
    }

    @FXML
    private void onAddTask() {
        try {
            Stage dialog = new Stage();
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_task.fxml"));
            Scene scene = new Scene(loader.load());
            // controller will create and add task
            Object ctrl = loader.getController();
            if (ctrl instanceof com.smartstudy.controller.TasksController) {
                com.smartstudy.controller.TasksController tctrl = (com.smartstudy.controller.TasksController) ctrl;
                tctrl.setContext(this::addTaskFromDialog);
            }
            dialog.setScene(scene);
            dialog.setTitle("Add Task");
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTaskFromDialog(Task task) {
        // assign id
        task.setId(idGen.incrementAndGet());
        tasks.add(task);
        storage.saveTasks(tasks);
        updateStats();
        buildCharts();
    }

    @FXML
    private void onViewTasks() {
        try {
            Stage dialog = new Stage();
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/tasks.fxml"));
            Scene scene = new Scene(loader.load());
            Object ctrl = loader.getController();
            if (ctrl instanceof com.smartstudy.controller.TasksController) {
                com.smartstudy.controller.TasksController tctrl = (com.smartstudy.controller.TasksController) ctrl;
                tctrl.setTasksList(tasks);
                tctrl.setOnChangeListener(this::onTasksChanged);
            }
            dialog.setScene(scene);
            dialog.setTitle("All Tasks");
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onTasksChanged(List<Task> changed) {
        this.tasks = changed;
        storage.saveTasks(tasks);
        updateStats();
        buildCharts();
    }

    private void updateStats() {
        int score = ProductivityCalculator.calculateScore(tasks);
        lblScore.setText(score + "%");
        // simple streak calculation: current consecutive days with at least one completed task
        int streak = calcCurrentStreak();
        lblStreak.setText(streak + " days");
    }

    private int calcCurrentStreak() {
        // naive approach: count recent days up to today where at least 1 task completed
        Set<LocalDate> completedDates = tasks.stream()
                .filter(Task::isCompleted)
                .map(t -> t.getDeadline())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        LocalDate d = LocalDate.now();
        int s = 0;
        while (completedDates.contains(d.minusDays(s))) s++;
        return s;
    }

    private void buildCharts() {
        chartsBox.getChildren().clear();
        if (tasks.isEmpty()) {
            Label empty = new Label("No tasks yet. Add a task to see analytics.");
            chartsBox.getChildren().add(empty);
            return;
        }

        // Pie chart: by subject
        Map<String, Long> bySubject = tasks.stream().collect(Collectors.groupingBy(
                t -> t.getSubject() == null || t.getSubject().isBlank() ? "Uncategorized" : t.getSubject(),
                Collectors.counting()
        ));
        PieChart pie = new PieChart();
        pie.setTitle("Tasks by Subject");
        bySubject.forEach((k,v) -> pie.getData().add(new PieChart.Data(k, v)));
        pie.setLegendVisible(false);

        // Bar chart: tasks completed per day (last 7 days)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setTitle("Completed Tasks (last 7 days)");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long c = tasks.stream().filter(Task::isCompleted)
                    .filter(t -> day.equals(t.getDeadline()))
                    .count();
            series.getData().add(new XYChart.Data<>(day.toString(), c));
        }
        bar.getData().add(series);

        chartsBox.getChildren().addAll(pie, bar);
    }

    public List<Task> getTasks() { return tasks; }

    public void stopService() {
        if (reminderService != null) reminderService.stop();
    }
}
