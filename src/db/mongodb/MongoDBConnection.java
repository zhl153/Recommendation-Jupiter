package db.mongodb;

import java.util.List;
import java.util.Set;

import java.util.HashSet;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import entity.Item.ItemBuilder;
import external.TicketMasterAPI;
import db.DBConnection;
import entity.Item;

public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;
	
	public MongoDBConnection() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, String itemIds) { // 点击favorite加入收藏（数据库）
		// TODO Auto-generated method stub
		// 一个key-value pair是一个new Document
		db.getCollection("users").updateOne(new Document("user_id", userId),
//				new Document("$push", new Document("favorite", new Document("$each", itemIds)))); // 一个一个加入favorite
				new Document("$push", new Document("favorite", itemIds))); // String? List<String>
	}

	@Override
	public void unsetFavoriteItems(String userId, String itemIds) {
		// TODO Auto-generated method stub
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pullAll", new Document("favorite", itemIds))); // 移除所有favorite的value
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		Set<String> favoriteItems = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		
		if (iterable.first() != null && iterable.first().containsKey("favorite")) { // 不是空且存在favorite的key
			@SuppressWarnings("unchecked") // 声明强制转化没有问题
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItems.addAll(list);
		}

		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		Set<Item> favoriteItems = new HashSet<>();
		
		Set<String> itemIds = getFavoriteItemIds(userId); // 找到对应user的favorite的item的ids
		for (String itemId : itemIds) { // 根据itemid找item信息
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				Document doc = iterable.first(); // 找到对应的document
				
				ItemBuilder builder = new ItemBuilder(); // 使用builder创建，参数太多时可避免过多的constructor
				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setUrl(doc.getString("url"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setRating(doc.getDouble("rating"));
				builder.setDistance(doc.getDouble("distance"));
				builder.setCategories(getCategories(itemId));
				
				favoriteItems.add(builder.build()); // 创建出item
			}
		}

		return favoriteItems;

	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		
		if (iterable.first() != null && iterable.first().containsKey("categories")) { // 存在categories这个field，则加入
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}

		return categories;

	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub // 从TicketMaster中搜索
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item); // 自定义函数↓
		}
		return items; 
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub // 搜索结果写入， 没有ignore
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId())); // 查找
		// 若以存在则跳过 IGNORE
		if (iterable.first() == null) {
			db.getCollection("items")
					.insertOne(new Document().append("item_id", item.getItemId()).append("distance", item.getDistance())
							.append("name", item.getName()).append("address", item.getAddress())
							.append("url", item.getUrl()).append("image_url", item.getImageUrl())
							.append("rating", item.getRating()).append("categories", item.getCategories()));
		}

		
	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			Document doc = iterable.first();
			return doc.getString("first_name") + " " + doc.getString("last_name");
		}
		return "";
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			Document doc = iterable.first();
			boolean res = doc.getString("password").equals(password);
			if (res == true) {
				System.out.print("succeed");
				return true;
			} else {
				System.out.print("password does not match");
				return false;
			}
		}
		System.out.print("user does not exist");
		return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection failed");
			return false;
		}
		//implement register
		try {
			FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
			if (iterable.first() == null) {
				db.getCollection("users").insertOne(new Document().append("user_id", userId).append("password", password).append("first_name", firstname).append("last_name", lastname));
				System.out.print("registered");
				return true;
			} else {
				System.out.print("user already exist");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return true;
	}

}
