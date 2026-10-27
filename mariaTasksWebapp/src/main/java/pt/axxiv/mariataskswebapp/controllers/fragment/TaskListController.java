package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.renderer.TaskListRenderer;

public class TaskListController extends SelectorComposer<Vlayout> {

    @Wire Label lbSectionTitle;
    @Wire Button btEditTitleSection;
    @Wire Button btDeleteSection;
    @Wire Button btCreateTask;
    @Wire Vlayout taskList;

    private Vlayout taskListContainer;
    private AppViewModel viewModel;

    public static final String UPDATE_LIST = "updateList";
    public static final String UPDATE_BTCREATE_CLASS = "updateBtCreateClass";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        taskListContainer = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        EventQueue<Event> eq = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));

        lbSectionTitle.setValue(viewModel.getSelectedSection().getTitle());
        TaskListRenderer.generateTaskList(taskList, viewModel, new TaskDAO().findAllToShowOnSection(viewModel.getCurrentUser().getId(), viewModel.getSelectedSection()), false);

        EventQueue<Event> eqUpdate = EventQueues.lookup(UPDATE_LIST, EventQueues.DESKTOP, true);
        eqUpdate.subscribe(event -> {
            lbSectionTitle.setValue(viewModel.getSelectedSection().getTitle());
            TaskListRenderer.generateTaskList(taskList, viewModel, new TaskDAO().findAllToShowOnSection(viewModel.getCurrentUser().getId(), viewModel.getSelectedSection()), false);
        });
        EventQueue<Event> eqBtCreate = EventQueues.lookup(UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.subscribe(event -> {
            updateBtCreateClass((String) event.getData());
        });
    }

    private void updateBtCreateClass(String data) {
        btCreateTask.setSclass(data);
    }

    @Listen("onClick = #btEditTitleSection")
    public void onClickbtEditTitleSection(Event e) {
        EventQueue<Event> eq = EventQueues.lookup(SideBarController.EDIT_SECTION, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, btEditTitleSection));
    }


    @Listen("onClick = #btDeleteSection")
    public void onClickbtDeleteSection(Event e) {
        EventQueue<Event> eq = EventQueues.lookup(SideBarController.DELETE_SECTION, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, btDeleteSection));
    }

    @Listen("onClick = #btCreateTask")
    public void onClickbtCreateTask(Event e) {
        EventQueue<Event> eq = EventQueues.lookup(FormTaskController.CLEAR_VALUES, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));
        EventQueue<Event> eqToggleView = EventQueues.lookup(FormContainerController.TOGGLE_VIEW, EventQueues.DESKTOP, true);
        eqToggleView.publish(new Event("onRefresh", null, null));
    }
}
