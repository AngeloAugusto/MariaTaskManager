package pt.axxiv.mariatasks.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import pt.axxiv.mariatasks.auth.AuthUtil;
import pt.axxiv.mariatasks.connection.dao.SectionDAO;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.connection.factory.TaskFactory;
import pt.axxiv.mariatasks.connection.labels.SectionFields;
import pt.axxiv.mariatasks.connection.labels.TaskFields;
import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDaily;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskFormat;
import pt.axxiv.mariatasks.data.TaskOnce;
import pt.axxiv.mariatasks.data.User;

public class MainController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;
	@Wire
	private Vlayout taskList;
	@Wire
	private Div sidebar;
	@Wire
	private Button btCreateTask;
	@Wire
	private Vlayout taskSection;
	@Wire
	private Vlayout todayTaskList;
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
	private Popup ppConfirmDeletion;
	@Wire
	private Textbox txSection;
	@Wire
	private Button btProfile;
	@Wire
	private Button btDeleteSection;
	@Wire
    private Timer myTimer;

	private List<Section> sections = new ArrayList<Section>();
	private List<Task> tasksToday = new ArrayList<Task>();
	private Map<Section, List<Task>> tasksMap = new HashMap<Section, List<Task>>();
	private ListModelList<FrequencyTypes> frequencyFormatListModelList;
	private ListModelList<TaskFormat> taskFormatListModelList ;

	private boolean showSectionTitle = false;
	private boolean inHistoric = false;
	private boolean editSectionTitle = false;
	private Section selectedSection;
	
	private Task editingTask = null;

	private User currentUser;
	
	
	@Override
    public void doBeforeComposeChildren(Window comp) throws Exception {
        super.doBeforeComposeChildren(comp);

        // Check if logged in
        checkIsLogedIn();
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
		

		currentUser = new UserDAO().findById((ObjectId) Sessions.getCurrent().getAttribute("currentUserId"));
		
		if(currentUser == null) {
			return;
		}
		
		sections = new SectionDAO().findAllByUser(currentUser.getId());
		
		if(sections.size()<=0) {
			Section section = new Section("To Do", "", currentUser.getId());
			section = new SectionDAO().insert(section);
			sections.add(section);
		}
		
		selectedSection = sections.get(0);
		
		for(Section s : sections) {
			tasksMap.put(s, new ArrayList<Task>());
		}
		
		for(Task t : new TaskDAO().findAllOpenByUser(currentUser.getId())) {
			addTaskToMap(t);
			
		}
		
		tasksToday = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
		
		taskFormatListModelList = new ListModelList<TaskFormat>(TaskFormat.values());
		cbFormat.setModel(taskFormatListModelList);
		
		frequencyFormatListModelList = new ListModelList<FrequencyTypes>(FrequencyTypes.values());
		frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
		cbFrequency.setModel(frequencyFormatListModelList);
		
		taskFormatListModelList.addToSelection(TaskFormat.ONCE);

		generateSectionList();
		generateTaskList(tasksMap.get(selectedSection));
		generateTodayTaskList(tasksToday);

	}
	


	@Listen("onOK = #txTitle")
	public void onEnterPress(Event event) {
		onClickbtSaveNewTask(null);
	}

	private void generateTodayTaskList(List<Task> tasks) {
		while (todayTaskList.getFirstChild() != null)
			todayTaskList.removeChild(todayTaskList.getFirstChild());
	    
	    if(tasks.size()<=0)
	    	return;

	    for (Task t : tasks) {
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
	        
	        if (inHistoric) {
	            Label closedAt = new Label("Task closed at "+t.closedDateFormatted());
	            closedAt.setStyle("font-size: 12px; color: gray; margin-top: 2px;");
	            textContainer.appendChild(closedAt);
	        }
	        
	        row.addEventListener("onClick", e ->{
	        	if(editingTask == null || editingTask != t) {
	        		openNewTaskWindow();
		        	editingTask = t;
	        	} else if(t == editingTask) {
	        		closeNewTaskWindow();
	        		editingTask = null;
	        		return;
	        	}else {
	        		System.out.println("NOW WHAT???");
	        		return;
	        	}
	        	
		        
		        txTitle.setValue(editingTask.getTitle());
		        txNotes.setValue(editingTask.getNotes());
		        
		        if(editingTask.getTimeOfTheDay()!=null)
		        	tbTime.setValue(localTimeToDate(editingTask.getTimeOfTheDay()));
		       
		        if(editingTask instanceof TaskOnce) {
		        	taskFormatListModelList.addToSelection(TaskFormat.ONCE);
		        	
		        	onSelectDefaultOnTaskFormat();
		        	
		        }else if(editingTask instanceof TaskDaily) {
		        	taskFormatListModelList.addToSelection(TaskFormat.EVERY_DAY);
		        	
		        	onSelectDefaultOnTaskFormat();
		        	
		        }else if(editingTask instanceof TaskCustom tCostum) {
		        	taskFormatListModelList.addToSelection(TaskFormat.FREQUENCY);
		        	ibPeriod.setValue(tCostum.getPeriod());
		        	frequencyFormatListModelList.addToSelection(tCostum.getFrequencyTypes());

					onSelectFrequencyOnTaskFormat();
					
		        } else if(editingTask instanceof TaskDate tDate) {
		        	taskFormatListModelList.addToSelection(TaskFormat.DATE);
		        	dbSelectedDate.setValue(tDate.getSelectedDate());
		        	
		        	onSelectDateOnTaskFormat();
		        } 
	        });
	        row.appendChild(textContainer);

	        // Done button
	        Button btDone = new Button(" ");
	        btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px;");
	        btDone.addEventListener("onClick", e -> {
	            List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
				ts.remove(t);
				tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
				tasksToday.remove(t);
	            t.setClosed();
	            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());
	            
	            if(t instanceof TaskOnce || t instanceof TaskDate) {
	            	generateTaskList(tasksMap.get(selectedSection));
	        		generateTodayTaskList(tasksToday);
		            return;
	            }

            	Task task = TaskFactory.createRollingTask(t);
            	new TaskDAO().insert(task);
            	
            	tasksToday = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
            	
            	generateTaskList(tasksMap.get(selectedSection));
        		generateTodayTaskList(tasksToday);
	        });
	        row.appendChild(btDone);
	        

	        todayTaskList.appendChild(row);
	    }
	}

	private void generateTaskList(List<Task> tasks) {
	    while (taskList.getFirstChild() != null)
	        taskList.removeChild(taskList.getFirstChild());
	    
	    if(tasks.size()<=0)
	    	return;

	    //tasks.stream().filter(t -> t.getSection().equals(selectedSection.getId())).collect(Collectors.toSet())
	    for (Task t : tasks) {
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
	        
	        if (inHistoric) {
	            Label closedAt = new Label("Task closed at "+t.closedDateFormatted());
	            closedAt.setStyle("font-size: 12px; color: gray; margin-top: 2px;");
	            textContainer.appendChild(closedAt);
	        }
	        
	        if(!inHistoric) {
		        row.addEventListener("onClick", e ->{
		        	if(editingTask == null || editingTask != t) {
		        		openNewTaskWindow();
			        	editingTask = t;
		        	} else if(t == editingTask) {
		        		closeNewTaskWindow();
		        		editingTask = null;
		        		return;
		        	}else {
		        		System.out.println("NOW WHAT???");
		        		return;
		        	}
		        	
			        
			        txTitle.setValue(editingTask.getTitle());
			        txNotes.setValue(editingTask.getNotes());
			        
			        if(editingTask.getTimeOfTheDay()!=null)
			        	tbTime.setValue(localTimeToDate(editingTask.getTimeOfTheDay()));
			       
			        if(editingTask instanceof TaskOnce) {
			        	taskFormatListModelList.addToSelection(TaskFormat.ONCE);
			        	
			        	onSelectDefaultOnTaskFormat();
			        	
			        }else if(editingTask instanceof TaskDaily) {
			        	taskFormatListModelList.addToSelection(TaskFormat.EVERY_DAY);
			        	
			        	onSelectDefaultOnTaskFormat();
			        	
			        }else if(editingTask instanceof TaskCustom tCostum) {
			        	taskFormatListModelList.addToSelection(TaskFormat.FREQUENCY);
			        	ibPeriod.setValue(tCostum.getPeriod());
			        	frequencyFormatListModelList.addToSelection(tCostum.getFrequencyTypes());
	
						onSelectFrequencyOnTaskFormat();
						
			        } else if(editingTask instanceof TaskDate tDate) {
			        	taskFormatListModelList.addToSelection(TaskFormat.DATE);
			        	dbSelectedDate.setValue(tDate.getSelectedDate());
			        	
			        	onSelectDateOnTaskFormat();
			        } 
		        });
	        }

	        row.appendChild(textContainer);

	        // Done button
	        if(!inHistoric) {
		        Button btDone = new Button(" ");
		        btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px;");
		        btDone.addEventListener("onClick", e -> {
		            List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
					ts.remove(t);
					tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
					tasksToday.remove(t);
		            t.setClosed();
		            new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());
		            
		            if(t instanceof TaskOnce || t instanceof TaskDate) {
		            	generateTaskList(tasksMap.get(selectedSection));
		        		generateTodayTaskList(tasksToday);
			            return;
		            }
	
	            	Task task = TaskFactory.createRollingTask(t);
	            	new TaskDAO().insert(task);

	    			tasksToday = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
	            	
	            	generateTaskList(tasksMap.get(selectedSection));
	        		generateTodayTaskList(tasksToday);
		        });
		        row.appendChild(btDone);
	        }

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
	            if(inHistoric) {
		    		closeHistoric();
	            }
	    		selectedSection = section;
	            generateSectionList();
	            generateTaskList(tasksMap.get(selectedSection));
	    	});
		    menuItems.appendChild(bt);
	    }
	    
	    if(showSectionTitle) {
	    	btProfile.setIconSclass("z-icon-user");
	    	btProfile.setSclass("profile-btn open");
	    	btProfile.setLabel(currentUser.getTitle());
	    }else {
	    	btProfile.setIconSclass("z-icon-user");
	    	btProfile.setSclass("profile-btn");
	    	btProfile.setLabel(null);
		}
	    
	}

	@Listen("onClick = #btToggleMenu")
	public void onClickbtToggleMenu(Event e) {
		toggleSectionMenu();
        generateSectionList();
	}
	
	private void toggleSectionMenu() {
		boolean collapsed = sidebar.getSclass().contains("collapsed");
		if (collapsed) {
			expandSectionMenu();
	    } else {
	    	collapseSectionMenu();
	    }
	}

	private void collapseSectionMenu() {
        // Collapse sidebar
        sidebar.setSclass("sidebar collapsed");
        showSectionTitle = false;
	}

	private void expandSectionMenu() {
        // Expand sidebar
        sidebar.setSclass("sidebar open");
        showSectionTitle = true;
	}
	

	@Listen("onClick = #teste")
	public void onClickbtDeleteBD(Event e) {
		new TaskDAO().deleteAll();
		new SectionDAO().deleteAll();
		new UserDAO().deleteAll();
		tasksMap = new HashMap<Section, List<Task>>();
		sections = new ArrayList<Section>();
		tasksToday = new ArrayList<Task>();
		
		User user = new User("Test", "teste", "password");
		user = new UserDAO().insert(user);
		
		Section section = new Section("To Do", "", user.getId());
		section = new SectionDAO().insert(section);
		sections.add(section);
		

		generateSectionList();
		generateTaskList(tasksMap.get(selectedSection));
		generateTodayTaskList(tasksToday);
		System.out.println("DB CLEARED....................");
	}

	@Listen("onClick = #btCreateTask")
	public void onClickbtCreateTask(Event e) {
		clearNewTaskWindow();
		toggleNewTaskWindow();
	}
	
	private void toggleNewTaskWindow() {
	    String sclass = addTaskForm.getSclass();
	    if (sclass.contains("open")) {
	    	closeNewTaskWindow();
	    } else {
	    	
	    	if(inHistoric) {
	    		closeHistoric();
	    	    return;
	    	}
	    	
	    	openNewTaskWindow();
	    }
	}
	
	private void closeHistoric() {
		inHistoric = false;
	    btCreateTask.setSclass("add-btn");
		generateSectionList();
	    generateTaskList(tasksMap.get(selectedSection));
	}

	@Listen("onClick = #btSaveNewTask")
	public void onClickbtSaveNewTask(Event e) {
		String title = txTitle.getValue();
		String notes = txNotes.getValue();
		TaskFormat selectedFormat = (TaskFormat) cbFormat.getSelectedItem().getValue();
		
		Task task = TaskFactory.createTask(selectedFormat, title, notes, selectedSection.getId(), currentUser.getId(), tbTime.getValueInLocalTime());
		
		if(editingTask != null) {
			task.setId(editingTask.getId());
			task.setStartDate(editingTask.getStartDate());
			editingTask = null;
		}
		
		if(task instanceof TaskCustom ) {
			((TaskCustom) task).setPeriod(ibPeriod.getValue());
			((TaskCustom) task).setFrequencyTypes(cbFrequency.getSelectedItem().getValue());
		} else if(task instanceof TaskDate) {
			((TaskDate) task).setSelectedDate(dbSelectedDate.getValue());
		}
		

		new TaskDAO().insert(task);
		addTaskToMap(task);
		tasksToday = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
		
    	closeNewTaskWindow();
		
		generateTaskList(tasksMap.get(selectedSection));
		generateTodayTaskList(tasksToday);
	}
	
	private void clearNewTaskWindow() {
		txTitle.setValue("");
	    txNotes.setValue("");
	    taskFormatListModelList.addToSelection(TaskFormat.ONCE);
	    tbTime.setValue(null);
	    
	    editingTask = null;

		onSelectDefaultOnTaskFormat();
	    
	    clearDateFields();
	    clearFrequencyFields();
	}
	
	private void openNewTaskWindow() {
        addTaskForm.setSclass("add-task-form open"); // slide in
        taskSection.setSclass("task-section retracted");
        btCreateTask.setSclass("add-btn rotate");
        txTitle.setFocus(true);
	}
	
	private void closeNewTaskWindow() {
		
		clearNewTaskWindow();
		
	    // close with animation
	    addTaskForm.setSclass("add-task-form");
	    taskSection.setSclass("task-section");
	    btCreateTask.setSclass("add-btn");
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
		}else {
			onSelectDefaultOnTaskFormat();
		}
    }
	
	private void onSelectDefaultOnTaskFormat() {
    	layoutForDate.setVisible(false);
    	layoutForFrequency.setVisible(false);	
	}

	private void onSelectDateOnTaskFormat() {
    	layoutForDate.setVisible(true);
    	layoutForFrequency.setVisible(false);
	}
	
	private void clearDateFields() {
    	dbSelectedDate.setValue(null);	
	}
	
	private void onSelectFrequencyOnTaskFormat() {
    	layoutForDate.setVisible(false);
    	layoutForFrequency.setVisible(true);
	}
	
	private void clearFrequencyFields() {
    	ibPeriod.setValue(null);
    	frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
	}

	@Listen("onClick = #btClearTime")
	public void onClickbtClearTime(Event e) {
		tbTime.setValue(null);
	}
	
	@Listen("onClick = #btAddSection")
	public void onClickbtAddSection(Event e) {
		editSectionTitle=false;
		txSection.setValue("");
		txSection.setFocus(true);
	}
	
	@Listen("onOK = #txSection")
	public void onEnterPresstxSection(Event event) {
		onClickbtSaveNewSection(null);
	}

	@Listen("onClick = #btSaveNewSection")
	public void onClickbtSaveNewSection(Event e) {
		String title = txSection.getValue();
		
		Section sectionToChec = new SectionDAO().findByTitle(title);
		if(sectionToChec!=null) {
			Clients.showNotification("Section already exists.", txSection);
			txSection.setFocus(true);
			return;
		}
		
		Section section = selectedSection;
		if(editSectionTitle) {
			
			selectedSection.setTitle(txSection.getValue());
			new SectionDAO().updateValue(selectedSection.getId(), SectionFields.TITLE, txSection.getValue());
			
			int index = sections.indexOf(section);
			sections.remove(index);
			sections.add(index, selectedSection);
			
			editSectionTitle=false;
		}else {
			section = new Section(title, "", currentUser.getId());
			section = new SectionDAO().insert(section);
			
			sections.add(section);
			tasksMap.put(section, new ArrayList<Task>());
		}

	    txSection.setValue("");
	    ppAddSection.close();
	    
		generateSectionList();
	}


	@Listen("onClick = #btLogout")
	public void onClickbtLogout(Event e) {
		AuthUtil.logout();
        Executions.sendRedirect("/login.zul");
	}

	@Listen("onClick = #btHistoric")
	public void onClickbtHistoric(Event e) {
		
        if(inHistoric) {
    		closeHistoric();
    		return;
        }
		
		inHistoric = true;
		lbSectionTitle.setValue("Historic");
		generateTaskList(new TaskDAO().findAllClosed(currentUser.getId()));
		btCreateTask.setSclass("add-btn rotate");
	}

	@Listen("onClick = #btEditTitleSection")
	public void onClickbtEditTitleSection(Event e) {
		editSectionTitle=true;
		txSection.setValue(selectedSection.getTitle());
	}
	
	@Listen("onClick = #btDeleteSection")
	public void onClickbtDeleteSection(Event e) {

		if(sections.size()<=1) {
			Clients.showNotification("Section can't be deleted.", btDeleteSection);
			return;
		}
		
		ppConfirmDeletion.open(btDeleteSection);
	}

	@Listen("onClick = #btYes")
	public void onClickbtYes(Event e) {
		
		new TaskDAO().deleteFromSection(selectedSection.getId());
		
		new SectionDAO().delete(selectedSection.getId());
		
		sections.remove(selectedSection);
		selectedSection = sections.get(0);
        generateSectionList();
        generateTaskList(tasksMap.get(selectedSection));
        
		tasksToday = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
		generateTodayTaskList(tasksToday);
		
		ppConfirmDeletion.close();
		
	}

	@Listen("onClick = #btNo")
	public void onClickbtNo(Event e) {
		ppConfirmDeletion.close();
	}
	
	
	@Listen("onTimer = #myTimer")
    public void runTask(Event event) {

        // Check if logged in
        checkIsLogedIn();
		
		if(inHistoric)
			return;
        
		List<Task> tasksTemp = new TaskDAO().findAllOpenByUser(selectedSection, currentUser.getId());
		List<Task> tasksTodayTemp = new TaskDAO().findAllOpenTodayByUser(currentUser.getId());
		
		if(tasksTemp != null && tasksTemp.size()>0) {
			List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(tasksTemp.get(0).getSection())).findFirst().get());
			if(ts.size()!=tasksTemp.size()) {
				tasksMap.put(sections.stream().filter(s -> s.getId().equals(tasksTemp.get(0).getSection())).findFirst().get(), tasksTemp);
				generateTaskList(tasksMap.get(selectedSection));
			}
		}
		
		if(tasksTodayTemp != null && tasksTodayTemp.size()!=tasksToday.size()) {
				tasksToday = tasksTodayTemp;
				generateTodayTaskList(tasksToday);
		}
	}
	
	private void addTaskToMap(Task t) {
		List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
		ts.add(t);
		tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
	}
	
	public static Date localTimeToDate(LocalTime localTime) {
        // Combine with today's date
        LocalDate today = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(today, localTime);

        // Convert to ZonedDateTime using system default zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

        // Convert to Instant, then to Date
        return Date.from(zonedDateTime.toInstant());
    }
}

