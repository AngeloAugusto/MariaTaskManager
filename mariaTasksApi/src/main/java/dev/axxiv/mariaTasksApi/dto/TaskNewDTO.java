package dev.axxiv.mariaTasksApi.dto;

import java.time.LocalTime;
import java.util.Date;
import org.bson.types.ObjectId;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDaily;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.TaskOnce;
import pt.axxiv.mariatasks.data.FrequencyTypes;

public class TaskNewDTO {
    private String id;
    private String title;
    private String notes;
    private Date startDate;
    private Date closeDate;
    private String sectionId;
    private String ownerId;
    private LocalTime timeOfTheDay;
    
    // Fields for task creation
    private TaskFormat format;
    private Integer period;  // For FREQUENCY format
    private FrequencyTypes frequencyType;  // For FREQUENCY format
    private Date selectedDate;  // For DATE format
    
    // Constructors, getters, and setters...
    
    public static TaskNewDTO fromTask(Task task) {
        TaskNewDTO dto = new TaskNewDTO();
        dto.setId(task.getId().toHexString());
        dto.setTitle(task.getTitle());
        dto.setNotes(task.getNotes());
        dto.setStartDate(task.getStartDate());
        dto.setCloseDate(task.getCloseDate());
        dto.setTimeOfTheDay(task.getTimeOfTheDay());
        
        if (task.getSection() != null) {
            dto.setSectionId(task.getSection().toHexString());
        }
        if (task.getOwnerId() != null) {
            dto.setOwnerId(task.getOwnerId().toHexString());
        }
        
        // Set format-specific fields
        if (task instanceof TaskOnce) {
            dto.setFormat(TaskFormat.ONCE);
        } else if (task instanceof TaskDaily) {
            dto.setFormat(TaskFormat.EVERY_DAY);
        } else if (task instanceof TaskCustom) {
            dto.setFormat(TaskFormat.FREQUENCY);
            TaskCustom custom = (TaskCustom) task;
            dto.setPeriod(custom.getPeriod());
            dto.setFrequencyType(custom.getFrequencyTypes());
        } else if (task instanceof TaskDate) {
            dto.setFormat(TaskFormat.DATE);
            TaskDate dateTask = (TaskDate) task;
            dto.setSelectedDate(dateTask.getSelectedDate());
        }
        
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

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public LocalTime getTimeOfTheDay() {
		return timeOfTheDay;
	}

	public void setTimeOfTheDay(LocalTime timeOfTheDay) {
		this.timeOfTheDay = timeOfTheDay;
	}

	public TaskFormat getFormat() {
		return format;
	}

	public void setFormat(TaskFormat format) {
		this.format = format;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public FrequencyTypes getFrequencyType() {
		return frequencyType;
	}

	public void setFrequencyType(FrequencyTypes frequencyType) {
		this.frequencyType = frequencyType;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	
    
    
    
    // Getters and setters for all fields...
}