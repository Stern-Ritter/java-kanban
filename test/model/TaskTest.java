package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    private static final int TASK_ID = 1;
    private static final String TASK_NAME = "Начать заниматься спортом.";
    private static final String TASK_DESCRIPTION = "Пойти в спортзал.";
    private static final int TASK_DURATION = 60;
    private static final LocalDateTime TASK_START_TIME = LocalDateTime.parse("08.11.2022 15:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    @Test
    void getStartTime() {
        final Task task = new Task(TASK_ID,
                TASK_NAME,
                TASK_DESCRIPTION,
                TASK_DURATION,
                TASK_START_TIME);

        final LocalDateTime startTime = task.getStartTime();
        assertEquals(TASK_START_TIME, startTime, "The task start time does not match.");
    }

    @Test
    void getDuration() {
        final Task task = new Task(TASK_ID,
                TASK_NAME,
                TASK_DESCRIPTION,
                TASK_DURATION,
                TASK_START_TIME);

        final int duration = task.getDuration();
        assertEquals(TASK_DURATION, duration, "The task duration does not match.");
    }

    @Test
    void getEndTime() {
        final Task task = new Task(TASK_ID,
                TASK_NAME,
                TASK_DESCRIPTION,
                TASK_DURATION,
                TASK_START_TIME);

        final LocalDateTime endTime = task.getEndTime();
        assertEquals(TASK_START_TIME.plusMinutes(TASK_DURATION), endTime, "The task end time does not match.");
    }
}