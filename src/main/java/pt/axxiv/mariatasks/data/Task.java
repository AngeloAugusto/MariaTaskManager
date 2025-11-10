package pt.axxiv.mariatasks.data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

public class Task implements Comparable<Task> {

	private ObjectId id;
	private String title;
	private String notes;
	private Date startDate;
	private Date closeDate;
	private ObjectId parent;
	private ObjectId section;
	private LocalTime timeOfTheDay;
	private ObjectId ownerId;

	public Task(String title, String notes, ObjectId section, ObjectId user, LocalTime localtime) {
		this.title = title;
		this.notes = notes;
		this.startDate = Calendar.getInstance().getTime();
		this.section = section;
		this.ownerId = user;
		this.timeOfTheDay = localtime;
	}

	public Task() {
		this.startDate = Calendar.getInstance().getTime();
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
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

	public void setClosed() {
		this.closeDate = Calendar.getInstance().getTime();
	}

	public ObjectId getParent() {
		return parent;
	}

	public void setParent(ObjectId parent) {
		this.parent = parent;
	}

	public ObjectId getSection() {
		return section;
	}

	public void setSection(ObjectId section) {
		this.section = section;
	}

	public LocalTime getTimeOfTheDay() {
		return timeOfTheDay;
	}

	public void setTimeOfTheDay(LocalTime timeOfTheDay) {
		this.timeOfTheDay = timeOfTheDay;
	}
	
	public ObjectId getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(ObjectId ownerId) {
		this.ownerId = ownerId;
	}

	public String timeFormated() {
		if (timeOfTheDay == null) {
	        return "";
	    }
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	    return timeOfTheDay.format(formatter);
	}

	@Override
	public int compareTo(Task arg0) {
		return getStartDate().compareTo(arg0.getStartDate());
	}
}
