package pt.axxiv.mariataskswebapp.helpers;

import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.connection.labels.TaskFields;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskWeekday;

public class TaskHelper {
    public static void toggleTaskCloseDate(Task t) {
        if(t.getCloseDate() != null) {
            t.setCloseDate(null);
            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, null);
            Task hasNewRollingTask = new TaskDAO().findByParent(t.getId());
            if(hasNewRollingTask != null) {
                new TaskDAO().delete(hasNewRollingTask.getId());
            }
        }else {
            t.setClosed();
            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());
            Task task = TaskFactory.createRollingTask(t);
            if(task != null) {
                new TaskDAO().insert(task);
            }
        }
    }

    public static void toggleTaskCloseDateWithDone(Task t, boolean isDone) {
        if(t.getCloseDate() != null) {
            t.setCloseDate(null);
            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, null);

            if(t instanceof TaskWeekday) {
                ((TaskWeekday) t).setDone(isDone);
                new TaskDAO().updateValue(t.getId(), TaskFields.DONE, isDone);
            }

            Task hasNewRollingTask = new TaskDAO().findByParent(t.getId());
            if(hasNewRollingTask != null) {
                new TaskDAO().delete(hasNewRollingTask.getId());
            }
        }else {
            t.setClosed();
            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());

            if(t instanceof TaskWeekday) {
                ((TaskWeekday) t).setDone(isDone);
                new TaskDAO().updateValue(t.getId(), TaskFields.DONE, isDone);
            }

            Task task = TaskFactory.createRollingTask(t);
            if(task != null) {
                new TaskDAO().insert(task);
            }
        }
    }
}
