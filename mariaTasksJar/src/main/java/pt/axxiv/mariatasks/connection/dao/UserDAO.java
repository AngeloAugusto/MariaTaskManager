package pt.axxiv.mariatasks.connection.dao;


import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import pt.axxiv.mariatasks.connection.MongoDBConnectionOffline;
import pt.axxiv.mariatasks.connection.labels.UserFields;
import pt.axxiv.mariatasks.data.User;
import static com.mongodb.client.model.Filters.eq;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserDAO {
	private final MongoCollection<Document> collection;

	public UserDAO() {
		MongoDatabase db = MongoDBConnectionOffline.getDatabase();
        this.collection = db.getCollection(UserFields.COLLECTION);
        
        createTTLIndex();
	}
	
	private void createTTLIndex() {
        try {
            IndexOptions indexOptions = new IndexOptions().expireAfter(30L, TimeUnit.DAYS);
            collection.createIndex(Indexes.ascending(UserFields.BEARER_TOKEN_EXPIRY), indexOptions);
        } catch (Exception e) {
            System.err.println("Note: TTL index might already exist: " + e.getMessage());
        }
    }
	
	private User createFromDocument(Document doc) {
	    User user = new User();

	    user.setId(doc.getObjectId(UserFields.ID));
	    user.setTitle(doc.getString(UserFields.TITLE));
	    user.setUsername(doc.getString(UserFields.USERNAME));
	    user.setPassword(doc.getString(UserFields.PASSWORD));
	    user.setBearerToken(doc.getString(UserFields.BEARER_TOKEN));
	    user.setBearerTokenExpiry(doc.getDate(UserFields.BEARER_TOKEN_EXPIRY));
	    
	    return user;
	}
	
	public User insert(User user) {
        Document doc = new Document(UserFields.TITLE, user.getTitle())
        		.append(UserFields.USERNAME, user.getUsername())
        		.append(UserFields.PASSWORD, user.getPassword());
        
        collection.insertOne(doc);
        user.setId(doc.getObjectId(UserFields.ID));
        return user;
    }

	public User findById(ObjectId id) {
	    Document doc = collection.find(eq(UserFields.ID, id)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public User findByUsername(String name) {
	    Document doc = collection.find(eq(UserFields.USERNAME, name)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

	public User findByRememberToken(String token) {
	    Document doc = collection.find(eq(UserFields.REMEMBER_TOKEN, token)).first();
	    if (doc != null) {
	        return createFromDocument(doc);
	    }
	    return null;
	}

    public User findByBearerToken(String bearerToken) {
        Document doc = collection.find(eq(UserFields.BEARER_TOKEN, bearerToken)).first();
        if (doc != null) {
            User user = createFromDocument(doc);
            
            // Check if token is expired
            if (user.isBearerTokenExpired()) {
                // Auto-cleanup expired token
                updateValue(user.getId(), UserFields.BEARER_TOKEN, null);
                updateValue(user.getId(), UserFields.BEARER_TOKEN_EXPIRY, null);
                return null;
            }
            
            return user;
        }
        return null;
    }
    
    public void updateBearerToken(ObjectId userId, String bearerToken, Date expiryDate) {
        Document updateDoc = new Document("$set", new Document()
            .append(UserFields.BEARER_TOKEN, bearerToken)
            .append(UserFields.BEARER_TOKEN_EXPIRY, expiryDate));
        
        collection.updateOne(eq(UserFields.ID, userId), updateDoc);
    }
    
    public void clearBearerToken(ObjectId userId) {
        Document updateDoc = new Document("$set", new Document()
            .append(UserFields.BEARER_TOKEN, null)
            .append(UserFields.BEARER_TOKEN_EXPIRY, null));
        
        collection.updateOne(eq(UserFields.ID, userId), updateDoc);
    }

	public List<User> findAll() {
	    List<User> users = new ArrayList<>();
	    for (Document doc : collection.find()) {
	    	users.add(createFromDocument(doc));
	    }
	    return users;
	}

    public void updateValue(ObjectId id, String valueLabel, Object value) {
        collection.updateOne(eq(UserFields.ID, id), new Document("$set", new Document(valueLabel, value)));
    }

    public void delete(ObjectId id) {
        collection.deleteOne(eq(UserFields.ID, id));
    }

	public void deleteAll() {
		collection.deleteMany(new Document());
	}

}
