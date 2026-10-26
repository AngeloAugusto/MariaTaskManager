package pt.axxiv.mariataskswebapp;

import org.zkoss.zul.Window;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.data.*;

import java.util.*;

public class AppViewModel {
    private Section selectedSection;
    private List<Section> userSections;
    private User currentUser;
    private Window mainWindow;
    private Task editingTask;
    private Birthday editingBirthday;

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

    public Birthday getEditingBirthday() {
        return editingBirthday;
    }

    public void setEditingBirthday(Birthday editingBirthday) {
        this.editingBirthday = editingBirthday;
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

}
