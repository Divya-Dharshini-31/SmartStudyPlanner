package com.smartstudy.controller;

import com.smartstudy.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddTaskController {

    @FXML private TextField tfTitle;
    @FXML private TextField tfSubject;
    @FXML private DatePicker dpDeadline;
    @FXML private TextField tfEst;

    @FXML
    private void onSaveTask() {

        Task task = new Task(
                0, // id (can be auto-generated later)
                tfTitle.getText(),
                tfSubject.getText(),
                dpDeadline.getValue(),
                Integer.parseInt(tfEst.getText()),
                false // completed
        );

        // TEMPORARY TEST (VERY IMPORTANT)
        System.out.println("Task Saved:");
        System.out.println(task.getTitle() + " | " + task.getSubject());
    }
}
