package sg.edu.ntu.taskflow_api.model;

import jakarta.persistence.GeneratedValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor 
@Entity 
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private long id;
    @Column(name = "title")
    @NotBlank(message = "Title is mandatory")
    private String title;

    @Column(name = "description")
    @NotBlank(message = "Description is mandatory")
    private String description;

    // Status of the task: "todo", "in_progress", or "done"
    @Column(name = "status")
    private String status = "todo";

    // Priority of the task: "low", "medium", or "high"
    @Column(name = "priority")
    private String priority = "low";

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = "todo";
        this.priority = "low";
    }
}
