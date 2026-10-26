package pt.axxiv.mariatasks.connection.dao;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import pt.axxiv.mariatasks.connection.MongoDBConnectionOffline;
import pt.axxiv.mariatasks.connection.labels.BirthdayFields;
import pt.axxiv.mariatasks.connection.labels.TaskFields;
import pt.axxiv.mariatasks.data.Birthday;
import pt.axxiv.mariatasks.data.Task;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BirthdayDAO {
	private final MongoCollection<Document> collection;

	public BirthdayDAO() {
		MongoDatabase db = MongoDBConnectionOffline.getDatabase();
        this.collection = db.getCollection(BirthdayFields.COLLECTION);
	}

	private Birthday createFromDocument(Document doc) {
		Birthday birthday = new Birthday();

		birthday.setId(doc.getObjectId(BirthdayFields.ID));
		birthday.setName(doc.getString(BirthdayFields.NAME));
		String mdString = doc.getString(BirthdayFields.MONTH_DAY);
		if (mdString != null) {
			birthday.setMonthDay(MonthDay.parse(mdString));
		}
		birthday.setSection(doc.getObjectId(BirthdayFields.SECTION));
		birthday.setOwnerId(doc.getObjectId(BirthdayFields.OWNER));

	    return birthday;
	}

	public void insert(Birthday birthday) {
        Document doc = new Document(BirthdayFields.NAME, birthday.getName())
				.append(BirthdayFields.MONTH_DAY, birthday.getMonthDay().toString())
				.append(BirthdayFields.SECTION, birthday.getSection())
				.append(BirthdayFields.OWNER, birthday.getOwnerId());

	    if(birthday.getId() == null) {
	    	collection.insertOne(doc);
			birthday.setId(doc.getObjectId(BirthdayFields.ID));
	    }else {
	    	 collection.updateOne(eq(BirthdayFields.ID, birthday.getId()), new Document("$set", doc));
	    }
    }

	public Birthday findById(ObjectId id) {
		Document doc = collection.find(eq(BirthdayFields.ID, id)).first();
		if (doc != null) {
			return createFromDocument(doc);
		}
		return null;
	}

	public List<Birthday> findAllBySection(ObjectId sectionId) {
		List<Birthday> birthdays = new ArrayList<>();
		for (Document doc : collection.find(eq(BirthdayFields.SECTION, sectionId))) {
			birthdays.add(createFromDocument(doc));
		}

		LocalDate today = LocalDate.now(ZoneId.of("Europe/Lisbon"));

		birthdays.sort(Comparator.comparingInt(b -> daysUntilNextBirthday(b.getMonthDay(), today)));

		return birthdays;
	}

	private int daysUntilNextBirthday(MonthDay md, LocalDate today) {
		if (md == null) return Integer.MAX_VALUE;

		LocalDate next = nextOccurrence(md, today.getYear());
		if (next.isBefore(today)) {
			next = nextOccurrence(md, today.getYear() + 1);
		}
		return (int) ChronoUnit.DAYS.between(today, next);
	}

	private LocalDate nextOccurrence(MonthDay md, int year) {
		if (md.getMonthValue() == 2 && md.getDayOfMonth() == 29 && !Year.isLeap(year)) {
			return LocalDate.of(year, 2, 28);
		}
		return md.atYear(year);
	}

    public void updateValue(ObjectId id, String valueLabel, Object value) {
        collection.updateOne(eq(BirthdayFields.ID, id), new Document("$set", new Document(valueLabel, value)));
    }

    public void delete(ObjectId id) {
        collection.deleteOne(eq(BirthdayFields.ID, id));
    }

	public void deleteAll() {
		collection.deleteMany(new Document());
	}

	public Birthday update(Birthday birthday) {
	    if (birthday.getId() == null) {
	        throw new IllegalArgumentException("Birthday ID cannot be null for update operation");
	    }

	    // Check if task exists
	    Document existingDoc = collection.find(eq(BirthdayFields.ID, birthday.getId())).first();
	    if (existingDoc == null) {
	        throw new IllegalArgumentException("Birthday not found with id: " + birthday.getId());
	    }

	    // Create update document
	    Document updateDoc = new Document();

	    // Add all fields that should be updated
	    if (birthday.getName() != null) {
	        updateDoc.append(BirthdayFields.NAME, birthday.getName());
	    }
		if (birthday.getMonthDay() != null) {
			updateDoc.append(BirthdayFields.MONTH_DAY, birthday.getMonthDay().toString());
		}
		if (birthday.getSection() != null) {
			updateDoc.append(BirthdayFields.SECTION, birthday.getSection());
		}
		if (birthday.getOwnerId() != null) {
			updateDoc.append(BirthdayFields.OWNER, birthday.getOwnerId());
		}

	    // Only update if there are fields to update
	    if (!updateDoc.isEmpty()) {
	        collection.updateOne(eq(BirthdayFields.ID, birthday.getId()), new Document("$set", updateDoc));
	    }

	    // Return the updated task
	    return findById(birthday.getId());
	}

}
