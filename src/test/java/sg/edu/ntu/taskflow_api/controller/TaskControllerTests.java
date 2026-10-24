package sg.edu.ntu.taskflow_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.ntu.taskflow_api.exceptions.GlobalExceptionHandler;
import sg.edu.ntu.taskflow_api.exceptions.TaskNotFoundException;
import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;

@WebMvcTest(TaskController.class)
@Import({GlobalExceptionHandler.class, TaskControllerTests.ChatClientTestConfiguration.class})
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ChatClient chatClient;

    @Test
    void findAllTasks_WhenTasksExist_ReturnsTasks() throws Exception {
        Task task = new Task("Write tests", "Cover the task API");
        task.setId(1L);
        when(taskService.findAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Write tests"))
                .andExpect(jsonPath("$[0].description").value("Cover the task API"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void findTaskById_WhenTaskExists_ReturnsTask() throws Exception {
        Task task = new Task("Write tests", "Cover the task API");
        task.setId(7L);
        when(taskService.findTaskById(7L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Write tests"));
    }

    @Test
    void createTask_WithValidTask_ReturnsCreatedTask() throws Exception {
        Task createdTask = new Task("Write tests", "Cover the task API");
        createdTask.setId(3L);
        when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Write tests","description":"Cover the task API"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void createTask_WithBlankRequiredFields_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","description":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Title is mandatory")))
                .andExpect(jsonPath("$.message").value(containsString("Description is mandatory")));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_WhenTaskExists_ReturnsUpdatedTask() throws Exception {
        Task updatedTask = new Task("Updated title", "Updated description");
        updatedTask.setId(5L);
        updatedTask.setCompleted(true);
        when(taskService.updateTask(eq(5L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","description":"Updated description","completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService).updateTask(eq(5L), any(Task.class));
    }

    @Test
    void deleteTask_WhenTaskExists_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/tasks/8"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(taskService).deleteTask(8L);
    }

    @Test
    void markTaskAsComplete_WhenTaskExists_ReturnsOk() throws Exception {
        mockMvc.perform(put("/api/tasks/9/complete"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(taskService).markTaskAsComplete(9L);
    }

    @Test
    void findTaskById_WhenTaskDoesNotExist_ReturnsNotFound() throws Exception {
        when(taskService.findTaskById(404L))
                .thenThrow(new TaskNotFoundException(404L));

        mockMvc.perform(get("/api/tasks/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find task with ID: 404"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getTaskSummary_WhenNoTasksExist_ReturnsEmptyMessage() throws Exception {
        when(taskService.findAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/summary"))
                .andExpect(status().isOk())
                .andExpect(content().string("There are no tasks yet."));

        verifyNoInteractions(chatClient);
    }

    @TestConfiguration
    static class ChatClientTestConfiguration {

        @Bean
        ChatClient chatClient() {
            return mock(ChatClient.class);
        }

        @Bean
        ChatClient.Builder chatClientBuilder(ChatClient chatClient) {
            ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_SELF);
            when(builder.build()).thenReturn(chatClient);
            return builder;
        }
    }
}
