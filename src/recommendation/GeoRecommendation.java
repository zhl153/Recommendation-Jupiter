package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import entity.Item;

public class GeoRecommendation {
  public List<Item> recommendItems(String userId, double lat, double lon) {
	  // user -> item -> category -> search
		List<Item> recommendedItems = new ArrayList<>();

		// Step 1, get all favorited item_ids
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// Step 2, get all categories, sort by count
		// {"sports": 5, "music": 3, "art": 2}
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId : favoritedItemIds) {
			Set<String> categories = connection.getCategories(itemId); // 根据item_id找category
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1); // 合并为一个hash map
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue()); // 排序
		});
		
		// Step 3, search based on category, filter out favorite items
		Set<String> visitedItemIds = new HashSet<>(); // 辅助去重
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey()); // search from TicketMaster
			// sort by geo-distance
			for (Item item : items) { // 去除已收藏和已推荐
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item); 
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		
		connection.close();
		return recommendedItems;
}

}
