package com.smartstudy.controller;

import com.smartstudy.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class TasksController {

    @FXML private TableView<Task> table;
    @FXML private TableColumn<Task, Integer> colId;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colSubject;
    @FXML private TableColumn<Task, LocalDate> colDeadline;
    @FXML private TableColumn<Task, Integer> colEst;
    @FXML private TableColumn<Task, Boolean> colCompleted;
    @FXML private Button btnAdd, btnClose, btnDelete, btnEdit, btnMark;
    @FXML private TextField tfSearch;

    private ObservableList<Task> list = FXCollections.observableArrayList();
    private Consumer<List<Task>> onChange;
    private Consumer<Task> addContext;

    public void setTasksList(List<Task> tasks) {
        list.setAll(tasks);
        table.setItems(list);
    }

    public void setOnChangeListener(Consumer<List<Task>> onChange) {
        this.onChange = onChange;
    }

    public void setContext(Consumer<Task> addContext) {
        this.addContext = addContext;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colEst.setCellValueFactory(new PropertyValueFactory<>("estimatedHours"));
        colCompleted.setCellValueFactory(new PropertyValueFactory<>("completed"));

        btnAdd.setOnAction(e -> onAdd());
        btnClose.setOnAction(e -> onClose());
        btnDelete.setOnAction(e -> onDelete());
        btnEdit.setOnAction(e -> onEdit());
        btnMark.setOnAction(e -> onMarkComplete());
        tfSearch.textProperty().addListener((obs, oldV, newV) -> onSearch(newV));
    }

    private void onAdd() {
        // open add dialog (reuse same view by opening the add_task.fxml controller)
        try {
            Stage dialog = new Stage();
            dialog.initOwner(table.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/add_task.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            Object ctrl = loader.getController();
            if (ctrl instanceof TasksController) {
                TasksController ctrl2 = (TasksController) ctrl;
                ctrl2.setContext(task -> {
                    // receive new task from dialog
                    task.setId(generateTempId());
                    list.add(task);
                    if (onChange != null) onChange.accept(list);
                    dialog.close();
                });
            } else {
                // if add_task has a specialized controller, you can adapt here.
            }
            dialog.setScene(scene);
            dialog.setTitle("Add Task");
            dialog.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Note: the Add dialog also uses TasksController in this simple implementation;
    // alternatively you could make a separate AddTaskController class.

    private int generateTempId() {
        return list.stream().mapToInt(Task::getId).max().orElse(0) + 1;
    }

    private void onDelete() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Select a task first");
            return;
        }
        list.remove(sel);
        if (onChange != null) onChange.accept(list);
    }

    private void onEdit() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Select a task first"); return; }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/add_task.fxml"));
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof TasksController) {
                TasksController addCtrl = (TasksController) ctrl;
                // populate fields by reflection: use dialog's nodes
                Dialog<ButtonType> d = new Dialog<>();
                d.getDialogPane().setContent(root);
                d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                // set fields using lookup
                TextField tfTitle = (TextField)d.getDialogPane().lookup("#tfTitle");
                TextField tfSub = (TextField)d.getDialogPane().lookup("#tfSubject");
                DatePicker dp = (DatePicker)d.getDialogPane().lookup("#dpDeadline");
                TextField tfEst = (TextField)d.getDialogPane().lookup("#tfEst");
                tfTitle.setText(sel.getTitle());
                tfSub.setText(sel.getSubject());
                dp.setValue(sel.getDeadline());
                tfEst.setText(String.valueOf(sel.getEstimatedHours()));
                if (d.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent()) {
                    sel.setTitle(tfTitle.getText());
                    sel.setSubject(tfSub.getText());
                    sel.setDeadline(dp.getValue());
                    sel.setEstimatedHours(Integer.parseInt(tfEst.getText().isBlank() ? "1" : tfEst.getText()));
                    table.refresh();
                    if (onChange != null) onChange.accept(list);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onMarkComplete() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Select a task first"); return; }
        sel.setCompleted(!sel.isCompleted());
        table.refresh();
        if (onChange != null) onChange.accept(list);
    }

    private void onClose() {
        Stage s = (Stage) table.getScene().getWindow();
        s.close();
    }

    private void onSearch(String q) {
        if (q == null || q.isBlank()) {
            table.setItems(list);
            return;
        }
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : list) {
            if (t.getTitle().toLowerCase().contains(q.toLowerCase()) ||
                (t.getSubject() != null && t.getSubject().toLowerCase().contains(q.toLowerCase()))) {
                filtered.add(t);
            }
        }
        table.setItems(filtered);
    }

    private void show(String txt) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(txt);
        a.show();
    }
}
