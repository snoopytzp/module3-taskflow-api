package sg.edu.ntu.taskflow_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;

class TaskServiceImplTests {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final TaskServiceImpl taskService = new TaskServiceImpl(taskRepository);

    @Test
    void updateTask_UpdatesStatusAndPriority() {
        Task existingTask = new Task("Existing title", "Existing description");
        Task taskUpdate = new Task("Updated title", "Updated description");
        taskUpdate.setStatus("in-progress");
        taskUpdate.setPriority("high");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updatedTask = taskService.updateTask(1L, taskUpdate);

        assertEquals("in-progress", updatedTask.getStatus());
        assertEquals("high", updatedTask.getPriority());
        verify(taskRepository).save(existingTask);
    }

    @Test
    void markTaskAsComplete_SetsStatusToDone() {
        Task task = new Task("Complete task", "Mark it done");
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        taskService.markTaskAsComplete(2L);

        assertEquals("done", task.getStatus());
        verify(taskRepository).save(task);
    }
}
