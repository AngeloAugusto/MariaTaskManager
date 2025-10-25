package pt.axxiv.mariatasks.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModelMap;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.connection.labels.TaskFields;
import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.TaskOnce;

public class MainController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;
	@Wire
	private Vlayout taskList;
	@Wire
	private Div sidebar;
	@Wire
	private Button btCreatTask;
	@Wire
	private Vlayout taskSection;
	@Wire
	private Label lbSectionTitle;
	@Wire
	private Vlayout menuItems;
	@Wire
	private Vlayout addTaskForm;
	@Wire
	private Textbox txTitle;
	@Wire
	private Textbox txNotes;
	@Wire
	private Combobox cbFormat;
	@Wire
    private Vlayout layoutForFrequency;
	@Wire
    private Vlayout layoutForDate;
	@Wire
	private Intbox ibPeriod;
	@Wire
	private Combobox cbFrequency;
	@Wire
	private Datebox dbSelectedDate;
	@Wire
	private Timebox tbTime;
	@Wire
	private Popup ppAddSection;
	@Wire
	private Textbox txSection;
	@Wire
    private Timer myTimer;

	private List<Section> sections = new ArrayList<Section>();
	private Map<Section, List<Task>> tasksMap = new HashMap<Section, List<Task>>();
	private ListModelList<FrequencyTypes> frequencyFormatListModelList;

	private boolean showSectionTitle = false;
	private Section selectedSection;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);

		sections = new SectionDAO().findAll();
		
		if(sections.size()<=0) {
			Section section = new Section("To Do", "");
			section = new SectionDAO().insert(section);
			sections.add(section);
		}
		
		selectedSection = sections.get(0);
		
		for(Section s : sections) {
			tasksMap.put(s, new ArrayList<Task>());
		}
		
		for(Task t : new TaskDAO().findAllOpen()) {
			addTaskToMap(t);
		}
		
		ListModelList<TaskFormat> taskFormatListModelList = new ListModelList<TaskFormat>(TaskFormat.values());
		cbFormat.setModel(taskFormatListModelList);
		
		frequencyFormatListModelList = new ListModelList<FrequencyTypes>(FrequencyTypes.values());
		frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
		cbFrequency.setModel(frequencyFormatListModelList);
		
		taskFormatListModelList.addToSelection(TaskFormat.ONCE);

		generateSectionList();
		generateTaskList();

	}

	private void generateTaskList() {
	    while (taskList.getFirstChild() != null)
	        taskList.removeChild(taskList.getFirstChild());

	    //tasks.stream().filter(t -> t.getSection().equals(selectedSection.getId())).collect(Collectors.toSet())
	    for (Task t : tasksMap.get(selectedSection)) {
	        Div row = new Div();
	        row.setSclass("line");

	        // Container for title + notes
	        Div textContainer = new Div();
	        textContainer.setStyle("display: flex; flex-direction: column;");
	        textContainer.setTooltiptext(t.getStartDate().toString());

	        // Title
	        String title = t.getTitle();
	        if(t.getTimeOfTheDay() != null) {
	        	title+=" - "+t.timeFormated();
	        }
	        if(t instanceof TaskDate taskDate) {
	        	title += " ("+taskDate.setSelectedDateString()+")";
	        }
	        Label titleLabel = new Label(title);
	        titleLabel.setSclass("taskTitle");
	        textContainer.appendChild(titleLabel);

	        // Notes
	        if (t.getNotes() != null && !t.getNotes().isEmpty()) {
	            Label notes = new Label(t.getNotes());
	            notes.setStyle("font-size: 12px; color: gray; margin-top: 2px;");
	            textContainer.appendChild(notes);
	        }

	        row.appendChild(textContainer);

	        // Done button
	        Button btDone = new Button(" ");
	        btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px;");
	        btDone.addEventListener("onClick", e -> {
	            List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
				ts.remove(t);
				tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
	            t.setClosed();
	            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());
	            
	            if(t instanceof TaskOnce) {
	            	generateTaskList();
		            return;
	            }

            	Task task = TaskFactory.createRollingTask(t);
            	new TaskDAO().insert(task);
            	
            	generateTaskList();
	        });
	        row.appendChild(btDone);

	        taskList.appendChild(row);
	    }
	}
	
	private void generateSectionList() {
	    while (menuItems.getFirstChild() != null)
	    	menuItems.removeChild(menuItems.getFirstChild());
	    
		lbSectionTitle.setValue(selectedSection.getTitle());
	
	    for(Section section : sections) {
	    	Button bt = new Button();
	    	bt.setSclass("menu-item collapsed" + (selectedSection.equals(section) ? " selected" : ""));
	    	bt.setIconSclass("z-icon-image");
	    	if(showSectionTitle) {
		    	bt.setSclass("menu-item open" + (selectedSection.equals(section) ? " selected" : ""));
	    		bt.setLabel(section.getTitle());
	    	}
	    	
	    	bt.addEventListener("onClick", e ->{
	    		selectedSection = section;
	            generateSectionList();
	            generateTaskList();
	    	});
		    menuItems.appendChild(bt);
	    }
	    
	}

	@Listen("onClick = #btToggleMenu")
	public void onClickbtToggleMenu(Event e) {
		boolean collapsed = sidebar.getSclass().contains("collapsed");
		if (collapsed) {
	        // Expand sidebar
	        sidebar.setSclass("sidebar open");
	        showSectionTitle = true;
	    } else {
	        // Collapse sidebar
	        sidebar.setSclass("sidebar collapsed");
	        showSectionTitle = false;
	    }
        generateSectionList();
	}

	@Listen("onClick = #teste")
	public void onClickbtDeleteBD(Event e) {
		new TaskDAO().deleteAll();
		new SectionDAO().deleteAll();
		tasksMap = new HashMap<Section, List<Task>>();
		sections = new ArrayList<Section>();
		
		Section section = new Section("To Do", "");
		section = new SectionDAO().insert(section);
		sections.add(section);

		generateSectionList();
		generateTaskList();
		System.out.println("DB CLEARED....................");
	}

	@Listen("onClick = #btCreatTask")
	public void onClickbtCreatTask(Event e) {
	    String sclass = addTaskForm.getSclass();
	    if (sclass.contains("open")) {
	        addTaskForm.setSclass("add-task-form"); // slide out
	        taskSection.setSclass("task-section");
	        btCreatTask.setSclass("add-btn");
	    } else {
	        addTaskForm.setSclass("add-task-form open"); // slide in
	        taskSection.setSclass("task-section retracted");
	        btCreatTask.setSclass("add-btn rotate");
	        txTitle.setFocus(true);
	    }
	}

	@Listen("onClick = #btSaveNewTask")
	public void onClickbtSaveNewTask(Event e) {
		String title = txTitle.getValue();
		String notes = txNotes.getValue();
		TaskFormat selectedFormat = (TaskFormat) cbFormat.getSelectedItem().getValue();
		
		Task task = TaskFactory.createTask(selectedFormat, title, notes, selectedSection.getId());
		
		if(task instanceof TaskCustom ) {
			((TaskCustom) task).setPeriod(ibPeriod.getValue());
			((TaskCustom) task).setFrequencyTypes(cbFrequency.getSelectedItem().getValue());
		} else if(task instanceof TaskDate) {
			((TaskDate) task).setSelectedDate(dbSelectedDate.getValue());
		}
		
		if(tbTime.getValue() != null) {
			task.setTimeOfTheDay(tbTime.getValueInLocalTime());
		}
		
		new TaskDAO().insert(task);
		addTaskToMap(task);
		
	    txTitle.setValue("");
	    txNotes.setValue("");
	    cbFormat.setSelectedIndex(0);
	    tbTime.setValue(null);
	    
    	layoutForDate.setVisible(false);
    	layoutForFrequency.setVisible(false);	
	    
	    // close with animation
	    addTaskForm.setSclass("add-task-form");
	    taskSection.setSclass("task-section");
        btCreatTask.setSclass("add-btn");
		
		generateTaskList();
	}
	
	@Listen("onSelect = #cbFormat")
    public void onComboChange() {
        TaskFormat selected = cbFormat.getSelectedItem().getValue();
        
		if(selected == TaskFormat.FREQUENCY) {
        	layoutForDate.setVisible(false);
        	layoutForFrequency.setVisible(true);
        	
        	ibPeriod.setValue(null);
        	frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
        	
        }else if(selected == TaskFormat.DATE) {
        	layoutForDate.setVisible(true);
        	layoutForFrequency.setVisible(false);

        	dbSelectedDate.setValue(null);
        	
		}else {
        	layoutForDate.setVisible(false);
        	layoutForFrequency.setVisible(false);	
		}
    }

	@Listen("onClick = #btClearTime")
	public void onClickbtClearTime(Event e) {
		tbTime.setValue(null);
	}

	@Listen("onClick = #btSaveNewSection")
	public void onClickbtAddSection(Event e) {
		String title = txSection.getValue();
		
		//TODO: Verificar se título existe ou não
		
		Section section = new Section(title, "");
		section = new SectionDAO().insert(section);
			
		sections.add(section);
		tasksMap.put(section, new ArrayList<Task>());

	    txSection.setValue("");
	    ppAddSection.close();
	    
		generateSectionList();
	}
	
	@Listen("onTimer = #myTimer")
    public void runTask(Event event) {
		List<Task> tasksTemp = new TaskDAO().findAllOpen(selectedSection);
		if(tasksTemp.size()>0) {

			List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(tasksTemp.get(0).getSection())).findFirst().get());
			if(ts.size()!=tasksTemp.size()) {
				tasksMap.put(sections.stream().filter(s -> s.getId().equals(tasksTemp.get(0).getSection())).findFirst().get(), tasksTemp);
				generateTaskList();
			}
		}
    }
	
	private void addTaskToMap(Task t) {
		List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
		ts.add(t);
		tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
	}
}
