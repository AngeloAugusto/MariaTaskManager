package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

public class TaskDaily extends Task{
	
	public TaskDaily() {
		super();
	}
	
	public TaskDaily(String title, String notes, ObjectId section) {
		super(title, notes, section);
	}
	
}
