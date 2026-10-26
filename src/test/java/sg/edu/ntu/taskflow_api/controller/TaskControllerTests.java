package sg.edu.ntu.taskflow_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.ntu.taskflow_api.exceptions.GlobalExceptionHandler;
import sg.edu.ntu.taskflow_api.exceptions.TaskNotFoundException;
import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;

@WebMvcTest(controllers = TaskController.class, properties =
        "app.cors.allowed-origins=http://localhost:4173,https://taskflow.example.com")
@Import({GlobalExceptionHandler.class, TaskControllerTests.ChatClientTestConfiguration.class})
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ChatClient chatClient;

        @ParameterizedTest
        @ValueSource(strings = {"http://localhost:4173", "https://taskflow.example.com"})
        void findAllTasks_FromAllowedOrigin_ReturnsCorsHeader(String origin) throws Exception {
                when(taskService.findAllTasks()).thenReturn(List.of());

                mockMvc.perform(get("/api/tasks").header(HttpHeaders.ORIGIN, origin))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"})
        void preflight_FromAllowedOrigin_AllowsApiMethodsAndHeaders(String method) throws Exception {
                mockMvc.perform(options("/api/tasks/1")
                                                .header(HttpHeaders.ORIGIN, "http://localhost:4173")
                                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
                                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,authorization,accept"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4173"))
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString(method)))
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")))
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")))
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("accept")));

                verifyNoInteractions(taskService);
        }

        @Test
        void findAllTasks_FromUnlistedOrigin_IsForbidden() throws Exception {
                mockMvc.perform(get("/api/tasks").header(HttpHeaders.ORIGIN, "https://untrusted.example.com"))
                                .andExpect(status().isForbidden())
                                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

                verifyNoInteractions(taskService);
        }

        @Test
        void preflight_FromUnlistedOrigin_IsForbidden() throws Exception {
                mockMvc.perform(options("/api/tasks")
                                                .header(HttpHeaders.ORIGIN, "https://untrusted.example.com")
                                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                                .andExpect(status().isForbidden())
                                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

                verifyNoInteractions(taskService);
        }

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
                .andExpect(jsonPath("$[0].status").value("todo"))
                .andExpect(jsonPath("$[0].priority").value("low"));
    }

    @Test
    void findTaskById_WhenTaskExists_ReturnsTask() throws Exception {
        Task task = new Task("Write tests", "Cover the task API");
        task.setId(7L);
        when(taskService.findTaskById(7L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("todo"))
                .andExpect(jsonPath("$.priority").value("low"));
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
                .andExpect(jsonPath("$.status").value("todo"))
                .andExpect(jsonPath("$.priority").value("low"));

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
        updatedTask.setStatus("in-progress");
        updatedTask.setPriority("high");
        when(taskService.updateTask(eq(5L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","description":"Updated description","status":"in-progress","priority":"high"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.status").value("in-progress"))
                .andExpect(jsonPath("$.priority").value("high"));

        verify(taskService).updateTask(eq(5L), any(Task.class));
    }

    @Test
    void createTask_WithInvalidStatusOrPriority_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Write tests","description":"Cover the task API","status":"blocked","priority":"urgent"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Status must be todo, in-progress, or done")))
                .andExpect(jsonPath("$.message").value(containsString("Priority must be low, medium, or high")));

        verifyNoInteractions(taskService);
    }

    @ParameterizedTest
    @MethodSource("invalidTaskFields")
    void createAndUpdateTask_WithInvalidField_ReturnBadRequest(
            String field, String jsonValue, String expectedMessage) throws Exception {
        String body = """
                {"title":"Write tests","description":"Cover the task API","%s":%s}
                """.formatted(field, jsonValue);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(expectedMessage)));

        mockMvc.perform(put("/api/tasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(expectedMessage)));

        verifyNoInteractions(taskService);
    }

    static Stream<Arguments> invalidTaskFields() {
        return Stream.of(
                Arguments.of("status", "\"blocked\"", "Status must be todo, in-progress, or done"),
                Arguments.of("status", "\"TODO\"", "Status must be todo, in-progress, or done"),
                Arguments.of("status", "\" todo \"", "Status must be todo, in-progress, or done"),
                Arguments.of("status", "\"\"", "Status is mandatory"),
                Arguments.of("status", "\" \"", "Status is mandatory"),
                Arguments.of("status", "null", "Status is mandatory"),
                Arguments.of("priority", "\"urgent\"", "Priority must be low, medium, or high"),
                Arguments.of("priority", "\"HIGH\"", "Priority must be low, medium, or high"),
                Arguments.of("priority", "\" high \"", "Priority must be low, medium, or high"),
                Arguments.of("priority", "\"\"", "Priority is mandatory"),
                Arguments.of("priority", "\" \"", "Priority is mandatory"),
                Arguments.of("priority", "null", "Priority is mandatory"));
    }

    @ParameterizedTest
    @CsvSource({
            "todo,low", "todo,medium", "todo,high",
            "in-progress,low", "in-progress,medium", "in-progress,high",
            "done,low", "done,medium", "done,high"
    })
    void createAndUpdateTask_WithAllowedValues_AcceptTask(String taskStatus, String priority) throws Exception {
        Task task = new Task("Write tests", "Cover the task API");
        task.setStatus(taskStatus);
        task.setPriority(priority);
        when(taskService.createTask(any(Task.class))).thenReturn(task);
        when(taskService.updateTask(eq(5L), any(Task.class))).thenReturn(task);
        String body = """
                {"title":"Write tests","description":"Cover the task API","status":"%s","priority":"%s"}
                """.formatted(taskStatus, priority);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(taskStatus))
                .andExpect(jsonPath("$.priority").value(priority));

        mockMvc.perform(put("/api/tasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(taskStatus))
                .andExpect(jsonPath("$.priority").value(priority));

        verify(taskService).createTask(argThat(input ->
                taskStatus.equals(input.getStatus()) && priority.equals(input.getPriority())));
        verify(taskService).updateTask(eq(5L), argThat(input ->
                taskStatus.equals(input.getStatus()) && priority.equals(input.getPriority())));
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
