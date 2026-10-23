package pt.axxiv.mariataskswebapp.renderer;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.*;
import pt.axxiv.mariatasks.connection.dao.SubTaskDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.data.SubTask;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskWeekday;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.controllers.fragment.FormTaskController;
import pt.axxiv.mariataskswebapp.helpers.SubTaskHelper;
import pt.axxiv.mariataskswebapp.helpers.TaskHelper;
import org.zkoss.zhtml.Hr;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class TaskListRenderer {

    public static void generateTaskList(Vlayout taskSection, AppViewModel viewModel, List<Task> tasks, boolean isToday) {
        while (taskSection.getFirstChild() != null)
            taskSection.removeChild(taskSection.getFirstChild());

        if(tasks == null || tasks.isEmpty())
            return;

        //tasks.stream().filter(t -> t.getSection().equals(selectedSection.getId())).collect(Collectors.toSet())
        LocalDate today = LocalDate.now();

        boolean hasClosed = tasks.getFirst().getCloseDate()!=null;
        boolean hasAfterToday = tasks.getFirst().getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(today);
        for (Task t : tasks) {
            if(t.getCloseDate()!= null && !hasClosed) {
                hasClosed=true;
                Hr hr = new Hr();
                hr.setStyle("width: 90%; margin-top: 15px; margin-bottom: 15px;");
                taskSection.appendChild(hr);
            }
            if(t.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(today) && !hasAfterToday) {
                hasAfterToday=true;
                Hr hr = new Hr();
                hr.setStyle("width: 90%; margin-top: 15px; margin-bottom: 15px;");
                taskSection.appendChild(hr);
            }

            Div row = new Div();
            row.setSclass("line");

            // Container for title + notes
            Div textContainer = new Div();
            textContainer.setStyle("display: flex; flex-direction: column; flex: 1; min-width: 0;");
            textContainer.setTooltiptext(t.getStartDate().toString());

            // Title
            String title = t.getTitle();
            if(t.getTimeOfTheDay() != null) {
                title+=" - "+t.timeFormated();
            }
            if(t instanceof TaskDate taskDate) {
                title += " ("+taskDate.setSelectedDateString()+")";
            }
            if(t instanceof TaskWeekday taskWeekday) {
                title += " - ("+taskWeekday.getWeekday()+")";
            }
            if(t.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(today)) {
                title += " | "+t.startDateFormatted();
            }
            Label titleLabel = new Label(title);
            titleLabel.setSclass("taskTitle");
            textContainer.appendChild(titleLabel);

            // Notes
            if (t.getNotes() != null && !t.getNotes().isEmpty()) {
                Div container = new Div();
                container.setSclass("notes-container");
                textContainer.appendChild(container);

                Html notes = new Html();
                notes.setContent(t.getNotes());
                container.appendChild(notes);
            }

            List<SubTask> subTasks = new SubTaskDAO().findByParent(t.getId());
            if(!subTasks.isEmpty()){
                Div subtaskList = new Div();
                subtaskList.setSclass("subtask-grid");
                subtaskList.setStyle("display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 10px; margin-left: 10px; margin-right: 10px; width: calc(100% - 20px); margin-top: 10px;");

                for(SubTask subTask : subTasks){
                    Div rowSt = new Div();
                    rowSt.setSclass("line");
                    rowSt.setStyle("box-sizing: border-box; display: flex;");

                    // Container for title
                    Div textContainerSt = new Div();
                    textContainerSt.setStyle("display: flex; flex-direction: column; flex: 1; min-width: 0;");

                    // Title
                    String titleSt = subTask.getTitle();
                    Label titleLabelSt = new Label(titleSt);
                    titleLabelSt.setSclass("subTaskTitle");
                    textContainerSt.appendChild(titleLabelSt);
                    rowSt.appendChild(textContainerSt);

                    Div separator = new Div();
                    separator.setStyle("width: 1px; align-self: stretch; background: #777; margin-left: auto; margin-right: 10px; flex-shrink: 0;");
                    rowSt.appendChild(separator);

                    Button btSubDone = new Button(" ");
                    btSubDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px; margin-right: 10px; color: white; flex-shrink: 0;");
                    if (subTask.getCloseDate() != null) {
                        btSubDone.setIconSclass("z-icon-check");
                    }
                    btSubDone.setDisabled(t.getCloseDate() != null);
                    btSubDone.addEventListener("onClick", e -> onClickDoneSubTask(subTask, viewModel));
                    rowSt.appendChild(btSubDone);


                    subtaskList.appendChild(rowSt);
                }
                textContainer.appendChild(subtaskList);
            }

            if (t.getCloseDate()!= null) {
                Label closedAt = new Label("Task closed at "+t.closedDateFormatted());
                closedAt.setStyle("font-size: 12px; color: gray; margin-top: 2px;");
                textContainer.appendChild(closedAt);
            }

            if(t.getCloseDate()== null) {
                row.addEventListener("onClick", e ->onClickOnTaskRow(t, viewModel));
            }

            row.appendChild(textContainer);

            // Done button
            if(!t.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(today)) {

                Div separator = new Div();
                separator.setStyle("width: 1px; align-self: stretch; background: #777; margin-left: auto; margin-right: 20px; flex-shrink: 0;");
                row.appendChild(separator);

                if(t instanceof TaskWeekday && t.getCloseDate()==null) {
                    Div div = new Div();
                    Button btYes = new Button(" ");
                    btYes.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px; color: white;");
                    btYes.addEventListener("onClick", e -> onClickYes(t, viewModel));
                    btYes.setIconSclass("z-icon-check");

                    Button btNo = new Button(" ");
                    btNo.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px; color: white;");
                    btNo.addEventListener("onClick", e -> onClickNo(t, viewModel));
                    btNo.setIconSclass("z-icon-times");

                    div.appendChild(btNo);
                    div.appendChild(btYes);
                    row.appendChild(div);
                }else {
                    Button btDone = new Button(" ");
                    btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px; color: white;");
                    if(t.getCloseDate() != null) {
                        btDone.setIconSclass("z-icon-check");
                    }
                    btDone.addEventListener("onClick", e -> onClickDone(t, viewModel));
                    row.appendChild(btDone);
                }
            }

            taskSection.appendChild(row);
        }
    }

    public static void onClickOnTaskRow(Task t, AppViewModel viewModel) {
        if(viewModel.getEditingTask() == null || viewModel.getEditingTask() != t) {
            viewModel.setEditingTask(t);

            EventQueue<Event> eq = EventQueues.lookup(FormTaskController.OPEN_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));

        } else if(t == viewModel.getEditingTask()) {
            EventQueue<Event> eq = EventQueues.lookup(FormTaskController.CLOSE_FORM, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
            viewModel.setEditingTask(null);
            return;
        }else {
            System.out.println("NOW WHAT???");
            return;
        }
    }

    private static void onClickYes(Task t, AppViewModel viewModel) {

        if(viewModel.getEditingTask() != null) {
            //closeNewTaskWindow(); //TODO
            viewModel.setEditingTask(null);
        }

//        if(inHistoric)
//            closeHistoric(); //TODO

        TaskHelper.toggleTaskCloseDateWithDone(t, true);

        viewModel.updateTaskMap();

    }

    private static void onClickNo(Task t, AppViewModel viewModel) {

        if(viewModel.getEditingTask() != null) {
            //closeNewTaskWindow(); //TODO
            viewModel.setEditingTask(null);
        }

//        if(inHistoric)
//            closeHistoric(); //TODO

        TaskHelper.toggleTaskCloseDateWithDone(t, false);

        viewModel.updateTaskMap();
    }

    private static void onClickDone(Task t, AppViewModel viewModel) {
        if(viewModel.getEditingTask() != null) {
            //closeNewTaskWindow(); //TODO
            viewModel.setEditingTask(null);
        }

//        if(inHistoric)
//            closeHistoric(); //TODO

        TaskHelper.toggleTaskCloseDate(t);

        viewModel.updateTaskMap();
    }


    private static void onClickDoneSubTask(SubTask t, AppViewModel viewModel) {
        if(viewModel.getEditingTask() != null) {
            //closeNewTaskWindow(); //TODO
            viewModel.setEditingTask(null);
        }

//        if(inHistoric)
//            closeHistoric(); //TODO

        SubTaskHelper.toggleSubTaskCloseDate(t);

        viewModel.updateTaskMap();
    }
}
