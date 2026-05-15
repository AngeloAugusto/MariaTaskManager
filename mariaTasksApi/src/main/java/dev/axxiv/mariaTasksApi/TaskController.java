package dev.axxiv.mariaTasksApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.axxiv.mariaTasksApi.dto.TaskDTO;
import dev.axxiv.mariaTasksApi.dto.TaskNewDTO;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.User;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	private TaskDAO taskDao;
	private UserDAO userDao;
	private SectionDAO sectionDao;

	public TaskController() {
		this.taskDao = new TaskDAO();
		this.userDao = new UserDAO();
		this.sectionDao = new SectionDAO();
	}

	@GetMapping()
	public ResponseEntity<?> getMyOpenTasks(@RequestHeader(value = "Authorization", required = false) String token) {
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

	@GetMapping("/formats")
	public ResponseEntity<?> getTaskFormats() {
	    try {
	        List<Map<String, Object>> formats = Arrays.stream(TaskFormat.values())
	            .map(format -> {
	                Map<String, Object> formatInfo = new HashMap<>();
	                formatInfo.put("value", format.name());
	                formatInfo.put("label", format.getLabel());
	                formatInfo.put("code", format.getValue());
	                return formatInfo;
	            })
	            .collect(Collectors.toList());
	        
	        return ResponseEntity.ok(formats);
	        
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error fetching task formats: " + e.getMessage());
	    }
	}
	
	@GetMapping("/frequency-types")
	public ResponseEntity<?> getFrequencyTypes() {
	    try {
	        List<Map<String, Object>> frequencyTypes = Arrays.stream(FrequencyTypes.values())
	            .map(type -> {
	                Map<String, Object> typeInfo = new HashMap<>();
	                typeInfo.put("value", type.name());
	                typeInfo.put("label", type.getLabel());
	                typeInfo.put("code", type.getValue());
	                return typeInfo;
	            })
	            .collect(Collectors.toList());
	        
	        return ResponseEntity.ok(frequencyTypes);
	        
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error fetching frequency types: " + e.getMessage());
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

			if (task == null || !task.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			return ResponseEntity.ok(TaskDTO.fromTask(task));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}

	@GetMapping("/section/{id}")
	public ResponseEntity<?> getTaskBySection(@PathVariable String id,
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
			
			Section section = sectionDao.findById(new ObjectId(id));
			
			if (section == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Section not found");
			}
			
			// Get tasks for this user
			List<Task> userTasks = taskDao.findAllOpenByUserAndSection(section ,user.getId());
			List<TaskDTO> taskDTOs = userTasks.stream().map(TaskDTO::fromTask).collect(Collectors.toList());

			return ResponseEntity.ok(taskDTOs);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}

	@PostMapping("/new")
	public ResponseEntity<?> createTask(
	        @RequestHeader(value = "Authorization", required = false) String token, 
	        @RequestBody TaskNewDTO taskDTO) {
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

	        // Validate required fields
	        if (taskDTO.getTitle() == null || taskDTO.getTitle().isEmpty()) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task title is required");
	        }
	        
	        if (taskDTO.getFormat() == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task format is required");
	        }

	        // Get section if provided
	        ObjectId sectionId = null;
	        if (taskDTO.getSectionId() != null && !taskDTO.getSectionId().isEmpty()) {
	            try {
	                sectionId = new ObjectId(taskDTO.getSectionId());
	                Section section = sectionDao.findById(sectionId);
	                if (section == null) {
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Section not found");
	                }
	            } catch (IllegalArgumentException e) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid section ID format");
	            }
	        }
	        
	        //TODO: Verificar o tempo que a última tarefa foi criada. Se for menos de 5 segundos, bloquear.
	        //TODO: Talvez criar um contador, se for disparado várias vezes este alerta, bloquear conta

	        // Create the task using TaskFactory
	        Task createdTask = TaskFactory.createTask(
	            taskDTO.getFormat(),
	            taskDTO.getTitle(),
	            taskDTO.getNotes(),
	            sectionId,
	            user.getId(),
	            taskDTO.getTimeOfTheDay()
	        );

	        // Set additional fields if needed
	        if (createdTask instanceof TaskCustom && taskDTO.getPeriod() != null && taskDTO.getFrequencyType() != null) {
	            TaskCustom customTask = (TaskCustom) createdTask;
	            customTask.setPeriod(taskDTO.getPeriod());
	            customTask.setFrequencyTypes(taskDTO.getFrequencyType());
	        } else if (createdTask instanceof TaskDate && taskDTO.getSelectedDate() != null) {
	            TaskDate dateTask = (TaskDate) createdTask;
	            dateTask.setSelectedDate(taskDTO.getSelectedDate());
	        }

	        // Set start date if provided
	        if (taskDTO.getStartDate() != null) {
	            createdTask.setStartDate(taskDTO.getStartDate());
	        }

	        // Save the task
	        taskDao.insert(createdTask);

	        // Convert to DTO and return
	        TaskDTO responseDTO = TaskDTO.fromTask(createdTask);

	        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error creating task: " + e.getMessage());
	    }
	}
	
	@PutMapping("/{id}")
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

	@PutMapping("/{id}/done")
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
			if(newTask != null) {
				taskDao.insert(newTask);
			}

			return ResponseEntity.ok("Task closed successfully");

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating task: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteTaskById(@PathVariable String id,
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

			if (task == null || !task.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}
			
			if (task.getCloseDate() != null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You can't delete a task that is already closed");
			}
			
			taskDao.delete(taskObjectId);

	        return ResponseEntity.ok("Task deleted successfully");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}

	@GetMapping("/history")
	public ResponseEntity<?> getHistory(@RequestHeader(value = "Authorization", required = false) String token) {
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
			List<Task> userTasks = taskDao.findAllClosed(user.getId());
			List<TaskDTO> taskDTOs = userTasks.stream().map(TaskDTO::fromTask).collect(Collectors.toList());

			return ResponseEntity.ok(taskDTOs);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching tasks: " + e.getMessage());
		}
	}
}
