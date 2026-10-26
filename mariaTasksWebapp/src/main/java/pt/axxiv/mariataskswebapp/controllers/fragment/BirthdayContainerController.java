package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariatasks.connection.dao.BirthdayDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.data.Birthday;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.renderer.BirthdayListRenderer;
import pt.axxiv.mariataskswebapp.renderer.TaskListRenderer;

import java.util.List;

public class BirthdayContainerController extends SelectorComposer<Vlayout> {

    @Wire Label lbSectionTitle;
    @Wire Button btEditTitleSection;
    @Wire Button btDeleteSection;
    @Wire Button btCreateBirthDay;
    @Wire Div birthdayList;

    private Vlayout birthdayListContainer;
    private AppViewModel viewModel;

    public static final String UPDATE_LIST = "updateList";
    public static final String UPDATE_BTCREATE_CLASS = "updateBtCreateClass";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        birthdayListContainer = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        lbSectionTitle.setValue(viewModel.getSelectedSection().getTitle());
        BirthdayListRenderer.generateList(birthdayList, viewModel, new BirthdayDAO().findAllBySection(viewModel.getSelectedSection().getId()));

        EventQueue<Event> eq = EventQueues.lookup(UPDATE_LIST, EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            lbSectionTitle.setValue(viewModel.getSelectedSection().getTitle());
            BirthdayListRenderer.generateList(birthdayList, viewModel, new BirthdayDAO().findAllBySection(viewModel.getSelectedSection().getId()));
        });
        EventQueue<Event> eqBtCreate = EventQueues.lookup(UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.subscribe(event -> {
            updateBtCreateBirthDayClass((String) event.getData());
        });
    }

    private void updateBtCreateBirthDayClass(String data) {
        btCreateBirthDay.setSclass(data);
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

    @Listen("onClick = #btCreateBirthDay")
    public void onClickbtCreateBirthDay(Event e) {
        EventQueue<Event> eq = EventQueues.lookup(FormBirthdayController.CLEAR_VALUES, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));
        EventQueue<Event> eqToggleView = EventQueues.lookup(FormContainerController.TOGGLE_VIEW, EventQueues.DESKTOP, true);
        eqToggleView.publish(new Event("onRefresh", null, null));
    }
}
