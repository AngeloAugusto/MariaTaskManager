package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskWeekday;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.helpers.TaskHelper;
import pt.axxiv.mariataskswebapp.renderer.TaskListRenderer;

import java.util.List;

public class TodayTaskListController  extends SelectorComposer<Vlayout> {
    @Wire private Button btCollapseToday;
    @Wire private Vlayout todayTaskList;

    private Vlayout todayTaskSection;

    private AppViewModel viewModel;

    public static final String UPDATE_LIST = "updateList";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        todayTaskSection = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        TaskListRenderer.generateTaskList(todayTaskList, viewModel, new TaskDAO().findAllOpenTodayByUser(viewModel.getCurrentUser().getId()), true);

        EventQueue<Event> eq = EventQueues.lookup(UPDATE_LIST, EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            TaskListRenderer.generateTaskList(todayTaskList, viewModel, new TaskDAO().findAllOpenTodayByUser(viewModel.getCurrentUser().getId()), true);
        });
    }

    @Listen("onClick = #btCollapseToday")
    public void onClickbtCollapseToday() {
        todayTaskList.setVisible(!todayTaskList.isVisible());
        boolean isDown = btCollapseToday.getIconSclass().contains("down");
        if (isDown) {
            btCollapseToday.setIconSclass("z-icon-chevron-up");
        } else {
            btCollapseToday.setIconSclass("z-icon-chevron-down");
        }
    }
}
