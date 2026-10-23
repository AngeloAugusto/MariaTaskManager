package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariataskswebapp.AppViewModel;

public class FormContainerController extends SelectorComposer<Vlayout>  {
    @Wire
    Include icFromTask;

    private Vlayout formContainer;
    private AppViewModel viewModel;


    public static final String CONTAINER_SCLASS = "containerSclass";
    public static final String TOGGLE_VIEW = "toggleView";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        formContainer = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        EventQueue<Event> eq = EventQueues.lookup(CONTAINER_SCLASS, EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            updateFormContainerClass((String) event.getData());
        });
        EventQueue<Event> eqToggleView = EventQueues.lookup(TOGGLE_VIEW, EventQueues.DESKTOP, true);
        eqToggleView.subscribe(event -> {
            toggleNewTaskWindow();
        });
        //TODO: IF SELECTION TYPE TASK icFromTask.visible
        //TOOD: ELSE...
    }

    private void updateFormContainerClass(String data) {
        formContainer.setSclass(data);
    }
    private void toggleNewTaskWindow() {
        String sclass = formContainer.getSclass();
        if (sclass.contains("open")) {
            EventQueue<Event> eq = EventQueues.lookup(FormTaskController.CLOSE_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        } else {
            EventQueue<Event> eq = EventQueues.lookup(FormTaskController.OPEN_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        }
    }
}
