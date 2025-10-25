package pt.axxiv.mariatasks.connection.factory;

import java.util.Calendar;

import org.bson.types.ObjectId;

import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDaily;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.TaskOnce;

public class TaskFactory {
	
	public static Task createTask(TaskFormat format, String title, String notes, ObjectId section) {
        return switch (format) {
            case ONCE -> new TaskOnce(title, notes, section);
            case EVERY_DAY -> new TaskDaily(title, notes, section);
            case FREQUENCY -> new TaskCustom(title, notes, section);
            case DATE -> new TaskDate(title, notes, section);
        };
    }
	
	public static Task createRollingTask(Task oldTask) {
		Task t = null;
		if(oldTask instanceof TaskDaily oldTaskDaily) {
			t = new TaskDaily(oldTaskDaily.getTitle(), oldTaskDaily.getNotes(), oldTaskDaily.getSection());
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
			
			t = new TaskCustom(oldTaskCustom.getTitle(), oldTaskCustom.getNotes(), oldTaskCustom.getPeriod(), oldTaskCustom.getFrequencyTypes(), oldTaskCustom.getSection());
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
			
		}else if(oldTask instanceof TaskDate oldTaskDate) {
			
			Calendar c = Calendar.getInstance();
			c.setTime(oldTaskDate.getSelectedDate());
			c.add(Calendar.YEAR, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			t = new TaskDate(oldTask.getTitle(), oldTask.getNotes(), c.getTime(), oldTask.getSection());
		}
		
		return t;
    }
}
