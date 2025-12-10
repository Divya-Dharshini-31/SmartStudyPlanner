package com.smartstudy.utils;

import com.smartstudy.model.Task;

import java.time.LocalDate;
import java.util.List;

public class ProductivityCalculator {
    // Simple score: (completed/total)*80 + (onTimeRatio*20)
    public static int calculateScore(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return 0;
        long total = tasks.size();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long onTime = tasks.stream().filter(t -> t.isCompleted() && !t.getDeadline().isAfter(LocalDate.now())).count();
        double score = (completed * 1.0 / total) * 80 + (total == 0 ? 0 : (onTime * 1.0 / total) * 20);
        return (int)Math.round(score);
    }
}
