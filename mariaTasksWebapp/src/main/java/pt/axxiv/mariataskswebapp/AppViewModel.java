package pt.axxiv.mariataskswebapp;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Window;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.SectionType;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.User;
import pt.axxiv.mariataskswebapp.controllers.fragment.SideBarController;
import pt.axxiv.mariataskswebapp.controllers.fragment.TaskListController;
import pt.axxiv.mariataskswebapp.controllers.fragment.TodayTaskListController;

import java.util.*;

public class AppViewModel {
    private Section selectedSection;
    private List<Section> userSections;
    private User currentUser;
    private Window mainWindow;
    private Task editingTask;
    private Map<Section, List<Task>> tasksMap = new HashMap<>();

    public Window getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(Window mainWindow) {
        this.mainWindow = mainWindow;
    }

    public Task getEditingTask() {
        return editingTask;
    }

    public void setEditingTask(Task editingTask) {
        this.editingTask = editingTask;
    }

    public Section getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(Section selectedSection) {
        this.selectedSection = selectedSection;
    }

    public List<Section> getUserSections() {
        return userSections;
    }

    public void setUserSections(List<Section> userSections) {
        this.userSections = userSections;
    }

    public void updateUserSections() {
        this.userSections = new SectionDAO().findAllByUser(this.currentUser.getId());

        if(this.userSections == null || this.userSections.size()<=0) {
            Section section = new Section("To Do", "z-icon-pencil-square-o", currentUser.getId(), SectionType.TASKS);
            section = new SectionDAO().insert(section);
            this.userSections.add(section);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    public static AppViewModel fromSession() {
        return (AppViewModel) org.zkoss.zk.ui.Sessions.getCurrent().getAttribute("appVM");
    }
    public static void storeInSession(AppViewModel vm) {
        org.zkoss.zk.ui.Sessions.getCurrent().setAttribute("appVM", vm);
    }

    public void updateWindowTitle() {
        this.mainWindow.getPage().setTitle("MariaTasks - "+this.selectedSection.getTitle());
    }

    public List<Task> getTasksForSelectedSection() {
        return tasksMap.get(this.selectedSection);
    }

    public Map<Section, List<Task>> getTasksMap() {
        return tasksMap;
    }

    public void setTasksMap(Map<Section, List<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

    public void addTaskToMap(Task t) {
        List<Task> ts = tasksMap.get(this.userSections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
        ts.add(t);
        tasksMap.put(this.userSections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
    }
    public void addSectionToMap(Section section) {
        tasksMap.put(section, new ArrayList<Task>());
    }

    public void removeTaskFromMap(Task t) {
        List<Task> ts = tasksMap.get(this.userSections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
        ts.remove(t);
        tasksMap.put(this.userSections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
    }

    public void updateTaskMap() {
        this.tasksMap = new HashMap<>();
        for(Section s : this.userSections) {
            this.tasksMap.put(s, new ArrayList<>());
        }

        Set<Task> allTasks = new HashSet<>();
        allTasks.addAll(new TaskDAO().findAllOpenByUser(this.currentUser.getId()));
        allTasks.addAll(new TaskDAO().findAllClosedTodayByUser(this.currentUser.getId()));

        List<Task> sortedTasks = new ArrayList<>(allTasks);

        sortedTasks.sort(
                Comparator
                        .comparing((Task t) -> t.getCloseDate() != null)
                        .thenComparing(Task::getCloseDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Task::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getTitle, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId)
        );

        for(Task t : sortedTasks) {
            addTaskToMap(t);
        }

        EventQueue<Event> eq = EventQueues.lookup(TaskListController.UPDATE_LIST, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));
        EventQueue<Event> eqToday = EventQueues.lookup(TodayTaskListController.UPDATE_LIST, EventQueues.DESKTOP, true);
        eqToday.publish(new Event("onRefresh", null, null));
    }
}
