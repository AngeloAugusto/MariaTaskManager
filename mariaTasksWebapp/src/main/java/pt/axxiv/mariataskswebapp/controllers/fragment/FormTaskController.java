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
import pt.axxiv.mariatasks.connection.dao.SubTaskDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.data.*;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.controllers.MainController;
import pt.axxiv.mariataskswebapp.util.NoteUtil;
import pt.axxiv.mariataskswebapp.util.TimeUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Calendar;

public class FormTaskController extends SelectorComposer<Vlayout> {

    @Wire Textbox txTitle;
    @Wire Textbox txNotes;
    @Wire Timebox tbTime;
    @Wire Button btClearTime;
    @Wire Combobox cbFormat;
    @Wire Vlayout layoutForFrequency;
    @Wire Intbox ibPeriod;
    @Wire Combobox cbFrequency;
    @Wire Vlayout layoutForDate;
    @Wire Datebox dbSelectedDate;
    @Wire Vlayout layoutForWeekday;
    @Wire Button btMon;
    @Wire Button btTue;
    @Wire Button btWed;
    @Wire Button btThu;
    @Wire Button btFri;
    @Wire Button btSat;
    @Wire Button btSun;
    @Wire Checkbox chRolling;
    @Wire Datebox dbStartDate;
    @Wire Combobox cbSection;
    @Wire Vlayout layoutSubTask;
    @Wire Textbox txSubTaskTitle;
    @Wire Button btAddSubTask;
    @Wire Vlayout subtaskList;
    @Wire Button btDeleteTask;
    @Wire Button btSaveNewTask;
    @Wire Vlayout taskHistory;
    @Wire Vlayout taskHistoryList;
    @Wire Popup ppConfirmDeletionTask;
    @Wire Button btTaskNo;
    @Wire Button btTaskYes;
    @Wire Popup ppConfirmDeletionSubTask;
    @Wire Button btSubTaskNo;
    @Wire Button btSubTaskYes;

    private Vlayout formTask;
    private AppViewModel viewModel;

    private List<Integer> weekdays = new ArrayList<>();
    private List<SubTask> subTasks = new ArrayList<>();
    private ListModelList<TaskFormat> taskFormatListModelList ;
    private SubTask editingSubTask = null;

    private ListModelList<FrequencyTypes> frequencyFormatListModelList;

    public static final String CLEAR_VALUES = "clearValues";
    public static final String CLOSE_FORM = "closeForm";
    public static final String OPEN_FORM = "openForm";

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        formTask = comp;

        viewModel = AppViewModel.fromSession();
        if (viewModel == null || viewModel.getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        taskFormatListModelList = new ListModelList<TaskFormat>(TaskFormat.values());
        cbFormat.setModel(taskFormatListModelList);

        frequencyFormatListModelList = new ListModelList<FrequencyTypes>(FrequencyTypes.values());
        frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
        cbFrequency.setModel(frequencyFormatListModelList);

        taskFormatListModelList.addToSelection(TaskFormat.ONCE);

        EventQueue<Event> eq = EventQueues.lookup(CLEAR_VALUES, EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            clearNewTaskWindow();
        });
        EventQueue<Event> eqClose = EventQueues.lookup(CLOSE_FORM, EventQueues.DESKTOP, true);
        eqClose.subscribe(event -> {
            closeNewTaskWindow();
        });
        EventQueue<Event> eqOpen = EventQueues.lookup(OPEN_FORM, EventQueues.DESKTOP, true);
        eqOpen.subscribe(event -> {
            openNewTaskWindow();
        });

    }


    @Listen("onOK = #txTitle")
    public void onEnterPress(Event event) {
        onClickbtSaveNewTask(null);
    }

    @Listen("onClick = #btSaveNewTask")
    public void onClickbtSaveNewTask(Event e) {
        String title = txTitle.getValue();
        String notes = NoteUtil.convertToHtml(txNotes.getValue());

        TaskFormat selectedFormat = (TaskFormat) cbFormat.getSelectedItem().getValue();

        if(selectedFormat.equals(TaskFormat.WEEKDAY) && (weekdays==null || weekdays.isEmpty() || weekdays.size()<=0)) {
            Clients.showNotification("No weekday was selected.", layoutForWeekday);
            return;
        }

        Task task = TaskFactory.createTask(selectedFormat, title, notes, viewModel.getSelectedSection().getId(), viewModel.getCurrentUser().getId(), tbTime.getValueInLocalTime());

        if(viewModel.getEditingTask() != null) {
            task.setId(viewModel.getEditingTask().getId());
            task.setStartDate(dbStartDate.getValue());
            task.setSection(cbSection.getSelectedItem().getValue());
            task.setParent(viewModel.getEditingTask().getParent());
            viewModel.setEditingTask(null);
        }

        if(task instanceof TaskCustom) {
            ((TaskCustom) task).setPeriod(ibPeriod.getValue());
            ((TaskCustom) task).setFrequencyTypes(cbFrequency.getSelectedItem().getValue());
        } else if(task instanceof TaskDate) {
            ((TaskDate) task).setSelectedDate(dbSelectedDate.getValue());
        }else if(task instanceof TaskWeekday) {
            ((TaskWeekday) task).setSelectedWeekdays(weekdays);
            ((TaskWeekday) task).setRolling(chRolling.isChecked());

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            int daysToAdd = ((TaskWeekday) task).findNextSelectedWeekday(currentDayOfWeek);
            c.add(Calendar.DATE, daysToAdd);
            task.setStartDate(c.getTime());

            LocalDate date = c.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DayOfWeek dow = date.getDayOfWeek();
            ((TaskWeekday)task).setWeekday(dow.getDisplayName(TextStyle.SHORT, Locale.US));

        }


        new TaskDAO().insert(task);

        if(!subTasks.isEmpty()){
            for(SubTask subTask : subTasks){
                subTask.setParentTask(task.getId());
                new SubTaskDAO().insert(subTask);
            }
            subTasks = new ArrayList<>();
        }

        EventQueue<Event> eq = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));

        closeNewTaskWindow();
    }


    private void closeNewTaskWindow() {

        clearNewTaskWindow();

        // close with animation
        EventQueue<Event> eq = EventQueues.lookup(FormContainerController.CONTAINER_SCLASS, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, "add-task-form"));
        EventQueue<Event> eqUpdateCenter = EventQueues.lookup(MainController.UPDATE_CENTER_CLASS, EventQueues.DESKTOP, true);
        eqUpdateCenter.publish(new Event("onRefresh", null, "task-section"));
        EventQueue<Event> eqBtCreate = EventQueues.lookup(TaskListController.UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.publish(new Event("onRefresh", null, "add-btn"));

        btDeleteTask.setVisible(false);
        dbStartDate.setVisible(false);
        taskHistory.setVisible(false);
    }

    private void clearNewTaskWindow() {

        txTitle.setValue("");
        txNotes.setValue("");
        taskFormatListModelList.addToSelection(TaskFormat.ONCE);
        tbTime.setValue(null);
        clearWeekdayFields();
        

        viewModel.setEditingTask(null);

        onSelectDefaultOnTaskFormat();

        clearDateFields();
        clearFrequencyFields();

        
        txSubTaskTitle.setValue("");
        subTasks = new ArrayList<>();
        generateSubTaskList();
        
    }

    private void clearWeekdayFields() {
        weekdays = new ArrayList<>();
        loadWeekdayButtons();
        chRolling.setChecked(true);
    }
    @Listen("onClick = #btMon")
    public void onClickbtMon(Event e) {
        toggleWeekDay(0);
    }

    @Listen("onClick = #btTue")
    public void onClickbtTue(Event e) {
        toggleWeekDay(1);
    }

    @Listen("onClick = #btWed")
    public void onClickbtWed(Event e) {
        toggleWeekDay(2);
    }

    @Listen("onClick = #btThu")
    public void onClickbtThu(Event e) {
        toggleWeekDay(3);
    }

    @Listen("onClick = #btFri")
    public void onClickbtFri(Event e) {
        toggleWeekDay(4);
    }

    @Listen("onClick = #btSat")
    public void onClickbtSat(Event e) {
        toggleWeekDay(5);
    }

    @Listen("onClick = #btSun")
    public void onClickbtSun(Event e) {
        toggleWeekDay(6);
    }

    private void toggleWeekDay(int weekday) {
        if (weekdays.contains(weekday)) {
            weekdays.remove(Integer.valueOf(weekday));
        } else {
            weekdays.add(weekday);
        }
        loadWeekdayButtons();
    }

    private void loadWeekdayButtons() {
        btMon.setSclass("add-btn" + (weekdays.contains(0) ? " selected" : ""));
        btTue.setSclass("add-btn" + (weekdays.contains(1) ? " selected" : ""));
        btWed.setSclass("add-btn" + (weekdays.contains(2) ? " selected" : ""));
        btThu.setSclass("add-btn" + (weekdays.contains(3) ? " selected" : ""));
        btFri.setSclass("add-btn" + (weekdays.contains(4) ? " selected" : ""));
        btSat.setSclass("add-btn" + (weekdays.contains(5) ? " selected" : ""));
        btSun.setSclass("add-btn" + (weekdays.contains(6) ? " selected" : ""));
    }

    @Listen("onSelect = #cbFormat")
    public void onComboChange() {
        TaskFormat selected = cbFormat.getSelectedItem().getValue();

        if(selected == TaskFormat.FREQUENCY) {
            onSelectFrequencyOnTaskFormat();
            clearFrequencyFields();
        }else if(selected == TaskFormat.DATE) {
            onSelectDateOnTaskFormat();
            clearDateFields();
        }else if(selected == TaskFormat.WEEKDAY) {
            onSelectWeekdayOnTaskFormat();
            clearWeekdayFields();
        }else {
            onSelectDefaultOnTaskFormat();
        }
    }

    private void onSelectDefaultOnTaskFormat() {
        layoutForDate.setVisible(false);
        layoutForFrequency.setVisible(false);
        layoutForWeekday.setVisible(false);
    }

    private void onSelectDateOnTaskFormat() {
        layoutForDate.setVisible(true);
        layoutForFrequency.setVisible(false);
        layoutForWeekday.setVisible(false);
    }

    private void onSelectWeekdayOnTaskFormat() {
        layoutForWeekday.setVisible(true);
        layoutForFrequency.setVisible(false);
        layoutForDate.setVisible(false);
    }

    private void clearDateFields() {
        dbSelectedDate.setValue(new Date());
    }

    private void onSelectFrequencyOnTaskFormat() {
        layoutForDate.setVisible(false);
        layoutForFrequency.setVisible(true);
        layoutForWeekday.setVisible(false);
    }

    private void clearFrequencyFields() {
        ibPeriod.setValue(null);
        frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
    }

    @Listen("onClick = #btClearTime")
    public void onClickbtClearTime(Event e) {
        tbTime.setValue(null);
    }

    public void generateSubTaskList(){
        while (subtaskList.getFirstChild() != null)
            subtaskList.removeChild(subtaskList.getFirstChild());

        if(subTasks == null || subTasks.isEmpty())
            return;

        for(SubTask subTask : subTasks){
            Div row = new Div();
            row.setSclass("line");

            // Container for title + notes
            Div textContainer = new Div();
            textContainer.setStyle("display: flex; flex-direction: column;");

            // Title
            String title = subTask.getTitle();
            Label titleLabel = new Label(title);
            titleLabel.setSclass("subTaskTitle");
            textContainer.appendChild(titleLabel);
            row.appendChild(textContainer);

            Button btDelete = new Button(" ");
            btDelete.setStyle(
                    "margin-left: auto; background: #242526; border: 2px solid white; " +
                            "padding: 0; height: 30px; width: 30px; margin-right: 10px; " +
                            "color: white; flex-shrink: 0;"
            );
            btDelete.setIconSclass("z-icon-trash");
            btDelete.addEventListener("onClick", e -> {
                e.stopPropagation();
                onClickDeleteSubTask(btDelete, subTask);
            });
            row.appendChild(btDelete);

            // Row click
            row.addEventListener("onClick", e -> onClickOnSubTaskRow(subTask));

            subtaskList.appendChild(row);
        }
    }


    private void onClickOnSubTaskRow(SubTask subTask) {
        editingSubTask = subTask;
        txSubTaskTitle.setText(subTask.getTitle());
    }

    private void onClickDeleteSubTask(Button button, SubTask subTask) {
        editingSubTask = subTask;
        ppConfirmDeletionSubTask.open(button);
    }

    @Listen("onClick = #btSubTaskNo")
    public void onClickbtSubTaskNo(Event e) {
        ppConfirmDeletionSubTask.close();
    }

    @Listen("onClick = #btSubTaskYes")
    public void onClickbtSubTaskYes(Event e) {

        if(viewModel.getEditingTask() != null) {
            new SubTaskDAO().delete(editingSubTask.getId());
        }

        subTasks.remove(editingSubTask);
        editingSubTask = null;
        generateSubTaskList();

        ppConfirmDeletionSubTask.close();
    }

    @Listen("onOK = #txSubTaskTitle")
    public void onEnterPresstxSubTaskTitle(Event event) {
        onClickbtAddSubTask(null);
    }

    @Listen("onClick = #btAddSubTask")
    public void onClickbtAddSubTask(Event e) {

        if(txSubTaskTitle.getText().isEmpty()){
            Clients.showNotification("No sub task title was inserted.", "warning", txSubTaskTitle, "before_start", 3000);
            return;
        }

        SubTask subTask = editingSubTask != null ? editingSubTask : new SubTask();
        subTask.setTitle(txSubTaskTitle.getValue());

        if(editingSubTask == null){
            subTasks.addFirst(subTask);
        }
        editingSubTask = null;

        txSubTaskTitle.setValue("");

        generateSubTaskList();
    }

    @Listen("onClick = #btDeleteTask")
    public void onClickbtDeleteTask(Event e) {
        ppConfirmDeletionTask.open(btDeleteTask);
    }


    @Listen("onClick = #btTaskYes")
    public void onClickbtTaskYes(Event e) {

        new TaskDAO().delete(viewModel.getEditingTask().getId());
        viewModel.setEditingTask(null);
        EventQueue<Event> eq = EventQueues.lookup(MainContainerController.REDRAW_CONTAINER, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, null));

        ppConfirmDeletionTask.close();
        closeNewTaskWindow();

    }

    @Listen("onClick = #btTaskNo")
    public void onClickbtTaskNo(Event e) {
        ppConfirmDeletionTask.close();
    }


    private void openNewTaskWindow() {
        EventQueue<Event> eq = EventQueues.lookup(FormContainerController.CONTAINER_SCLASS, EventQueues.DESKTOP, true);
        eq.publish(new Event("onRefresh", null, "add-task-form open"));
        EventQueue<Event> eqUpdateCenter = EventQueues.lookup(MainController.UPDATE_CENTER_CLASS, EventQueues.DESKTOP, true);
        eqUpdateCenter.publish(new Event("onRefresh", null, "task-section retracted"));
        EventQueue<Event> eqBtCreate = EventQueues.lookup(TaskListController.UPDATE_BTCREATE_CLASS, EventQueues.DESKTOP, true);
        eqBtCreate.publish(new Event("onRefresh", null, "add-btn rotate"));
        txTitle.setFocus(true);

        subTasks = new ArrayList<>();

        if(viewModel.getEditingTask() != null) {
            cbSection.setVisible(true);
            List<Section> tempSec = viewModel.getUserSections();
            Section taskSection = tempSec.stream().filter(e -> e.getId().equals(viewModel.getEditingTask().getSection())).toList().getFirst();
            ListModelList<Section> sectionModelList = new ListModelList<Section>(tempSec);
            cbSection.setModel(sectionModelList);
            sectionModelList.addToSelection(taskSection);
            cbSection.setItemRenderer((item, data, index) -> {
                Section section = (Section) data;
                item.setLabel(section.getTitle());
                item.setValue(section.getId());
            });
            btDeleteTask.setVisible(true);
            dbStartDate.setVisible(true);
            taskHistory.setVisible(generateTaskHistory());

            txTitle.setValue(viewModel.getEditingTask().getTitle());
            txNotes.setValue(NoteUtil.convertToText(viewModel.getEditingTask().getNotes()));

            dbStartDate.setValue(viewModel.getEditingTask().getStartDate());

            subTasks = new SubTaskDAO().findByParent(viewModel.getEditingTask().getId());
            generateSubTaskList();

            if(viewModel.getEditingTask().getTimeOfTheDay()!=null)
                tbTime.setValue(TimeUtil.localTimeToDate(viewModel.getEditingTask().getTimeOfTheDay()));

            if(viewModel.getEditingTask() instanceof TaskOnce) {
                taskFormatListModelList.addToSelection(TaskFormat.ONCE);

                onSelectDefaultOnTaskFormat();

            }else if(viewModel.getEditingTask() instanceof TaskDaily) {
                taskFormatListModelList.addToSelection(TaskFormat.EVERY_DAY);

                onSelectDefaultOnTaskFormat();

            }else if(viewModel.getEditingTask() instanceof TaskCustom tCostum) {
                taskFormatListModelList.addToSelection(TaskFormat.FREQUENCY);
                ibPeriod.setValue(tCostum.getPeriod());
                frequencyFormatListModelList.addToSelection(tCostum.getFrequencyTypes());

                onSelectFrequencyOnTaskFormat();

            } else if(viewModel.getEditingTask() instanceof TaskDate tDate) {
                taskFormatListModelList.addToSelection(TaskFormat.DATE);
                dbSelectedDate.setValue(tDate.getSelectedDate());

                onSelectDateOnTaskFormat();
            } else if(viewModel.getEditingTask() instanceof TaskWeekday tWeekday) {
                taskFormatListModelList.addToSelection(TaskFormat.WEEKDAY);

                weekdays = tWeekday.getSelectedWeekdays();
                chRolling.setChecked(tWeekday.isRolling());
                loadWeekdayButtons();

                onSelectWeekdayOnTaskFormat();
            } else {
                System.out.println("WHAT FORMAT???");
            }

        }else {
            cbSection.setVisible(false);
        }
    }

    private boolean generateTaskHistory() {

        List<Task> taskList = new ArrayList<>();
        Task parent = new TaskDAO().findById(viewModel.getEditingTask().getParent());
        int count = 0;
        boolean hasMore = false;

        while (parent != null && count<15) {
            taskList.add(parent);
            parent = new TaskDAO().findById(parent.getParent());
            count++;
        }

        if (parent != null) {
            hasMore = true;
        }

        if(taskList.size()<=0) {
            return false;
        }

        while (taskHistoryList.getFirstChild() != null)
            taskHistoryList.removeChild(taskHistoryList.getFirstChild());

        for (Task t : taskList) {
            Div row = new Div();
            row.setSclass("line");

            Div textContainer = new Div();
            textContainer.setStyle("display: flex; flex-direction: column;");
            String closeDate = t.closedDateFormatted();
            if (t instanceof TaskWeekday) {
                if(((TaskWeekday) t).isDone()) {
                    closeDate+=" (Done)";
                }else {
                    closeDate+=" (Not Done)";
                }
            }
            Label titleLabel = new Label(closeDate);
            titleLabel.setSclass("taskTitle");
            textContainer.appendChild(titleLabel);
            row.appendChild(textContainer);
            taskHistoryList.appendChild(row);
        }

        if (hasMore) {
            Div row = new Div();
            row.setSclass("line");

            Div textContainer = new Div();
            textContainer.setStyle("display: flex; flex-direction: column;");

            Label moreLabel = new Label("and more...");
            moreLabel.setSclass("taskTitle");
            textContainer.appendChild(moreLabel);
            row.appendChild(textContainer);
            taskHistoryList.appendChild(row);
        }



        return true;
    }
}
