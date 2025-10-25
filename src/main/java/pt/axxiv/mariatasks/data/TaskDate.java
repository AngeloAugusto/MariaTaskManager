package pt.axxiv.mariatasks.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;

public class TaskDate extends Task{
	
	private Date selectedDate;

	public TaskDate() {
		super();
	}
	
	public TaskDate(String title, String notes, ObjectId section) {
		super(title, notes, section);
	}
	
	public TaskDate(String title, String notes, Date seleDate, ObjectId section) {
		super(title, notes, section);
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
