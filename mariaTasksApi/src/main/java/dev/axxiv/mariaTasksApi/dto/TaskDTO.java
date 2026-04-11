package dev.axxiv.mariaTasksApi.dto;

import java.util.Date;

import pt.axxiv.mariatasks.data.Task;

public class TaskDTO {
    private String id;
    private String title;
    private String notes;
    private Date startDate;
    private Date closeDate;
    private String parent;
    private String sectionId;
    private String sectionTitle;
    private String timeOfTheDay;
    private String ownerId;
    
    // Constructor from Task entity
    public static TaskDTO fromTask(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId() == null ? null : task.getId().toString());
        dto.setTitle(task.getTitle() == null ? null : task.getTitle());
        dto.setNotes(task.getNotes() == null ? null : task.getNotes());
        dto.setStartDate(task.getStartDate());
        dto.setCloseDate(task.getCloseDate());
        dto.setParent(task.getParent() == null ? null : task.getParent().toString());
        dto.setSectionId(task.getSectionClass().getId().toString());
        dto.setSectionTitle(task.getSectionClass().getTitle().toString());
        dto.setTimeOfTheDay(task.timeFormated());
        dto.setOwnerId(task.getOwnerId() == null ? null : task.getOwnerId().toString());
        return dto;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public String isTimeOfTheDay() {
		return timeOfTheDay;
	}

	public void setTimeOfTheDay(String timeOfTheDay) {
		this.timeOfTheDay = timeOfTheDay;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
    
    
    
    
}
