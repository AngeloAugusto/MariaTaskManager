package pt.axxiv.mariatasks.data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;

public class TaskWeekday extends Task{

	private List<Integer> selectedWeekdays;
	private String weekday;
	private boolean rolling;
	private boolean done;

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
			currentDayInYourFormat = currentDayOfWeek - 2; // Monday=1 -> 0, Tuesday=2 -> 1, etc.
		}

		int startOffset = skipToday ? 1 : 0;

		// First, try to find a selected weekday within the current week
		for (int i = startOffset; i <= 6 - currentDayInYourFormat; i++) {
			int dayToCheck = currentDayInYourFormat + i;
			if (selectedWeekdays.contains(dayToCheck)) {
				return i;
			}
		}

		// No selected weekday left in the current week
		if (rolling) {
			// Jump to next week and find the first selected weekday
			int daysUntilNextMonday = 7 - currentDayInYourFormat;
			for (int i = 0; i < 7; i++) {
				int dayToCheck = i; // 0=Monday ... 6=Sunday of next week
				if (selectedWeekdays.contains(dayToCheck)) {
					return daysUntilNextMonday + i;
				}
			}
			// Should not happen if selectedWeekdays is not empty
			return daysUntilNextMonday + 7;
		} else {
			// Not rolling: do NOT search next week — fall back to next week anyway as last resort?
			// If you truly want to restrict to this week only, return -1 here.
			// Keeping original fallback (7 days) to avoid breaking callers:
			return -1;
		}
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isRolling() {
		return rolling;
	}

	public void setRolling(boolean rolling) {
		this.rolling = rolling;
	}
}
