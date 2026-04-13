package pt.axxiv.mariataskswebapp.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
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
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

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
import pt.axxiv.mariataskswebapp.auth.AuthUtil;

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
	private Combobox cbSection;
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
	private Popup ppConfirmDeletionTask;
	@Wire
	private Textbox txSection;
	@Wire
	private Button btProfile;
	@Wire
	private Button btDeleteSection;
	@Wire
	private Button btDeleteTask;
	@Wire
    private Timer myTimer;
	@Wire
    private Div iconContainer;
    @Wire
    private Button btAddSection;
    @Wire
    private Button btIcon;
    @Wire
	private Popup ppIcon;
    @Wire
    private Button btEditTitleSection;

    private Window window;

	private List<Section> sections = new ArrayList<Section>();
	private Map<Section, List<Task>> tasksMap = new HashMap<Section, List<Task>>();
	private ListModelList<FrequencyTypes> frequencyFormatListModelList;
	private ListModelList<TaskFormat> taskFormatListModelList ;

	private boolean showSectionTitle = false;
	private boolean inHistoric = false;
	private boolean editSectionTitle = false;
	private Section selectedSection;
	
	private Task editingTask = null;

	private User currentUser;
	
	List<String> icons = Arrays.asList(
		    "home", "search", "user", "cog", "cogs", "wrench", "trash-o",
		    "edit", "pencil", "pencil-square-o", "save", "print",
		    "bell", "bell-o", "envelope", "envelope-o", "calendar", "calendar-o",
		    "clock-o", "heart", "star", "star-o", "bookmark", "bookmark-o",
		    "tag", "tags", "book", "file", "files-o", "folder", "folder-open",
		    "folder-o", "folder-open-o",
		    "check", "times", "times-circle", "times-circle-o", "close", 
		    "remove", "warning", "info", "question", "ban",
		    "play", "pause", "stop", "video-camera", "camera", "camera-retro", "image",
		    "shopping-cart", "shopping-bag", "shopping-basket", "credit-card",
		    "money", "dollar", "euro", "gift", "barcode",
		    "facebook-square", "twitter-square", "linkedin-square", "github-square",
		    "google-plus-square", "instagram", "youtube-play",
		    "briefcase", "building", "desktop", "file-text", "users",
		    "heartbeat", "medkit", "bicycle", "apple", "paw",
		    "graduation-cap", "calculator", "flask", "globe",
		    "plane", "car", "train", "map-marker", "suitcase",
		    "cutlery", "tv", "paint-brush", "trash",
		    "bank", "pie-chart",
		    "lightbulb-o", "magic", "rocket", "flag", "bullseye",
		    "film", "music", "gamepad", "headphones", "ticket",
		    "cloud", "database", "coffee", "bed", "leaf", "futbol-o", 
		    "trophy", "fire", "phone", "comments"
		);
	
	
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
		window=comp;

		currentUser = new UserDAO().findById((ObjectId) Sessions.getCurrent().getAttribute("currentUserId"));
		
		if(currentUser == null) {
			return;
		}

		updateSections();
		selectedSection = sections.get(0);
		generateSectionList();
		updateTaskMap();

		window.getPage().setTitle("MariaTasks - "+selectedSection.getTitle());
		
		
		taskFormatListModelList = new ListModelList<TaskFormat>(TaskFormat.values());
		cbFormat.setModel(taskFormatListModelList);
		
		frequencyFormatListModelList = new ListModelList<FrequencyTypes>(FrequencyTypes.values());
		frequencyFormatListModelList.addToSelection(FrequencyTypes.BY_DAY);
		cbFrequency.setModel(frequencyFormatListModelList);
		
		taskFormatListModelList.addToSelection(TaskFormat.ONCE);
		
		createIconGrid();

	}
	
	@Listen("onClick = #btIcon")
	public void onClickBtIcon(Event e) {
	    ppIcon.open(btIcon, "before_end");
	}
	


	@Listen("onOK = #txTitle")
	public void onEnterPress(Event event) {
		onClickbtSaveNewTask(null);
	}

	private void generateTodayTaskList(List<Task> tasks) {
		while (todayTaskList.getFirstChild() != null)
			todayTaskList.removeChild(todayTaskList.getFirstChild());
	    
	    if(tasks == null || tasks.size()<=0)
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
	        
	        row.addEventListener("onClick", e -> onClickOnTaskRow(t));
	        row.appendChild(textContainer);

	        // Done button
	        Button btDone = new Button(" ");
	        btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px;");
	        btDone.addEventListener("onClick", e -> onClickDone(t));
	        row.appendChild(btDone);
	        

	        todayTaskList.appendChild(row);
	    }
	}
	
	private void onClickDone(Task t) {
		if(editingTask != null) {
    		closeNewTaskWindow();
    		editingTask = null;
    	}
		
		if(inHistoric)
			closeHistoric();
		
        t.setClosed();
        new TaskDAO().updateValue(t.getId(), TaskFields.CLOSE_DATE, t.getCloseDate());
        
        Task task = TaskFactory.createRollingTask(t);
        if(task != null) {
	    	new TaskDAO().insert(task);
        }
        
        updateTaskMap();
	}
	
	private void onClickOnTaskRow(Task t) {
		if(editingTask == null || editingTask != t) {
        	editingTask = t;
    		openNewTaskWindow();

        	btDeleteTask.setVisible(true);
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
	}

	private void generateTaskList(List<Task> tasks) {
	    while (taskList.getFirstChild() != null)
	        taskList.removeChild(taskList.getFirstChild());

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
	        
	        if (inHistoric || t.getCloseDate()!= null) {
	            Label closedAt = new Label("Task closed at "+t.closedDateFormatted());
	            closedAt.setStyle("font-size: 12px; color: gray; margin-top: 2px;");
	            textContainer.appendChild(closedAt);
	        }
	        
	        if(!inHistoric && t.getCloseDate()== null) {
		        row.addEventListener("onClick", e ->onClickOnTaskRow(t));
	        }

	        row.appendChild(textContainer);

	        // Done button
	        if(!inHistoric) {
		        Button btDone = new Button(" ");
	        	btDone.setStyle("margin-left:auto; background: #242526; border: 2px solid white; padding: 0px; height: 30px; width: 30px;margin-right: 20px; color: white;");
		        if(t.getCloseDate() != null) {
		        	btDone.setDisabled(t.getCloseDate() != null);
		        	btDone.setIconSclass("z-icon-check");
		        }
		        btDone.addEventListener("onClick", e -> onClickDone(t));
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
	    	bt.setIconSclass(section.getIcon());
	    	if(showSectionTitle) {
		    	bt.setSclass("menu-item open" + (selectedSection.equals(section) ? " selected" : ""));
	    		bt.setLabel(section.getTitle());
	    	}
	    	
	    	bt.addEventListener("onClick", e ->{
	            if(inHistoric) {
		    		closeHistoric();
	            }
	    		selectedSection = section;
	    		window.getPage().setTitle("MariaTasks - "+selectedSection.getTitle());
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
			task.setSection(cbSection.getSelectedItem().getValue());
			editingTask = null;
		}
		
		if(task instanceof TaskCustom ) {
			((TaskCustom) task).setPeriod(ibPeriod.getValue());
			((TaskCustom) task).setFrequencyTypes(cbFrequency.getSelectedItem().getValue());
		} else if(task instanceof TaskDate) {
			((TaskDate) task).setSelectedDate(dbSelectedDate.getValue());
		}
		

		new TaskDAO().insert(task);
		
		updateTaskMap();
		
    	closeNewTaskWindow();
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
        
        if(editingTask != null) {
        	cbSection.setVisible(true);
        	List<Section> tempSec = sections;
        	Section taskSection = tempSec.stream().filter(e -> e.getId().equals(editingTask.getSection())).collect(Collectors.toList()).get(0);
        	ListModelList<Section> sectionModelList = new ListModelList<Section>(tempSec);
        	cbSection.setModel(sectionModelList);
        	sectionModelList.addToSelection(taskSection);
        	cbSection.setItemRenderer((item, data, index) -> {
        		Section section = (Section) data;
                item.setLabel(section.getTitle());
                item.setValue(section.getId());
            });
        }else {
        	cbSection.setVisible(false);
        }
	}
	
	private void closeNewTaskWindow() {
		
		clearNewTaskWindow();
		
	    // close with animation
	    addTaskForm.setSclass("add-task-form");
	    taskSection.setSclass("task-section");
	    btCreateTask.setSclass("add-btn");

    	btDeleteTask.setVisible(false);
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
		dbSelectedDate.setValue(new Date());
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
		
		ppAddSection.open(btAddSection, "before_start");
	}
	
	@Listen("onOK = #txSection")
	public void onEnterPresstxSection(Event event) {
		onClickbtSaveNewSection(null);
	}

	@Listen("onClick = #btSaveNewSection")
	public void onClickbtSaveNewSection(Event e) {
		String title = txSection.getValue().trim();
		
		Section sectionToChec = new SectionDAO().findByTitle(title, currentUser.getId());
		if(sectionToChec!=null && !sectionToChec.getId().equals(selectedSection.getId())) {
			Clients.showNotification("Section already exists.", txSection);
			txSection.setFocus(true);
			return;
		}
		
		if(title==null || title.isBlank() || title.isEmpty()) {
			Clients.showNotification("Title cannot be empty", txSection);
			txSection.setFocus(true);
			return;
		}
		
		Section section = selectedSection;
		if(editSectionTitle) {
			
			selectedSection.setTitle(txSection.getValue());
			selectedSection.setIcon(btIcon.getIconSclass());
			new SectionDAO().updateValue(selectedSection.getId(), SectionFields.TITLE, txSection.getValue());
			new SectionDAO().updateValue(selectedSection.getId(), SectionFields.ICON, btIcon.getIconSclass());
			
			int index = sections.indexOf(section);
			sections.remove(index);
			sections.add(index, selectedSection);
			
			window.getPage().setTitle("MariaTasks - "+selectedSection.getTitle());
			
			editSectionTitle=false;
		}else {
			section = new Section(title, btIcon.getIconSclass(), currentUser.getId());
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
		ppAddSection.open(btEditTitleSection, "after_start");
		editSectionTitle=true;
		txSection.setValue(selectedSection.getTitle());
		btIcon.setIconSclass(selectedSection.getIcon());
	}
	
	@Listen("onClick = #btDeleteTask")
	public void onClickbtDeleteTask(Event e) {
		ppConfirmDeletionTask.open(btDeleteTask);
	}
	
	@Listen("onClick = #btTaskYes")
	public void onClickbtTaskYes(Event e) {
		removeTaskFromMap(editingTask);
		
		new TaskDAO().delete(editingTask.getId());
		
        updateTaskMap();
		
		ppConfirmDeletionTask.close();
		closeNewTaskWindow();
		
	}
	
	@Listen("onClick = #btTaskNo")
	public void onClickbtTaskNo(Event e) {
		ppConfirmDeletionTask.close();
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
		window.getPage().setTitle("MariaTasks - "+selectedSection.getTitle());
        generateSectionList();
        updateTaskMap();
		
		ppConfirmDeletion.close();
		
	}

	@Listen("onClick = #btNo")
	public void onClickbtNo(Event e) {
		ppConfirmDeletion.close();
	}
	
	
//	@Listen("onTimer = #myTimer")
//    public void runTask(Event event) {
//
//        // Check if logged in
//        checkIsLogedIn();
//        
//		updateSections();
//		updateTaskMap();
//	}
	
	private void createIconGrid() {

		for (String icon : icons) {
		    Button btn = new Button();
		    btn.setSclass("add-btn");
		    btn.setIconSclass("z-icon-" + icon);

		    btn.addEventListener(Events.ON_CLICK, e -> {
		        btIcon.setIconSclass("z-icon-" + icon);
		        ppIcon.close();
		    });

		    iconContainer.appendChild(btn);
		}
    }
	
	private void addTaskToMap(Task t) {
		List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
		ts.add(t);
		tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
	}
	
	private void removeTaskFromMap(Task t) {
		List<Task> ts = tasksMap.get(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get());
		ts.remove(t);
		tasksMap.put(sections.stream().filter(s -> s.getId().equals(t.getSection())).findFirst().get(), ts);
	}
	
	private void updateTaskMap() {
	    tasksMap = new HashMap<Section, List<Task>>();
	    for(Section s : sections) {
	        tasksMap.put(s, new ArrayList<Task>());
	    }
	    
	    Set<Task> allTasks = new HashSet<>();
	    allTasks.addAll(new TaskDAO().findAllOpenByUser(currentUser.getId()));
	    allTasks.addAll(new TaskDAO().findAllClosedTodayByUser(currentUser.getId()));
	    
	    List<Task> sortedTasks = new ArrayList<>(allTasks);

		sortedTasks.sort(
		    Comparator
		        // 1. Not closed first (false < true)
		        .comparing((Task t) -> t.getCloseDate() != null)
		
		        // 2. Then by startDate (older → newer)
		        .thenComparing(Task::getCloseDate, Comparator.nullsFirst(Comparator.naturalOrder()))
		        .thenComparing(Task::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
		);
		    
	    for(Task t : sortedTasks) {
	        addTaskToMap(t);
	    }

	    generateTaskList(tasksMap.get(selectedSection));
		generateTodayTaskList(new TaskDAO().findAllOpenTodayByUser(currentUser.getId()));
	}
	
	private void updateSections() {
		sections = new SectionDAO().findAllByUser(currentUser.getId());
		
		if(sections.size()<=0) {
			Section section = new Section("To Do", "z-icon-pencil-square-o", currentUser.getId());
			section = new SectionDAO().insert(section);
			sections.add(section);
		}
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

