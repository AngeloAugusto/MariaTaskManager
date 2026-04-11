package dev.axxiv.mariaTasksApi;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.axxiv.mariaTasksApi.dto.TaskDTO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskOnce;
import pt.axxiv.mariatasks.data.User;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	private TaskDAO taskDao;
	private UserDAO userDao;

	public TaskController() {
		this.taskDao = new TaskDAO();
		this.userDao = new UserDAO();
	}

	@GetMapping()
	public ResponseEntity<?> getMyTasks(@RequestHeader(value = "Authorization", required = false) String token) {
		try {
			// Validate token
			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required");
			}

			// Remove "Bearer " prefix if present
			String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;

			if (bearerToken.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
			}

			// Find user by bearer token
			User user = userDao.findByBearerToken(bearerToken);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
			}

			// Get tasks for this user
			List<Task> userTasks = taskDao.findAllOpenByUser(user.getId());
			List<TaskDTO> taskDTOs = userTasks.stream().map(TaskDTO::fromTask).collect(Collectors.toList());

			return ResponseEntity.ok(taskDTOs);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getTaskById(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String token) {
		try {
			// Validate token
			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required");
			}

			// Remove "Bearer " prefix if present
			String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;

			if (bearerToken.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
			}

			// Find user by bearer token
			User user = userDao.findByBearerToken(bearerToken);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
			}

			// Get tasks for this user
			ObjectId taskObjectId;
			try {
				taskObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid task ID format: " + id);
			}
			Task task = taskDao.findById(taskObjectId);

			if (task == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			return ResponseEntity.ok(TaskDTO.fromTask(task));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}

	@PostMapping("/{id}")
	public ResponseEntity<?> updateTask(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String token, @RequestBody TaskDTO taskDTO) {
		try {
			// Validate token
			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required");
			}

			// Remove "Bearer " prefix if present
			String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;

			if (bearerToken.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
			}

			// Find user by bearer token
			User user = userDao.findByBearerToken(bearerToken);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
			}

			// Find the task to update
			ObjectId taskObjectId;
			try {
				taskObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid task ID format: " + id);
			}
			Task existingTask = taskDao.findById(taskObjectId);

			if (existingTask == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			// Verify that the task belongs to the authenticated user
			if (!existingTask.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to edit this task");
			}

			// Verify if the task is not closed
			if (existingTask.getCloseDate() != null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't edit a task that is already closed");
			}

			// Update task fields
			if (taskDTO.getTitle() != null) {
				existingTask.setTitle(taskDTO.getTitle());
			}
			if (taskDTO.getNotes() != null) {
				existingTask.setNotes(taskDTO.getNotes());
			}
			if (taskDTO.getStartDate() != null) {
				existingTask.setStartDate(taskDTO.getStartDate());
			}
			if (taskDTO.getCloseDate() != null) {
				existingTask.setCloseDate(taskDTO.getCloseDate());
			}

			// Save the updated task
			Task updatedTask = taskDao.update(existingTask);

			// Convert to DTO and return
			TaskDTO responseDTO = TaskDTO.fromTask(updatedTask);

			return ResponseEntity.ok(responseDTO);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating task: " + e.getMessage());
		}
	}

	@PostMapping("/{id}/done")
	public ResponseEntity<?> updateTaskDone(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String token) {
		try {
			// Validate token
			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required");
			}

			// Remove "Bearer " prefix if present
			String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;

			if (bearerToken.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
			}

			// Find user by bearer token
			User user = userDao.findByBearerToken(bearerToken);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
			}

			// Find the task to update
			ObjectId taskObjectId;
			try {
				taskObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid task ID format: " + id);
			}
			Task existingTask = taskDao.findById(taskObjectId);

			if (existingTask == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			// Verify that the task belongs to the authenticated user
			if (!existingTask.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to edit this task");
			}

			// Verify if the task is not closed
			if (existingTask.getCloseDate() != null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You can't close a task that is already closed");
			}

			existingTask.setClosed();

			// Save the updated task
			Task updatedTask = taskDao.update(existingTask);
			Task newTask = TaskFactory.createRollingTask(updatedTask);
			taskDao.insert(newTask);

			// Convert to DTO and return
			TaskDTO responseDTO = TaskDTO.fromTask(updatedTask);

			return ResponseEntity.ok(responseDTO);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating task: " + e.getMessage());
		}
	}
}
