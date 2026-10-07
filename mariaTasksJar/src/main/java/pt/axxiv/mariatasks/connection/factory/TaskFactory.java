package pt.axxiv.mariatasks.connection.factory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.bson.types.ObjectId;

import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDaily;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.TaskOnce;
import pt.axxiv.mariatasks.data.TaskWeekday;

public class TaskFactory {
	
	public static Task createTask(TaskFormat format, String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
        return switch (format) {
            case ONCE -> new TaskOnce(title, notes, section, user, localTime);
            case EVERY_DAY -> new TaskDaily(title, notes, section, user, localTime);
            case FREQUENCY -> new TaskCustom(title, notes, section, user, localTime);
            case DATE -> new TaskDate(title, notes, section, user, localTime);
            case WEEKDAY -> new TaskWeekday(title, notes, section, user, localTime);
        };
    }
	
	public static Task createRollingTask(Task oldTask) {
		Task t = null;
		if(oldTask instanceof TaskDaily oldTaskDaily) {
			t = new TaskDaily(oldTaskDaily.getTitle(), oldTaskDaily.getNotes(), oldTaskDaily.getSection(), oldTask.getOwnerId(), oldTask.getTimeOfTheDay());
			t.setParent(oldTaskDaily.getId());
			
			Calendar c = Calendar.getInstance();
			c.setTime(oldTaskDaily.getCloseDate());
			c.add(Calendar.DATE, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			t.setStartDate(c.getTime());
			
		}else if(oldTask instanceof TaskCustom oldTaskCustom) {
			
			t = new TaskCustom(oldTaskCustom.getTitle(), oldTaskCustom.getNotes(), oldTaskCustom.getPeriod(), oldTaskCustom.getFrequencyTypes(), oldTaskCustom.getSection(), oldTask.getOwnerId(), oldTask.getTimeOfTheDay());
			t.setParent(oldTaskCustom.getId());
			
			Calendar c = Calendar.getInstance();
			c.setTime(oldTaskCustom.getCloseDate());
			
			if(oldTaskCustom.getFrequencyTypes().equals(FrequencyTypes.BY_DAY)) {
				c.add(Calendar.DATE, oldTaskCustom.getPeriod());
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
			} else if(oldTaskCustom.getFrequencyTypes().equals(FrequencyTypes.BY_MONTH)) {
				c.add(Calendar.MONTH, oldTaskCustom.getPeriod());
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
			} else if(oldTaskCustom.getFrequencyTypes().equals(FrequencyTypes.BY_YEAR)) {
				c.add(Calendar.YEAR, oldTaskCustom.getPeriod());
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
			}
			
			t.setStartDate(c.getTime());
			
		}else if(oldTask instanceof TaskWeekday oldTaskWeekday) {
			t = new TaskWeekday(oldTaskWeekday.getTitle(), oldTaskWeekday.getNotes(), 
                    oldTaskWeekday.getSection(), oldTask.getOwnerId(), 
                    oldTask.getTimeOfTheDay());
			((TaskWeekday)t).setRolling(oldTaskWeekday.isRolling());
			((TaskWeekday)t).setSelectedWeekdays(oldTaskWeekday.getSelectedWeekdays());
			t.setParent(oldTaskWeekday.getId());
			
			Calendar c = Calendar.getInstance();
			c.setTime(oldTaskWeekday.getCloseDate());
			
			// Get the current day of week (Calendar uses 1=Sunday, 2=Monday, ..., 7=Saturday)
			int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			// Find the next selected weekday, skipping today
			int daysToAdd = oldTaskWeekday.findNextSelectedWeekday(currentDayOfWeek, true);

			if(daysToAdd<0){
				return null;
			}
			
			c.add(Calendar.DATE, daysToAdd);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			LocalDate date = c.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			DayOfWeek dow = date.getDayOfWeek();
			((TaskWeekday)t).setWeekday(dow.getDisplayName(TextStyle.SHORT, Locale.US));
			
			t.setStartDate(c.getTime());
		}
//		else if(oldTask instanceof TaskDate oldTaskDate) {
//			
//			Calendar c = Calendar.getInstance();
//			c.setTime(oldTaskDate.getSelectedDate());
//			c.add(Calendar.YEAR, 1);
//			c.set(Calendar.HOUR_OF_DAY, 0);
//			c.set(Calendar.MINUTE, 0);
//			c.set(Calendar.SECOND, 0);
//			c.set(Calendar.MILLISECOND, 0);
//			
//			t = new TaskDate(oldTask.getTitle(), oldTask.getNotes(), c.getTime(), oldTask.getSection(), oldTask.getOwnerId(), oldTask.getTimeOfTheDay());
//		}
		
		return t;
    }
}
