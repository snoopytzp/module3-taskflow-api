package sg.edu.ntu.taskflow_api.config;
import org.springframework.stereotype.Component;
//import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;

@Component
public class DataLoader {
  private TaskRepository taskRepository;

  public DataLoader(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @PostConstruct
  public void loadData() {
    // Clear the database first
    taskRepository.deleteAll();

    // Load seed data
    taskRepository.save(new Task("Task 1", "Analyze System Hang"));
    taskRepository.save(new Task("Task 2", "Error code 500 on login"));
    taskRepository.save(new Task("Task 3", "Database connection issue"));
    taskRepository.save(new Task("Task 4", "Unable to send email notifications"));
  }
}