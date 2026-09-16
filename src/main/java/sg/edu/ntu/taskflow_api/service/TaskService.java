package sg.edu.ntu.taskflow_api.service;
import sg.edu.ntu.taskflow_api.model.Task;
import java.util.List;

// Interface for Task service operations
public interface TaskService {
    public List<Task> findAllTasks();
    public Task findTaskById(Long id);
    public Task createTask(Task task);
    public Task updateTask(Long id, Task task);
    public void deleteTask(Long id);
    public void markTaskAsComplete(Long id);

}
