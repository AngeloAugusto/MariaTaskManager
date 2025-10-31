package pt.axxiv.mariatasks.connection;

import org.bson.Document;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnectionOnline {
    private static final String DATABASE_NAME = "mariaDB";

    private static MongoClient mongoClient = null;

    private MongoDBConnectionOnline() {}

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
        	Dotenv dotenv = Dotenv.load();
            String connectionUri = String.format(
                "mongodb+srv://%s:%s@%s/?retryWrites=true&w=majority&appName=axxivCluster",
                dotenv.get("MONGO_USER"), dotenv.get("MONGO_PASS"),  dotenv.get("MONGO_HOST")
            );

            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionUri))
                    .serverApi(serverApi)
                    .build();

            mongoClient = MongoClients.create(settings);

            // Test connection
            try {
                MongoDatabase adminDB = mongoClient.getDatabase("admin");
                adminDB.runCommand(new Document("ping", 1));
                System.out.println("✅ Successfully connected to MongoDB!");
            } catch (MongoException e) {
                System.err.println("❌ MongoDB connection failed: " + e.getMessage());
            }
        }

        return mongoClient.getDatabase(DATABASE_NAME);
    }
}
