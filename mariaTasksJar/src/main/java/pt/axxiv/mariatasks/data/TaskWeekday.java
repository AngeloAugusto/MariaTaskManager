package pt.axxiv.mariatasks.data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;

public class TaskWeekday extends Task{

	private List<Integer> selectedWeekdays;
	private String weekday;

	public TaskWeekday() {
		super();
	}
	
	public TaskWeekday(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
		this.selectedWeekdays = new ArrayList<Integer>();
	}

	public List<Integer> getSelectedWeekdays() {
		return selectedWeekdays;
	}

	public void setSelectedWeekdays(List<Integer> selectedWeekdays) {
		this.selectedWeekdays = selectedWeekdays;
	}

	public String getWeekday() {
		return weekday;
	}

	public void setWeekday(String weekday) {
		this.weekday = weekday;
	}
	
	
	public int findNextSelectedWeekday(int currentDayOfWeek) {
	    return findNextSelectedWeekday(currentDayOfWeek, false);
	}

	public int findNextSelectedWeekday(int currentDayOfWeek, boolean skipToday) {
	    // Convert Calendar day of week (1-7, 1=Sunday) to your format (0=Monday to 6=Sunday)
	    int currentDayInYourFormat;
	    if (currentDayOfWeek == Calendar.SUNDAY) {
	        currentDayInYourFormat = 6; // Sunday is 6 in your format
	    } else {
	        currentDayInYourFormat = currentDayOfWeek - 2; // Monday=1 becomes 0, Tuesday=2 becomes 1, etc.
	    }
	    
	    int startOffset = skipToday ? 1 : 0;
	    
	    // Check for selected weekdays from today onwards
	    for (int i = startOffset; i <= 7; i++) {
	        int dayToCheck = (currentDayInYourFormat + i) % 7;
	        if (selectedWeekdays.contains(dayToCheck)) {
	            return i;
	        }
	    }
	    
	    // Fallback: add 7 days if no weekday found (shouldn't happen if selectedWeekdays is not empty)
	    return 7;
	}
	
}
