package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Vlayout;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariataskswebapp.AppViewModel;

public class MainContainerController extends SelectorComposer<Vlayout> {

    @Wire Include icTaskList;

    private Vlayout mainContainer;
    private AppViewModel viewModel;

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        mainContainer = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        //TODO: IF SELECTION TYPE TASK icTaskList.visible
        //TOOD: ELSE...
    }
}
