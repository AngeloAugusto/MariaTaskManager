package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariatasks.data.SectionType;
import pt.axxiv.mariataskswebapp.AppViewModel;

public class FormContainerController extends SelectorComposer<Vlayout>  {
    @Wire Include icFromTask;
    @Wire Include icFromBirthday;

    private Vlayout formContainer;
    private AppViewModel viewModel;


    public static final String CONTAINER_SCLASS = "containerSclass";
    public static final String TOGGLE_VIEW = "toggleView";
    public static final String CLOSE_VIEW = "closeView";
    public static final String OPEN_VIEW = "openView";

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
        EventQueue<Event> eqCloseView = EventQueues.lookup(CLOSE_VIEW, EventQueues.DESKTOP, true);
        eqCloseView.subscribe(event -> {
            closeWindow();
        });
        EventQueue<Event> eqOpenView = EventQueues.lookup(OPEN_VIEW, EventQueues.DESKTOP, true);
        eqOpenView.subscribe(event -> {
            openWindow();
        });
    }

    private void closeWindow() {
        String sclass = formContainer.getSclass();
        if (sclass.contains("open")) {
            if(viewModel.getSelectedSection().getType().equals(SectionType.BIRTHDAYS)){
                icFromBirthday.setVisible(true);
                icFromTask.setVisible(false);
                EventQueue<Event> eq = EventQueues.lookup(FormBirthdayController.CLOSE_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            } else {

                // Default Tasks
                icFromBirthday.setVisible(false);
                icFromTask.setVisible(true);
                EventQueue<Event> eq = EventQueues.lookup(FormTaskController.CLOSE_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            }
        }
    }

    private void openWindow() {
        if(viewModel.getSelectedSection().getType().equals(SectionType.BIRTHDAYS)){
            icFromBirthday.setVisible(true);
            icFromTask.setVisible(false);
            EventQueue<Event> eq = EventQueues.lookup(FormBirthdayController.OPEN_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        } else {

            // Default Tasks
            icFromBirthday.setVisible(false);
            icFromTask.setVisible(true);
            EventQueue<Event> eq = EventQueues.lookup(FormTaskController.OPEN_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
        }
    }

    private void updateFormContainerClass(String data) {
        formContainer.setSclass(data);
    }

    private void toggleNewTaskWindow() {
        String sclass = formContainer.getSclass();
        if (sclass.contains("open")) {
            if(viewModel.getSelectedSection().getType().equals(SectionType.BIRTHDAYS)){
                icFromBirthday.setVisible(true);
                icFromTask.setVisible(false);
                EventQueue<Event> eq = EventQueues.lookup(FormBirthdayController.CLOSE_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            } else {

                // Default Tasks
                icFromBirthday.setVisible(false);
                icFromTask.setVisible(true);
                EventQueue<Event> eq = EventQueues.lookup(FormTaskController.CLOSE_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            }
        } else {
            if(viewModel.getSelectedSection().getType().equals(SectionType.BIRTHDAYS)){
                icFromBirthday.setVisible(true);
                icFromTask.setVisible(false);
                EventQueue<Event> eq = EventQueues.lookup(FormBirthdayController.OPEN_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            } else {

                // Default Tasks
                icFromBirthday.setVisible(false);
                icFromTask.setVisible(true);
                EventQueue<Event> eq = EventQueues.lookup(FormTaskController.OPEN_FORM, EventQueues.DESKTOP, true);
                eq.publish(new Event("onRefresh", null, null));
            }
        }
    }
}
