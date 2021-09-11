package duke.tasks;

import duke.Duke;
import duke.exceptions.DukeEmptyDescriptionException;
import duke.exceptions.DukeEmptyTimeException;
import duke.exceptions.DukeExceedMaxTaskException;
import duke.exceptions.DukeInvalidTaskIndex;
import duke.exceptions.DukeTaskAlreadyCompletedException;
import duke.exceptions.DukeMissingKeywordException;

import java.io.FileWriter;
import java.io.IOException;

public class TaskManager {
    public static final int MAX_TASKS = 100;
    private int numberOfTasks;
    private final Task[] tasks;

    public TaskManager() {
        numberOfTasks = 0;
        tasks = new Task[MAX_TASKS];
    }

    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public void addLoadedTask(String[] taskDetails) {
        switch (taskDetails[0].trim()) {
        case "T":
            boolean isDone = taskDetails[1].trim().equalsIgnoreCase("1");
            Task todo = new Todo(taskDetails[2].trim(), isDone);
            tasks[numberOfTasks] = todo;
            numberOfTasks++;
            break;
        case "D":
            isDone = taskDetails[1].trim().equalsIgnoreCase("1");
            Task deadline = new Deadline(taskDetails[2].trim(), taskDetails[3].trim(), isDone);
            tasks[numberOfTasks] = deadline;
            numberOfTasks++;
            break;
        case "E":
            isDone = taskDetails[1].trim().equalsIgnoreCase("1");
            Task event = new Event(taskDetails[2].trim(), taskDetails[3].trim(), isDone);
            tasks[numberOfTasks] = event;
            numberOfTasks++;
            break;
        default:
            System.out.println("SOME ERROR HAS OCCURRED!");
        }
    }

    public void addTodo(String input) throws DukeEmptyDescriptionException,
            DukeExceedMaxTaskException,
            IOException {
        if (numberOfTasks >= MAX_TASKS) {
            throw new DukeExceedMaxTaskException();
        }
        if (input.isEmpty()) {
            throw new DukeEmptyDescriptionException();
        }
        Task todo = new Todo(input);
        tasks[numberOfTasks] = todo;
        numberOfTasks++;
        acknowledgeCommand(todo);
        FileWriter fw = new FileWriter(Duke.DATA_FILE,true);
        fw.write(Duke.NL + todo.toData());
        fw.close();
    }


    public void addDeadline(String input) throws DukeEmptyDescriptionException,
            DukeExceedMaxTaskException,
            DukeEmptyTimeException,
            DukeMissingKeywordException,
            IOException {
        if (numberOfTasks >= MAX_TASKS) {
            throw new DukeExceedMaxTaskException();
        }
        final int indexOfByPrefix = getIndexOfByPrefix(input);
        String deadlineDescription = getDeadlineDescription(input, indexOfByPrefix);
        String deadlineBy = getDeadlineBy(input, indexOfByPrefix);
        Task deadline = new Deadline(deadlineDescription, deadlineBy);
        tasks[numberOfTasks] = deadline;
        numberOfTasks++;
        acknowledgeCommand(deadline);
        FileWriter fw = new FileWriter(Duke.DATA_FILE,true);
        fw.write(Duke.NL + deadline.toData());
        fw.close();
    }

    private String getDeadlineBy(String input, int indexOfByPrefix) throws DukeEmptyTimeException {
        String deadlineBy = input.substring(indexOfByPrefix + 3).trim();
        if (deadlineBy.isEmpty()) {
            throw new DukeEmptyTimeException();
        }
        return deadlineBy;
    }

    private String getDeadlineDescription(String input, int indexOfByPrefix) throws
            DukeEmptyDescriptionException {
        String deadlineDescription = input.substring(0, indexOfByPrefix).trim();
        if (deadlineDescription.isEmpty()) {
            throw new DukeEmptyDescriptionException();
        }
        return deadlineDescription;
    }

    private int getIndexOfByPrefix(String input) throws DukeMissingKeywordException {
        int indexOfByPrefix = input.indexOf(Duke.DEADLINE_BY_PREFIX);
        if (indexOfByPrefix == -1) {
            throw new DukeMissingKeywordException("/by");
        }
        return indexOfByPrefix;
    }

    public void addEvent(String input) throws DukeEmptyDescriptionException,
            DukeExceedMaxTaskException,
            DukeEmptyTimeException,
            DukeMissingKeywordException,
            IOException {
        if (numberOfTasks >= MAX_TASKS) {
            throw new DukeExceedMaxTaskException();
        }
        final int indexOfAtPrefix = getIndexOfAtPrefix(input);
        String eventDescription = getEventDescription(input, indexOfAtPrefix);
        String eventAt = getEventAt(input, indexOfAtPrefix);
        Task event = new Event(eventDescription, eventAt);
        tasks[numberOfTasks] = event;
        numberOfTasks++;
        acknowledgeCommand(event);
        FileWriter fw = new FileWriter(Duke.DATA_FILE,true);
        fw.write(Duke.NL + event.toData());
        fw.close();
    }

    private String getEventAt(String input, int indexOfAtPrefix) throws DukeEmptyTimeException {
        String eventAt = input.substring(indexOfAtPrefix + 3).trim();
        if (eventAt.isEmpty()) {
            throw new DukeEmptyTimeException();
        }
        return eventAt;
    }

    private String getEventDescription(String input, int indexOfAtPrefix) throws
            DukeEmptyDescriptionException {
        String eventDescription = input.substring(0, indexOfAtPrefix).trim();
        if (eventDescription.isEmpty()) {
            throw new DukeEmptyDescriptionException();
        }
        return eventDescription;
    }

    private int getIndexOfAtPrefix(String input) throws DukeMissingKeywordException {
        final int indexOfAtPrefix = input.indexOf(Duke.EVENT_AT_PREFIX);
        if (indexOfAtPrefix == -1) {
            throw new DukeMissingKeywordException("/at");
        }
        return indexOfAtPrefix;
    }

    private void acknowledgeCommand(Task task) {
        String acknowledgementMessage = "Understood, "
                + Duke.NL + task.toString()
                + Duke.NL + "has been added. You now have "
                + numberOfTasks + " " + "task(s) in the list";
        Duke.printMessage(acknowledgementMessage);
    }

    public void printTasks() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < numberOfTasks; i++) {
            list.append(i + 1).append(".");
            list.append(tasks[i].toString());
            if (i < numberOfTasks - 1) {
                list.append(Duke.NL);
            }
        }
        Duke.printMessage(list.toString());
    }

    public void setTaskAsDone(int taskNumber) throws DukeInvalidTaskIndex,
            DukeTaskAlreadyCompletedException,
            IOException {
        if (taskNumber > numberOfTasks || taskNumber <= 0) {
            throw new DukeInvalidTaskIndex();
        }
        if (tasks[taskNumber - 1].isDone()) {
            throw new DukeTaskAlreadyCompletedException();
        }

        tasks[taskNumber - 1].setDone();
        Duke.printMessage("Good Job!! I've marked this task as done:" + Duke.NL
                + tasks[taskNumber - 1].toString());
        
        FileWriter fw = new FileWriter(Duke.DATA_FILE, false);
        for (int i = 0; i < numberOfTasks; i++) {
            fw.write(tasks[i].toData() + ((i >= numberOfTasks - 1) ? "" : Duke.NL));
        }
        fw.close();
    }

}
