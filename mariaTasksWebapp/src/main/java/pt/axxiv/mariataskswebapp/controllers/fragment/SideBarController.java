package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.bson.types.ObjectId;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.SectionType;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.User;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.auth.AuthUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SideBarController extends SelectorComposer<Div> {

    @Wire private Button btToggleMenu;
    @Wire private Vlayout menuItems;
    @Wire private Button btAddSection;
    @Wire private Button btProfile;
    @Wire private Popup ppAddSection;
    @Wire private Popup ppConfirmDeletion;
    @Wire private Button btIcon;
    @Wire private Textbox txSection;
    @Wire private Popup ppIcon;
    @Wire private Div iconContainer;
    @Wire private Combobox cbType;

    private Div sidebar;
    private AppViewModel viewModel;

    private boolean showSectionTitle = false;
    private boolean editSectionTitle = false;

    private ListModelList<SectionType> sectionTypeListModelList;

    List<String> icons = Arrays.asList(
            "home", "search", "user", "cog", "cogs", "wrench", "trash-o",
            "edit", "pencil", "pencil-square-o", "save", "print",
            "bell", "bell-o", "envelope", "envelope-o", "calendar", "calendar-o",
            "clock-o", "heart", "star", "star-o", "bookmark", "bookmark-o",
            "tag", "tags", "book", "file", "files-o", "folder", "folder-open",
            "folder-o", "folder-open-o",
            "check", "times", "times-circle", "times-circle-o", "close",
            "remove", "warning", "info", "question", "ban",
            "play", "pause", "stop", "video-camera", "camera", "camera-retro", "image",
            "shopping-cart", "shopping-bag", "shopping-basket", "credit-card",
            "money", "dollar", "euro", "gift", "barcode",
            "facebook-square", "twitter-square", "linkedin-square", "github-square",
            "google-plus-square", "instagram", "youtube-play",
            "briefcase", "building", "desktop", "file-text", "users",
            "heartbeat", "medkit", "bicycle", "apple", "paw",
            "graduation-cap", "calculator", "flask", "globe",
            "plane", "car", "train", "map-marker", "suitcase",
            "cutlery", "tv", "paint-brush", "trash",
            "bank", "pie-chart",
            "lightbulb-o", "magic", "rocket", "flag", "bullseye",
            "film", "music", "gamepad", "headphones", "ticket",
            "cloud", "database", "coffee", "bed", "leaf", "futbol-o",
            "trophy", "fire", "phone", "comments"
    );

    public static final String UPDATE_SECTION = "updateSections";
    public static final String EDIT_SECTION = "editSections";
    public static final String DELETE_SECTION = "deleteSections";

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        sidebar=comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        viewModel.updateUserSections();
        viewModel.setSelectedSection(viewModel.getUserSections().getFirst());

        generateSectionList();

        createIconGrid();

        sectionTypeListModelList = new ListModelList<>(SectionType.values());
        cbType.setModel(sectionTypeListModelList);
        cbType.setItemRenderer((item, data, index) -> {
            SectionType st = (SectionType) data;
            item.setLabel(st.getLabel());
            item.setValue(st);
        });
        sectionTypeListModelList.addToSelection(SectionType.TASKS);

        EventQueue<Event> eq = EventQueues.lookup(UPDATE_SECTION, EventQueues.DESKTOP, true);
        eq.subscribe(event -> generateSectionList());
        EventQueue<Event> eqEdit = EventQueues.lookup(EDIT_SECTION, EventQueues.DESKTOP, true);
        eqEdit.subscribe(event -> editSelectedSection((Button) event.getData()));
        EventQueue<Event> eqDelete = EventQueues.lookup(DELETE_SECTION, EventQueues.DESKTOP, true);
        eqDelete.subscribe(event -> deleteSelectedSection((Button) event.getData()));
    }

    @Listen("onOK = #txSection")
    public void onEnterPresstxSection(Event event) {
        onClickbtSaveNewSection(null);
    }

    @Listen("onClick = #btIcon")
    public void onClickBtIcon(Event e) {
        ppIcon.open(btIcon, "before_end");
    }

    @Listen("onClick = #btSaveNewSection")
    public void onClickbtSaveNewSection(Event e) {
        String title = txSection.getValue().trim();
        SectionType type = (SectionType) cbType.getSelectedItem().getValue();

        Section sectionToChec = new SectionDAO().findByTitle(title, viewModel.getCurrentUser().getId());
        if(sectionToChec!=null && !sectionToChec.getId().equals(viewModel.getSelectedSection().getId())) {
            Clients.showNotification("Section already exists.", txSection);
            txSection.setFocus(true);
            return;
        }

        if(title==null || title.isBlank() || title.isEmpty()) {
            Clients.showNotification("Title cannot be empty", txSection);
            txSection.setFocus(true);
            return;
        }

        Section section = viewModel.getSelectedSection();
        if(editSectionTitle) {

            viewModel.getSelectedSection().setTitle(txSection.getValue());
            viewModel.getSelectedSection().setIcon(btIcon.getIconSclass());
            viewModel.getSelectedSection().setType(type);
            new SectionDAO().update(viewModel.getSelectedSection());

            int index = viewModel.getUserSections().indexOf(section);
            viewModel.getUserSections().remove(index);
            viewModel.getUserSections().add(index, viewModel.getSelectedSection());

            viewModel.updateWindowTitle();

            EventQueue<Event> eq = EventQueues.lookup(TaskListController.UPDATE_LIST, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));

            editSectionTitle=false;
        }else {

            section = new Section(title, btIcon.getIconSclass(), viewModel.getCurrentUser().getId(), type);
            section = new SectionDAO().insert(section);

            viewModel.getUserSections().add(section);
            viewModel.addSectionToMap(section);
        }

        txSection.setValue("");
        ppAddSection.close();

        generateSectionList();
    }

    @Listen("onClick = #btAddSection")
    public void onClickbtAddSection(Event e) {
        editSectionTitle=false;
        txSection.setValue("");
        txSection.setFocus(true);
        btIcon.setIconSclass("z-icon-image");
        sectionTypeListModelList.addToSelection(SectionType.TASKS);

        ppAddSection.open(btAddSection, "before_start");
    }

    public void generateSectionList() {

        while (menuItems.getFirstChild() != null)
            menuItems.removeChild(menuItems.getFirstChild());

        for(Section section : viewModel.getUserSections()) {
            Button bt = new Button();
            bt.setSclass("menu-item collapsed" + (viewModel.getSelectedSection().equals(section) ? " selected" : ""));
            bt.setIconSclass(section.getIcon());
            if(showSectionTitle) {
                bt.setSclass("menu-item open" + (viewModel.getSelectedSection().equals(section) ? " selected" : ""));
                bt.setLabel(section.getTitle());
            }

            bt.addEventListener("onClick", e ->{
                viewModel.setSelectedSection(section);
                viewModel.updateWindowTitle();

                generateSectionList();
                EventQueue<Event> eq = EventQueues.lookup(TaskListController.UPDATE_LIST, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            });
            menuItems.appendChild(bt);
        }

        if(showSectionTitle) {
            btProfile.setIconSclass("z-icon-user");
            btProfile.setSclass("profile-btn open");
            btProfile.setLabel(viewModel.getCurrentUser().getTitle());
        }else {
            btProfile.setIconSclass("z-icon-user");
            btProfile.setSclass("profile-btn");
            btProfile.setLabel(null);
        }

    }

    @Listen("onClick = #btToggleMenu")
    public void onClickbtToggleMenu(Event e) {
        toggleSectionMenu();
        generateSectionList();
    }

    private void toggleSectionMenu() {
        boolean collapsed = sidebar.getSclass().contains("collapsed");
        if (collapsed) {
            expandSectionMenu();
        } else {
            collapseSectionMenu();
        }
    }

    private void collapseSectionMenu() {
        // Collapse sidebar
        sidebar.setSclass("sidebar collapsed");
        showSectionTitle = false;
    }

    private void expandSectionMenu() {
        // Expand sidebar
        sidebar.setSclass("sidebar open");
        showSectionTitle = true;
    }


    private void createIconGrid() {

        for (String icon : icons) {
            Button btn = new Button();
            btn.setSclass("add-btn");
            btn.setIconSclass("z-icon-" + icon);

            btn.addEventListener(Events.ON_CLICK, e -> {
                btIcon.setIconSclass("z-icon-" + icon);
                ppIcon.close();
            });

            iconContainer.appendChild(btn);
        }
    }

    @Listen("onClick = #btLogout")
    public void onClickbtLogout(Event e) {
        AuthUtil.logout();
        Executions.sendRedirect("/login.zul");
    }

    public void editSelectedSection(Button editButton){
        ppAddSection.open(editButton, "after_start");
        editSectionTitle=true;
        txSection.setValue(viewModel.getSelectedSection().getTitle());
        btIcon.setIconSclass(viewModel.getSelectedSection().getIcon());

        SectionType currentType = viewModel.getSelectedSection().getType() != null ? viewModel.getSelectedSection().getType() : SectionType.TASKS;
        sectionTypeListModelList.addToSelection(currentType);
    }

    public void deleteSelectedSection(Button deleteButton){

        if(viewModel.getUserSections().size()<=1) {
            Clients.showNotification("Section can't be deleted.");
            return;
        }

        ppConfirmDeletion.open(deleteButton);
    }



    @Listen("onClick = #btYes")
    public void onClickbtYes(Event e) {

        new TaskDAO().deleteFromSection(viewModel.getSelectedSection().getId());

        new SectionDAO().delete(viewModel.getSelectedSection().getId());

        viewModel.getUserSections().remove(viewModel.getSelectedSection());
        viewModel.setSelectedSection(viewModel.getUserSections().getFirst());
        viewModel.updateWindowTitle();

        viewModel.updateTaskMap();

        ppConfirmDeletion.close();

        generateSectionList();

    }

    @Listen("onClick = #btNo")
    public void onClickbtNo(Event e) {
        ppConfirmDeletion.close();
    }

}
