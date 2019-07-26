package db.mongodb;

import java.text.ParseException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBTableCreation { // 新建
  // Run as Java application to create MongoDB collections with index.
  public static void main(String[] args) throws ParseException {

		// Step 1, connection to MongoDB
		MongoClient mongoClient = new MongoClient(); // 连接
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME); // 读取

		// Step 2, remove old collections.
		db.getCollection("users").drop();
		db.getCollection("items").drop();

		// Step 3, create new collections
		IndexOptions options = new IndexOptions().unique(true); // 声明一个unique的index
		db.getCollection("users").createIndex(new Document("user_id", 1), options); // 1：从小到大排列。
		db.getCollection("items").createIndex(new Document("item_id", 1), options);

		// Step 4, insert fake user data and create index.
		db.getCollection("users").insertOne(
				new Document().append("user_id", "fake").append("password", "3229c1097c00d497a0fd282d586be050")
						.append("first_name", "Fake").append("last_name", "User"));

		mongoClient.close();
		System.out.println("Import is done successfully.");

  }
}

