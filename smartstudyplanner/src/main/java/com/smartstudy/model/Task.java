package com.smartstudy.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String subject;
    private LocalDate deadline;
    private int estimatedHours;
    private boolean completed;

    public Task() {}

    public Task(int id, String title, String subject, LocalDate deadline, int estimatedHours, boolean completed) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.deadline = deadline;
        this.estimatedHours = estimatedHours;
        this.completed = completed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public int getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
