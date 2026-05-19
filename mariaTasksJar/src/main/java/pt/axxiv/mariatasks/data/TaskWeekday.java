package pt.axxiv.mariatasks.data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

public class TaskWeekday extends Task{
	
	private List<Integer> selectedWeekdays = new ArrayList<Integer>();

	public TaskWeekday() {
		super();
	}
	
	public TaskWeekday(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime, List<Integer> selectedWeekdays) {
		super(title, notes, section, user, localTime);
		this.selectedWeekdays = selectedWeekdays;
	}

	public List<Integer> getSelectedWeekdays() {
		return selectedWeekdays;
	}

	public void setSelectedWeekdays(List<Integer> selectedWeekdays) {
		this.selectedWeekdays = selectedWeekdays;
	}
	
	
}
