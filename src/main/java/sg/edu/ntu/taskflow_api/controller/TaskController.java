package sg.edu.ntu.taskflow_api.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;

@RestController 
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ChatClient chatClient;

    public TaskController(TaskService taskService, ChatClient.Builder chatClientBuilder) {
        this.taskService = taskService;
        this.chatClient = chatClientBuilder
                .defaultSystem("You summarise task lists for a busy project manager. "
                        + "Reply in plain English, at most three short sentences. "
                        + "Say what remains in each status and note task priorities.")
                .build();
    }

    @GetMapping
    public ResponseEntity<List<Task>> findAllTasks() {
        List<Task> tasks = taskService.findAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findTaskById(@PathVariable Long id) {
        Task task = taskService.findTaskById(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PostMapping 
    public ResponseEntity<Object> createTask(@RequestBody @Valid Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@PathVariable Long id, @RequestBody @Valid Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Object> markTaskAsComplete(@PathVariable Long id) {
        taskService.markTaskAsComplete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/summary")
    public ResponseEntity<String> summary() {
        List<Task> tasks = taskService.findAllTasks();

        if (tasks.isEmpty()) {
            return new ResponseEntity<>("There are no tasks yet.", HttpStatus.OK);
        }

        String taskList = tasks.stream()
                .map(task -> "- [" + task.getStatus().toUpperCase() + "] ["
                        + task.getPriority().toUpperCase() + "] "
                        + task.getTitle() + ": " + task.getDescription())
                .collect(Collectors.joining("\n"));

        String summary = chatClient.prompt()
                .user(u -> u.text("Summarise the following tasks:\n{tasks}")
                        .param("tasks", taskList))
                .call()
                .content();

        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}
