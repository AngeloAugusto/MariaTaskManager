package pt.axxiv.mariatasks.data;

import java.time.LocalTime;

import org.bson.types.ObjectId;

public class TaskOnce extends Task{

	public TaskOnce() {
		super();
	}
	
	public TaskOnce(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
	}
	
	
}
