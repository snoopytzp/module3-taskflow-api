package sg.edu.ntu.taskflow_api.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;
import java.util.List;
import sg.edu.ntu.taskflow_api.exceptions.TaskNotFoundException;

@Primary 
@Service 
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task findTaskById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        // Update the existing task with the new values from the input task object
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());
        return taskRepository.save(existingTask);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.deleteById(id);
    }

    @Override
    public void markTaskAsComplete(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        task.setCompleted(true);
        taskRepository.save(task);
    }
}
