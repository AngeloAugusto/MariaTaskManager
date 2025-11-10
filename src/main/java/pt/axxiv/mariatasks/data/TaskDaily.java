package pt.axxiv.mariatasks.data;

import java.time.LocalTime;

import org.bson.types.ObjectId;

public class TaskDaily extends Task{
	
	public TaskDaily() {
		super();
	}
	
	public TaskDaily(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
	}
	
}
