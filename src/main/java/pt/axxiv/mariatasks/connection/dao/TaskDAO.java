package pt.axxiv.mariatasks.connection.dao;


import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import pt.axxiv.mariatasks.connection.MongoDBConnectionOffline;
import pt.axxiv.mariatasks.connection.labels.TaskFields;
import pt.axxiv.mariatasks.data.FrequencyTypes;
import pt.axxiv.mariatasks.data.Section;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.TaskCustom;
import pt.axxiv.mariatasks.data.TaskDaily;
import pt.axxiv.mariatasks.data.TaskDate;
import pt.axxiv.mariatasks.data.TaskOnce;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TaskDAO {
	private final MongoCollection<Document> collection;

	public TaskDAO() {
		MongoDatabase db = MongoDBConnectionOffline.getDatabase();
        this.collection = db.getCollection(TaskFields.TASK_COLLECTION);
	}
	
	private Task createFromDocument(Document doc) {
	    String type = doc.getString(TaskFields.TYPE);
	    Task task;
	    if(type == null) {
	    	task = new Task();
	    }else {
		    switch (type) {
		        case "TaskOnce" -> task = new TaskOnce();
		        case "TaskDaily" -> task = new TaskDaily();
		        case "TaskCustom" -> task = new TaskCustom();
		        case "TaskDate" -> task = new TaskDate();
		        default -> task = new Task(); // fallback
		    }
	    }
	    
	    task.setId(doc.getObjectId(TaskFields.ID));
	    task.setTitle(doc.getString(TaskFields.TITLE));
	    task.setStartDate(doc.getDate(TaskFields.START_DATE));
	    task.setCloseDate(doc.getDate(TaskFields.CLOSE_DATE));
	    task.setNotes(doc.getString(TaskFields.NOTES));
	    task.setParent(doc.getObjectId(TaskFields.PARENT));
	    task.setSection(doc.getObjectId(TaskFields.SECTION));
	    task.setOwnerId(doc.getObjectId(TaskFields.OWNER));
	    
	    Date date = doc.getDate(TaskFields.TIME_OF_THE_DAY);
	    if(date != null)
	    	task.setTimeOfTheDay(date.toInstant().atZone(ZoneOffset.UTC).toLocalTime());

	    if (task instanceof TaskCustom costum) {
	    	costum.setPeriod(doc.getInteger(TaskFields.PERIOD));
	    	costum.setFrequencyTypes(FrequencyTypes.fromValue(doc.getInteger(TaskFields.FREQUENCY_TYPE)));
        }
	    if (task instanceof TaskDate costum) {
	    	costum.setSelectedDate(doc.getDate(TaskFields.SELECTED_DATE));
        }
	    
	    return task;
	}
	
	public void insert(Task task) {
        Document doc = new Document(TaskFields.TITLE, task.getTitle())
        		.append(TaskFields.START_DATE, task.getStartDate())
        		.append(TaskFields.CLOSE_DATE, task.getCloseDate())
        		.append(TaskFields.NOTES, task.getNotes())
        		.append(TaskFields.PARENT, task.getParent())
        		.append(TaskFields.SECTION, task.getSection())
        		.append(TaskFields.TIME_OF_THE_DAY, task.getTimeOfTheDay())
        		.append(TaskFields.TYPE, task.getClass().getSimpleName())
        		.append(TaskFields.OWNER, task.getOwnerId());
        
        if (task instanceof TaskCustom costum) {
        	doc.append(TaskFields.PERIOD, costum.getPeriod());
        	doc.append(TaskFields.FREQUENCY_TYPE, costum.getFrequencyTypes().getValue());
        }
	    if (task instanceof TaskDate costum) {
        	doc.append(TaskFields.SELECTED_DATE, costum.getSelectedDate());
        }
        
	    if(task.getId() == null) {
	    	collection.insertOne(doc);
        	task.setId(doc.getObjectId(TaskFields.ID));
	    }else {
	    	 collection.updateOne(eq(TaskFields.ID, task.getId()), new Document("$set", doc));
	    }
    }

	public Task findById(ObjectId id) {
	    Document doc = collection.find(eq(TaskFields.ID, id)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public List<Task> findAll() {
	    List<Task> tasks = new ArrayList<>();
	    for (Document doc : collection.find()) {
	        tasks.add(createFromDocument(doc));
	    }
	    return tasks;
	}


    public List<Task> findAllOpenByUser(ObjectId userId) {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find(and(eq(TaskFields.CLOSE_DATE, null),eq(TaskFields.OWNER, userId)))) {
        	Task task = createFromDocument(doc);
	        
	        if (task.getStartDate() == null || !task.getStartDate().after(new Date())) {
	            tasks.add(task);
	        }
	    }
        
        Collections.sort(tasks, Collections.reverseOrder());
        
        return tasks;
    }


    public List<Task> findAllOpenByUser(Section section, ObjectId userId) {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find(and(eq(TaskFields.CLOSE_DATE, null),eq(TaskFields.OWNER, userId)))) {
        	Task task = createFromDocument(doc);
	        
	        if ((task.getStartDate() == null || !task.getStartDate().after(new Date())) && task.getSection().equals(section.getId())) {
	            tasks.add(task);
	        }
	    }
        
        Collections.sort(tasks, Collections.reverseOrder());
        
        return tasks;
    }


    public List<Task> findAllOpenTodayByUser(ObjectId userId) {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find(and(eq(TaskFields.CLOSE_DATE, null),eq(TaskFields.OWNER, userId)))) {
        	Task task = createFromDocument(doc);
	        
        	// TaskDaily and start date <= today
        	// TaskDate and selected_date <= today
        	// TaskCustom and start date <= today
        	// Task with TimeOfDay
        	
	        if (((task instanceof TaskDaily taskDaily)) && !taskDaily.getStartDate().after(new Date()) ||
	        		((task instanceof TaskCustom taskCustom)) && !taskCustom.getStartDate().after(new Date()) ||
	        		((task instanceof TaskDate taskDate)) && !taskDate.getSelectedDate().after(new Date()) ||
	        		(task.getTimeOfTheDay() != null && (task instanceof TaskOnce))) {
	            tasks.add(task);
	        }
	    }
        
        Collections.sort(tasks, Collections.reverseOrder());
        
        return tasks;
    }

    public List<Task> findAllClosed() {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find(ne(TaskFields.CLOSE_DATE, null)).sort(new BasicDBObject(TaskFields.CLOSE_DATE, -1))) {
	        tasks.add(createFromDocument(doc));
	    }
        return tasks;
    }

    public List<Task> findAllClosed(ObjectId userId) {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find(ne(TaskFields.CLOSE_DATE, null)).sort(new BasicDBObject(TaskFields.CLOSE_DATE, -1))) {
        	Task task = createFromDocument(doc);
        	if(task.getOwnerId().equals(userId))
        		tasks.add(createFromDocument(doc));
	    }
        return tasks;
    }

    public void updateValue(ObjectId id, String valueLabel, Object value) {
        collection.updateOne(eq(TaskFields.ID, id), new Document("$set", new Document(valueLabel, value)));
    }

    public void delete(ObjectId id) {
        collection.deleteOne(eq(TaskFields.ID, id));
    }

	public void deleteAll() {
		collection.deleteMany(new Document());
	}
	
	public static boolean isTodayOrBefore(Date date) {
	    if (date == null) return false;

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    zeroTime(cal);

	    Calendar now = Calendar.getInstance();
	    zeroTime(now);

	    // If date is before or equal to today
	    return !cal.after(now);
	}

	private static void zeroTime(Calendar cal) {
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	}

}
