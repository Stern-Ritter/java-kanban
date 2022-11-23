package utils.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import model.Epic;
import model.Subtask;
import service.TaskManager;
import utils.Status;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EpicSerializer implements JsonDeserializer<Epic> {
    private TaskManager taskManager;

    public EpicSerializer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public Epic deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : taskManager.getNextTaskId();
            String name = jsonObject.get("name").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = context.deserialize(jsonObject.get("status"), Status.class);
            LocalDateTime startTime = context.deserialize(jsonObject.get("startTime"), LocalDateTime.class);
            List<Subtask> subtasks = new ArrayList<>(Arrays.asList(context.deserialize(jsonObject.get("subtasks"), Subtask[].class)));

            Epic epic = new Epic(id, name, description, status, startTime);
            epic.setSubtasks(subtasks);
            return epic;
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
