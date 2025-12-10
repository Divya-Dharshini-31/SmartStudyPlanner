package com.smartstudy.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.smartstudy.model.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) ->
                    LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .setPrettyPrinting()
            .create();

    public static List<Task> readTasks(String path) {
        try {
            if (!Files.exists(Paths.get(path))) return new ArrayList<>();
            Reader r = new FileReader(path);
            Type listType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(r, listType);
            r.close();
            return tasks == null ? new ArrayList<>() : tasks;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void writeTasks(String path, List<Task> tasks) {
        try (Writer w = new FileWriter(path)) {
            gson.toJson(tasks, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
