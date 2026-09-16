package sg.edu.ntu.taskflow_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.ntu.taskflow_api.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
