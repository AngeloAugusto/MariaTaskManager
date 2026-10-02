package pt.axxiv.mariatasks.connection.dao;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import pt.axxiv.mariatasks.connection.MongoDBConnectionOffline;
import pt.axxiv.mariatasks.connection.labels.SubTaskFields;
import pt.axxiv.mariatasks.data.*;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class SubTaskDAO {
	private final MongoCollection<Document> collection;

	public SubTaskDAO() {
		MongoDatabase db = MongoDBConnectionOffline.getDatabase();
        this.collection = db.getCollection(SubTaskFields.SUB_TASK_COLLECTION);
	}

	private SubTask createFromDocument(Document doc) {
	    SubTask subtask = new SubTask();

		subtask.setId(doc.getObjectId(SubTaskFields.ID));
		subtask.setTitle(doc.getString(SubTaskFields.TITLE));
		subtask.setParentTask(doc.getObjectId(SubTaskFields.PARENT_TASK));
		subtask.setCloseDate(doc.getDate(SubTaskFields.CLOSE_DATE));
		subtask.setCreatedDate(doc.getDate(SubTaskFields.CREATED_DATE));

	    return subtask;
	}

	public void insert(SubTask subtask) {
        Document doc = new Document(SubTaskFields.TITLE, subtask.getTitle())
				.append(SubTaskFields.PARENT_TASK, subtask.getParentTask())
				.append(SubTaskFields.CLOSE_DATE, subtask.getCloseDate())
				.append(SubTaskFields.CREATED_DATE, subtask.getCreatedDate());

	    if(subtask.getId() == null) {
	    	collection.insertOne(doc);
			subtask.setId(doc.getObjectId(SubTaskFields.ID));
	    }else {
	    	 collection.updateOne(eq(SubTaskFields.ID, subtask.getId()), new Document("$set", doc));
	    }
    }

	public SubTask findById(ObjectId id) {
	    Document doc = collection.find(eq(SubTaskFields.ID, id)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public List<SubTask> findByParent(ObjectId id) {
		List<SubTask> subTasks = new ArrayList<>();
		for (Document doc : collection.find(eq(SubTaskFields.PARENT_TASK, id)).sort(Sorts.orderBy(Sorts.ascending(SubTaskFields.CLOSE_DATE), Sorts.ascending(SubTaskFields.CREATED_DATE)))) {
			subTasks.add(createFromDocument(doc));
		}
		return subTasks;
	}

    public void updateValue(ObjectId id, String valueLabel, Object value) {
        collection.updateOne(eq(SubTaskFields.ID, id), new Document("$set", new Document(valueLabel, value)));
    }

    public void delete(ObjectId id) {
        collection.deleteOne(eq(SubTaskFields.ID, id));
    }

	public void deleteAll() {
		collection.deleteMany(new Document());
	}

	public SubTask update(SubTask subtask) {
	    if (subtask.getId() == null) {
	        throw new IllegalArgumentException("SubTask ID cannot be null for update operation");
	    }

	    // Check if task exists
	    Document existingDoc = collection.find(eq(SubTaskFields.ID, subtask.getId())).first();
	    if (existingDoc == null) {
	        throw new IllegalArgumentException("SubTask not found with id: " + subtask.getId());
	    }

	    // Create update document
	    Document updateDoc = new Document();

	    // Add all fields that should be updated
	    if (subtask.getTitle() != null) {
	        updateDoc.append(SubTaskFields.TITLE, subtask.getTitle());
	    }
		if (subtask.getParentTask() != null) {
			updateDoc.append(SubTaskFields.PARENT_TASK, subtask.getParentTask());
		}
		if (subtask.getCloseDate() != null) {
			updateDoc.append(SubTaskFields.CLOSE_DATE, subtask.getCloseDate());
		}

	    // Only update if there are fields to update
	    if (!updateDoc.isEmpty()) {
	        collection.updateOne(eq(SubTaskFields.ID, subtask.getId()), new Document("$set", updateDoc));
	    }

	    // Return the updated task
	    return findById(subtask.getId());
	}

}
