package pt.axxiv.mariataskswebapp.controllers.fragment;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;
import pt.axxiv.mariatasks.connection.dao.BirthdayDAO;
import pt.axxiv.mariatasks.connection.dao.SubTaskDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.data.*;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.controllers.MainController;
import pt.axxiv.mariataskswebapp.util.NoteUtil;
import pt.axxiv.mariataskswebapp.util.TimeUtil;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Calendar;

public class FormBirthdayController extends SelectorComposer<Vlayout> {

    @Wire Textbox txName;
    @Wire Datebox dbBirthday;
    @Wire Button btDeleteBirthday;
    @Wire Button btSaveNewBirthday;
    @Wire Popup ppConfirmDeletionBirthday;
    @Wire Popup btBirthdayNo;
    @Wire Popup btBirthdayYes;

    private Vlayout formBirthday;
    private AppViewModel viewModel;

    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");

    public static final String CLEAR_VALUES = "clearValues";
    public static final String CLOSE_FORM = "closeForm";
    public static final String OPEN_FORM = "openForm";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        formBirthday = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        EventQueue<Event> eq = EventQueues.lookup(CLEAR_VALUES, EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            clearNewBirthdayWindow();
        });
        EventQueue<Event> eqClose = EventQueues.lookup(CLOSE_FORM, EventQueues.DESKTOP, true);
        eqClose.subscribe(event -> {
            closeNewBirthdayWindow();
        });
        EventQueue<Event> eqOpen = EventQueues.lookup(OPEN_FORM, EventQueues.DESKTOP, true);
        eqOpen.subscribe(event -> {
            openNewBirthdayWindow();
        });

    }


    @Listen("onOK = #txName; onOK = #dbBirthday")
    public void onEnterPress(Event event) {
        onClickbtSaveNewBirthday(null);
    }

    @Listen("onClick = #btSaveNewBirthday")
    public void onClickbtSaveNewBirthday(Event e) {
        String name = txName.getValue();

        Date picked = dbBirthday.getValue();
        if (picked == null) {
            Clients.showNotification("No date was selected.", dbBirthday);
            return;
        }

        MonthDay monthDay = toMonthDay(picked);

        Birthday birthday = new Birthday(name, monthDay, viewModel.getSelectedSection().getId(), viewModel.getCurrentUser().getId());

        if(viewModel.getEditingBirthday() != null) {
            birthday.setId(viewModel.getEditingBirthday().getId());
            viewModel.setEditingBirthday(null);
        }

        new BirthdayDAO().insert(birthday);

        EventQueue<Event> eq = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));

        closeNewBirthdayWindow();
    }

    public static MonthDay toMonthDay(Date date) {
        if (date == null) return null;
        LocalDate ld = date.toInstant().atZone(LISBON).toLocalDate();
        return MonthDay.of(ld.getMonthValue(), ld.getDayOfMonth());
    }

    private void closeNewBirthdayWindow() {

        clearNewBirthdayWindow();

        // close with animation
        EventQueue<Event> eq = EventQueues.lookup(FormContainerController.CONTAINER_SCLASS, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, "add-task-form"));
        EventQueue<Event> eqUpdateCenter = EventQueues.lookup(MainController.UPDATE_CENTER_CLASS, EventQueues.DESKTOP, true);
        eqUpdateCenter.publish(new Event("onRefresh", null, "task-section"));
        EventQueue<Event> eqBtCreate = EventQueues.lookup(BirthdayContainerController.UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.publish(new Event("onRefresh", null, "add-btn"));

        btDeleteBirthday.setVisible(false);
    }

    private void clearNewBirthdayWindow() {
        txName.setValue("");
        dbBirthday.setValue(new Date());
    }

    @Listen("onClick = #btDeleteBirthday")
    public void onClickbtDeleteBirthday(Event e) {
        ppConfirmDeletionBirthday.open(btDeleteBirthday);
    }
    
    @Listen("onClick = #btBirthdayYes")
    public void onClickbtBirthdayYes(Event e) {
        new BirthdayDAO().delete(viewModel.getEditingBirthday().getId());
        viewModel.setEditingBirthday(null);
        EventQueue<Event> eq = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));

        ppConfirmDeletionBirthday.close();
        closeNewBirthdayWindow();
    }

    @Listen("onClick = #btBirthdayNo")
    public void onClickbtBirthdayNo(Event e) {
        ppConfirmDeletionBirthday.close();
    }


    private void openNewBirthdayWindow() {
        EventQueue<Event> eq = EventQueues.lookup(FormContainerController.CONTAINER_SCLASS, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, "add-task-form open"));
        EventQueue<Event> eqUpdateCenter = EventQueues.lookup(MainController.UPDATE_CENTER_CLASS, EventQueues.DESKTOP, true);
        eqUpdateCenter.publish(new Event("onRefresh", null, "task-section retracted"));
        EventQueue<Event> eqBtCreate = EventQueues.lookup(BirthdayContainerController.UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.publish(new Event("onRefresh", null, "add-btn rotate"));
        txName.setFocus(true);

        if(viewModel.getEditingBirthday() != null) {

            btDeleteBirthday.setVisible(true);

            txName.setValue(viewModel.getEditingBirthday().getName());
            MonthDay md = viewModel.getEditingBirthday().getMonthDay();
            if (md != null) {
                LocalDate localDate = md.atYear(Year.now().getValue());
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                dbBirthday.setValue(date);
            }
        }
    }
}
