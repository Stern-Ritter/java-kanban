package utils.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import exceptions.BadRequestException;
import model.Subtask;
import service.TaskManager;
import utils.Status;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class SubtaskSerializer implements JsonDeserializer<Subtask> {
    private TaskManager taskManager;

    public SubtaskSerializer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public Subtask deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : taskManager.getNextTaskId();
            String name = jsonObject.get("name").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = context.deserialize(jsonObject.get("status"), Status.class);
            int duration = jsonObject.get("duration").getAsInt();
            LocalDateTime startTime = context.deserialize(jsonObject.get("startTime"), LocalDateTime.class);
            int epicId = jsonObject.get("epicId").getAsInt();

            return new Subtask(id, name, description, status, duration, startTime, epicId);
        } catch (NullPointerException ex) {
            throw new BadRequestException("Некорректный формат данных в теле запроса.", ex);
        }
    }
}
