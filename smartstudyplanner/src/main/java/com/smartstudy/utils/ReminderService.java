package com.smartstudy.utils;

import com.smartstudy.model.Task;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

public class ReminderService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Callable<Void> checkTask;

    public ReminderService(Supplier<List<Task>> tasksSupplier) {
        checkTask = () -> {
            List<Task> tasks = tasksSupplier.get();
            LocalDate today = LocalDate.now();
            for (Task t : tasks) {
                if (!t.isCompleted() && t.getDeadline() != null) {
                    if (t.getDeadline().isEqual(today)) {
                        Platform.runLater(() -> {
                            Alert a = new Alert(Alert.AlertType.INFORMATION);
                            a.setHeaderText("Task Due Today");
                            a.setContentText(t.getTitle() + " is due today.");
                            a.show();
                        });
                    } else if (t.getDeadline().isBefore(today)) {
                        Platform.runLater(() -> {
                            Alert a = new Alert(Alert.AlertType.WARNING);
                            a.setHeaderText("Overdue Task");
                            a.setContentText(t.getTitle() + " is overdue!");
                            a.show();
                        });
                    }
                }
            }
            return null;
        };
    }

    public void startEvery(long initialDelaySec, long periodSec) {
        scheduler.scheduleAtFixedRate(() -> {
            try { checkTask.call(); } catch (Exception e) { e.printStackTrace(); }
        }, initialDelaySec, periodSec, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public interface Supplier<T> { T get(); }
}
