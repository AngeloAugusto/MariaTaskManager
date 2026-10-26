package pt.axxiv.mariataskswebapp.controllers;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.auth.AuthUtil;
import pt.axxiv.mariataskswebapp.controllers.fragment.MainContainerController;
import pt.axxiv.mariataskswebapp.controllers.fragment.SideBarController;

public class MainController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	@Wire Vlayout center;

    private Window window;

	private AppViewModel viewModel;


	public static final String UPDATE_CENTER_CLASS = "updateCenterClass";
	
	@Override
    public void doBeforeComposeChildren(Window comp) throws Exception {
        super.doBeforeComposeChildren(comp);
		window=comp;

        // Check if logged in
        checkIsLogedIn();

		viewModel = new AppViewModel();
		viewModel.setCurrentUser(AuthUtil.getCurrentUser());
		viewModel.setMainWindow(window);
		AppViewModel.storeInSession(viewModel);

		EventQueue<Event> eq = EventQueues.lookup(UPDATE_CENTER_CLASS, EventQueues.DESKTOP, true);
		eq.subscribe(event -> {
			updateCenterClass((String) event.getData());
		});
    }

	private void updateCenterClass(String data) {
		center.setSclass(data);
	}

	private void checkIsLogedIn() {
		if (!AuthUtil.isLoggedIn()) {
            // Redirect to login page
            Executions.sendRedirect("/login.zul");
            return;
        }
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		window=comp;

		if(AuthUtil.getCurrentUser() == null) {
			return;
		}


	}

	@Listen("onTabFocus = #tasksTabWindow")
	public void onTabFocus() {
		checkIsLogedIn();
		viewModel.updateUserSections();

		EventQueue<Event> eq = EventQueues.lookup(SideBarController.UPDATE_SECTION, EventQueues.DESKTOP, true);
		eq.publish(new Event("onRefresh", null, null));

		EventQueue<Event> eqRedraw = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
		eqRedraw.publish(new Event("onRefresh", null, null));
	}

}

