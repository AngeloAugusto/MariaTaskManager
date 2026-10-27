package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.data.SectionType;
import pt.axxiv.mariataskswebapp.AppViewModel;

public class MainContainerController extends SelectorComposer<Vlayout> {

    @Wire Include icTaskContainer;
    @Wire Include icBirthdayContainer;

    private Vlayout mainContainer;
    private AppViewModel viewModel;

    public static final String REDRAW_CONTAINER = "redrawContainer";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        mainContainer = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        redrawContainer();

        EventQueue<Event> eqBtCreate = EventQueues.lookup(REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eqBtCreate.subscribe(event -> {
            redrawContainer();
        });
    }

    public void redrawContainer(){

        EventQueue<Event> eqClose = EventQueues.lookup(FormContainerController.CLOSE_VIEW, EventQueues.DESKTOP, true);
        eqClose.publish(new Event("onRefresh", null, null));

        if(viewModel.getSelectedSection().getType().equals(SectionType.BIRTHDAYS)){
            icTaskContainer.setVisible(false);
            icBirthdayContainer.setVisible(true);

            EventQueue<Event> eq = EventQueues.lookup(BirthdayContainerController.UPDATE_LIST, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        } else {
            // Default TASKS
            icTaskContainer.setVisible(true);
            icBirthdayContainer.setVisible(false);

            EventQueue<Event> eq = EventQueues.lookup(TaskListController.UPDATE_LIST, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        }
    }
}
