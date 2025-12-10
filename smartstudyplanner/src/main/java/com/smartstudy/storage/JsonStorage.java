package com.smartstudy.storage;

import com.smartstudy.model.Task;

import java.util.List;

public class JsonStorage {
    private final String filePath;

    public JsonStorage(String filePath) { this.filePath = filePath; }

    public List<Task> loadTasks() { return JsonUtils.readTasks(filePath); }
    public void saveTasks(List<Task> tasks) { JsonUtils.writeTasks(filePath, tasks); }
}
