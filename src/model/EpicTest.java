package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    private static final int EPIC_ID = 1;
    private static final String EPIC_NAME = "Начать заниматься спортом.";
    private static final String EPIC_DESCRIPTION = "Пойти в спортзал.";
    private static final LocalDateTime EPIC_START_TIME = LocalDateTime.parse("08.11.2022 15:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final int FIRST_SUBTASK_ID = 2;
    private static final String FIRST_SUBTASK_NAME = "Выбрать место тренировок.";
    private static final String FIRST_SUBTASK_DESCRIPTION = "Выбрать спортзал.";
    private static final int FIRST_SUBTASK_DURATION = 60;
    private static final LocalDateTime FIRST_SUBTASK_START_TIME = LocalDateTime.parse("08.11.2022 15:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final int SECOND_SUBTASK_ID = 3;
    private static final String SECOND_SUBTASK_NAME = "Записаться в зал.";
    private static final String SECOND_SUBTASK_DESCRIPTION = "Оплатить абонемент.";
    private static final int SECOND_SUBTASK_DURATION = 45;
    private static final LocalDateTime SECOND_SUBTASK_START_TIME = LocalDateTime.parse("08.11.2022 21:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic(EPIC_ID, EPIC_NAME, EPIC_DESCRIPTION, EPIC_START_TIME);
    }

    @Test
    void getStatusWhenSubtaskListIsEmpty() {
        final Status expectedStatus = Status.NEW;
        final Status status = epic.getStatus();
        assertEquals(expectedStatus, status, "The epic status is incorrect.");
    }

    @Test
    void getStatusWhenAllSubtaskHaveStatusNew() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final Status expectedStatus = Status.NEW;
        final Status status = epic.getStatus();
        assertEquals(expectedStatus, status, "The epic status is incorrect.");
    }

    @Test
    void getStatusWhenAllSubtaskHaveStatusDone() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        firstSubtask.setStatus(Status.DONE);
        secondSubtask.setStatus(Status.DONE);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final Status expectedStatus = Status.DONE;
        final Status status = epic.getStatus();
        assertEquals(expectedStatus, status, "The epic status is incorrect.");
    }

    @Test
    void getStatusWhenAllSubtaskHaveStatusNewAndDone() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        secondSubtask.setStatus(Status.DONE);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final Status expectedStatus = Status.IN_PROGRESS;
        final Status status = epic.getStatus();
        assertEquals(expectedStatus, status, "The epic status is incorrect.");
    }

    @Test
    void getStatusWhenAllSubtaskHaveStatusInProgress() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        firstSubtask.setStatus(Status.IN_PROGRESS);
        secondSubtask.setStatus(Status.IN_PROGRESS);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final Status expectedStatus = Status.IN_PROGRESS;
        final Status status = epic.getStatus();
        assertEquals(expectedStatus, status, "The epic status is incorrect.");
    }

    @Test
    void getStartTimeWhenHasOneSubtask() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask));

        final LocalDateTime startTime = epic.getStartTime();
        assertEquals(FIRST_SUBTASK_START_TIME, startTime, "The epic start time does not match.");
    }

    @Test
    void getDurationWhenHasOneSubtask() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask));

        final int duration = epic.getDuration();
        assertEquals(FIRST_SUBTASK_DURATION, duration, "The epic duration does not match.");
    }

    @Test
    void getEndTimeWhenHasOneSubtask() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask));

        final LocalDateTime endTime = epic.getEndTime();
        assertEquals(FIRST_SUBTASK_START_TIME.plusMinutes(FIRST_SUBTASK_DURATION), endTime, "The epic end time does not match.");
    }

    @Test
    void getStartTimeWhenHasMoreOneSubtasks() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final LocalDateTime startTime = epic.getStartTime();
        assertEquals(FIRST_SUBTASK_START_TIME, startTime, "The epic start time does not match.");
    }

    @Test
    void getDurationWhenHasMoreOneSubtasks() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final int duration = epic.getDuration();
        assertEquals(FIRST_SUBTASK_DURATION + SECOND_SUBTASK_DURATION, duration, "The epic duration does not match.");
    }

    @Test
    void getEndTimeWhenHasMoreOneSubtasks() {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID,
                FIRST_SUBTASK_NAME,
                FIRST_SUBTASK_DESCRIPTION,
                FIRST_SUBTASK_DURATION,
                FIRST_SUBTASK_START_TIME,
                EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID,
                SECOND_SUBTASK_NAME,
                SECOND_SUBTASK_DESCRIPTION,
                SECOND_SUBTASK_DURATION,
                SECOND_SUBTASK_START_TIME,
                EPIC_ID);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final LocalDateTime endTime = epic.getEndTime();
        assertEquals(SECOND_SUBTASK_START_TIME.plusMinutes(SECOND_SUBTASK_DURATION), endTime, "The epic end time does not match.");
    }

    @Test
    void getStartTimeWhenHasNoSubtasks() {
        final LocalDateTime startTime = epic.getStartTime();
        assertEquals(EPIC_START_TIME, startTime, "The epic start time does not match.");
    }

    @Test
    void getDurationWhenHasNoSubtasks() {
        final int expectedDuration = 0;
        final int duration = epic.getDuration();
        assertEquals(expectedDuration, duration, "The epic duration does not match.");
    }

    @Test
    void getEndTimeWhenHasNoSubtasks() {
        final LocalDateTime endTime = epic.getEndTime();
        assertEquals(EPIC_START_TIME, endTime, "The epic end time does not match.");
    }
}