package pt.axxiv.mariatasks.data;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

import org.bson.types.ObjectId;

public class TaskDate extends Task{
	
	private Date selectedDate;

	public TaskDate() {
		super();
	}
	
	public TaskDate(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user,localTime);
	}
	
	public TaskDate(String title, String notes, Date seleDate, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
		this.selectedDate = seleDate;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	public String setSelectedDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(this.getSelectedDate()).toString();
	}
	
	
	
	
}
