package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item { // search result -> Item
	private String itemId;
	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageUrl;
	private String url;
	private double distance;
	
	private Item(ItemBuilder builder) { // 私有构造函数, 创建一次不可修改, 内部的builder类可以调用, builder pattern!!!
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.rating = builder.rating;
		this.address = builder.address;
		this.categories = builder.categories;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.distance = builder.distance;
	}
	
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	
	public JSONObject toJSONObject() { //将item转换为JSON Object
		JSONObject object = new JSONObject();
		try {
			object.put("item_id", itemId);
			object.put("name", name);
			object.put("rating", rating);
			object.put("address", address);
			object.put("categories", new JSONArray(categories));
			object.put("image_url", imageUrl);
			object.put("url", url);
			object.put("distance", distance);
		}catch(Exception e) {
			//
			e.printStackTrace();
		}
		return object;
	}

	public static class ItemBuilder { // inner class, 创建一次不可修改, builder pattern!!! static，否则不可在无item的情况下新建
		private String itemId;
		private String name;
		private double rating;
		private String address;
		private Set<String> categories;
		private String imageUrl;
		private String url;
		private double distance;
		
		public ItemBuilder setItemId(String itemId) {
			this.itemId = itemId;
			return this;
		}
		public ItemBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
		public Item build() {
			return new Item(this); // 调用构造函数，生成对象
		}


	}
	
}
