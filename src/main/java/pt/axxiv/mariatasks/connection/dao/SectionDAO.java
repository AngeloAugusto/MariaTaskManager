package pt.axxiv.mariatasks.connection.dao;


import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import pt.axxiv.mariatasks.connection.MongoDBConnectionOffline;
import pt.axxiv.mariatasks.connection.labels.SectionFields;
import pt.axxiv.mariatasks.data.Section;
import static com.mongodb.client.model.Filters.eq;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {
	private final MongoCollection<Document> collection;

	public SectionDAO() {
		MongoDatabase db = MongoDBConnectionOffline.getDatabase();
        this.collection = db.getCollection(SectionFields.COLLECTION);
	}
	
	private Section createFromDocument(Document doc) {
	    Section section = new Section();

	    section.setId(doc.getObjectId(SectionFields.ID));
	    section.setTitle(doc.getString(SectionFields.TITLE));
	    section.setImgPath(doc.getString(SectionFields.IMGPATH));
	    section.setOwnerId(doc.getObjectId(SectionFields.OWNER));
	    
	    return section;
	}
	
	public Section insert(Section section) {
        Document doc = new Document(SectionFields.TITLE, section.getTitle())
        		.append(SectionFields.IMGPATH, section.getImgPath())
        		.append(SectionFields.OWNER, section.getOwnerId());
        
        collection.insertOne(doc);
        section.setId(doc.getObjectId(SectionFields.ID));
        return section;
    }

	public Section findById(ObjectId id) {
	    Document doc = collection.find(eq(SectionFields.ID, id)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public Section findByTitle(String title) {
	    Document doc = collection.find(eq(SectionFields.TITLE, title)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public List<Section> findAllByUser(ObjectId owner) {
	    List<Section> sections = new ArrayList<>();
	    for (Document doc : collection.find(eq(SectionFields.OWNER, owner))) {
	    	sections.add(createFromDocument(doc));
	    }
	    return sections;
	}

    public void updateValue(ObjectId id, String valueLabel, Object value) {
        collection.updateOne(eq(SectionFields.ID, id), new Document("$set", new Document(valueLabel, value)));
    }

    public void delete(ObjectId id) {
        collection.deleteOne(eq(SectionFields.ID, id));
    }

	public void deleteAll() {
		collection.deleteMany(new Document());
	}

}
