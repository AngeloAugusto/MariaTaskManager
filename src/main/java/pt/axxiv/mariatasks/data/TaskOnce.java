package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

public class TaskOnce extends Task{

	public TaskOnce() {
		super();
	}
	
	public TaskOnce(String title, String notes, ObjectId section, ObjectId user) {
		super(title, notes, section, user);
	}
	
	
}
