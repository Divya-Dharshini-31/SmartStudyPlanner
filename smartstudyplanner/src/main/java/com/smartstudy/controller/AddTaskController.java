package com.smartstudy.controller;

import com.smartstudy.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AddTaskController {

    @FXML private TextField tfTitle;
    @FXML private TextField tfSubject;
    @FXML private DatePicker dpDeadline;
    @FXML private TextField tfEst;

    private Consumer<Task> onTaskCreated;
    private Stage stage;

    public void setOnTaskCreated(Consumer<Task> callback) {
        this.onTaskCreated = callback;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onSaveTask() {
        try {
            if (tfTitle.getText().isBlank()) {
                showAlert("Title is required.");
                return;
            }

            int hours = Integer.parseInt(tfEst.getText());

            Task task = new Task(
                    0,
                    tfTitle.getText(),
                    tfSubject.getText(),
                    dpDeadline.getValue(),
                    hours,
                    false
            );

            if (onTaskCreated != null) {
                onTaskCreated.accept(task);
            }

            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Estimated hours must be a number.");
        }
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
