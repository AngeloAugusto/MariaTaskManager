package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

public class TaskOnce extends Task{

	public TaskOnce() {
		super();
	}
	
	public TaskOnce(String title, String notes, ObjectId section) {
		super(title, notes, section);
	}
	
	
}
