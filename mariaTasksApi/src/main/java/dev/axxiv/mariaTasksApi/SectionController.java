package dev.axxiv.mariaTasksApi;

import java.util.List;
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

import dev.axxiv.mariaTasksApi.dto.SectionDTO;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.User;

@RestController
@RequestMapping("/sections")
public class SectionController {

	private UserDAO userDao;
	private SectionDAO sectionDao;
	private TaskDAO taskDao;

	public SectionController() {
		this.userDao = new UserDAO();
		this.sectionDao = new SectionDAO();
		this.taskDao = new TaskDAO();
	}

	@GetMapping()
	public ResponseEntity<?> getSections(@RequestHeader(value = "Authorization", required = false) String token) {
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

			// Get sections for this user
			List<Section> userSections = sectionDao.findAllByUser(user.getId());
			List<SectionDTO> sectionDTOs = userSections.stream().map(SectionDTO::fromSection).collect(Collectors.toList());

			return ResponseEntity.ok(sectionDTOs);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching sections: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getSectionById(@PathVariable String id,
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

			// Get sections for this user
			ObjectId sectionObjectId;
			try {
				sectionObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid section ID format: " + id);
			}
			Section section = sectionDao.findById(sectionObjectId);

			if (section == null || !section.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Section not found");
			}

			return ResponseEntity.ok(SectionDTO.fromSection(section));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching sections: " + e.getMessage());
		}
	}

	@PostMapping("/new")
	public ResponseEntity<?> createSection(
			@RequestHeader(value = "Authorization", required = false) String token, @RequestBody SectionDTO sectionDTO) {
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
			
			
			Section section = new Section();
			
			section.setOwnerId(user.getId());
			
			// Update section fields
			if (sectionDTO.getTitle() != null) {
				if(sectionDao.findByTitle(sectionDTO.getTitle(), user.getId()) != null) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Section title already exists");
				}
				
				section.setTitle(sectionDTO.getTitle());
			}else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Section must have a title");
			}
			
			if (sectionDTO.getIcon() != null) {
				section.setIcon(sectionDTO.getIcon());
			}

			// Save the created section
			Section createSection = sectionDao.insert(section);

			// Convert to DTO and return
			SectionDTO responseDTO = SectionDTO.fromSection(createSection);

			return ResponseEntity.ok(responseDTO);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error creating section: " + e.getMessage());
		}
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<?> updateSection(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String token, @RequestBody SectionDTO sectionDTO) {
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

			// Find the section to update
			ObjectId sectionObjectId;
			try {
				sectionObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid section ID format: " + id);
			}
			Section existingSection = sectionDao.findById(sectionObjectId);

			if (existingSection == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Section not found");
			}

			// Verify that the section belongs to the authenticated user
			if (!existingSection.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to edit this section");
			}

			// Update section fields
			if (sectionDTO.getTitle() != null) {
				existingSection.setTitle(sectionDTO.getTitle());
			}
			if (sectionDTO.getIcon() != null) {
				existingSection.setIcon(sectionDTO.getIcon());
			}

			// Save the updated section
			Section updatedSection = sectionDao.update(existingSection);

			// Convert to DTO and return
			SectionDTO responseDTO = SectionDTO.fromSection(updatedSection);

			return ResponseEntity.ok(responseDTO);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating section: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteSectionById(@PathVariable String id,
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

			// Get section for this user
			ObjectId sectionObjectId;
			try {
				sectionObjectId = new ObjectId(id);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid section ID format: " + id);
			}
			Section section = sectionDao.findById(sectionObjectId);

			if (section == null || !section.getOwnerId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Section not found");
			}
			
			taskDao.deleteFromSection(sectionObjectId);
			sectionDao.delete(sectionObjectId);

	        return ResponseEntity.ok("Section deleted successfully");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching sections: " + e.getMessage());
		}
	}
}
