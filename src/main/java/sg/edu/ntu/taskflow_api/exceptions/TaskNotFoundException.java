package sg.edu.ntu.taskflow_api.exceptions;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Could not find task with ID: " + id);
    }
}
