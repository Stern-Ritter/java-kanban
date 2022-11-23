package utils.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import model.Subtask;
import model.Task;
import service.TaskManager;
import utils.Status;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class TaskSerializer implements JsonDeserializer<Task> {
    private TaskManager taskManager;

    public TaskSerializer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public Task deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : taskManager.getNextTaskId();
            String name = jsonObject.get("name").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = context.deserialize(jsonObject.get("status"), Status.class);
            int duration = jsonObject.get("duration").getAsInt();
            LocalDateTime startTime = context.deserialize(jsonObject.get("startTime"), LocalDateTime.class);
            Integer epicId = jsonObject.has("epicId") ? jsonObject.get("epicId").getAsInt() : null;

            if (epicId == null) {
                return new Task(id, name, description, status, duration, startTime);
            } else {
                return new Subtask(id, name, description, status, duration, startTime, epicId);
            }
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
