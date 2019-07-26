package external;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

// 向外发送请求
public class TicketMasterAPI {
	
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json"; // protocol://hostname/endpoint
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "aStDO2VAH29rDUc316sadSYqN7svevfV";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if(keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // 特殊字符转换，例如将空格转换为%20
		}catch (UnsupportedEncodingException e) {
			//TODO: handle exception
			e.printStackTrace();
		}
		
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s", API_KEY, lat, lon, keyword, 50);// %s替换字符
		String url = URL + "?" + query; // form the http request
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(); //尝试连接, !!!
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode(); // 调用API, execute the request!!!
			System.out.println("Sending request to url: " + url);
			System.out.println("Response code: " + responseCode);
			
			if (responseCode != 200) { // 若访问失败，返回空
				return new ArrayList<>();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // 转换成reader格式, !!!
			String line;
			StringBuilder response = new StringBuilder();
			
			while ((line = reader.readLine()) != null) { // 8k8k读取数据
				response.append(line); // 添加至response
			}
			reader.close();//!!!
			
			// http response: stream -> buffer reader -> string -> json object -> list
			JSONObject object = new JSONObject(response.toString()); // 返回的String
			
			if(!object.isNull("_embedded")) { // 取出返回之中key="_embedded"的value
				JSONObject embedded = object.getJSONObject("_embedded"); // build an instance from result -- bridge pattern!!!
				return getItemList(embedded.getJSONArray("events"));
			}
						
		}catch(Exception e) {
			//TODO: handle exception
			e.printStackTrace();
		}
		return new ArrayList<>();
	}	
	
	private String getAddress(JSONObject event) throws JSONException { //从返回结果中抽取数据（events中）
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for(int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder stringBuilder = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							stringBuilder.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							stringBuilder.append(",");
							stringBuilder.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							stringBuilder.append(",");
							stringBuilder.append(address.getString("line3"));
						}
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						stringBuilder.append(",");
						stringBuilder.append(city.getString("name"));
					}
					
					String result = stringBuilder.toString();
					if (!result.isEmpty()) {
						return result;
					}
				}
			}
		}
		return "";
	}
	
	// 下面测试
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);

		for (Item event : events) {
			System.out.println(event.toJSONObject());
		}
//		JSONArray events = search(lat, lon, null);
//
//		try {
//		    for (int i = 0; i < events.length(); ++i) {
//		       JSONObject event = events.getJSONObject(i);
//		       System.out.println(event.toString(2));
//		    }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}
	
	private Set<String> getCategories(JSONObject event) throws JSONException {
		
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); ++i) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}


	// events
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();
		for(int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder(); // use ItemBuilder to build item -- builder pattern!!!
			
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			// use helper functions to fetch information deep inside the response body
			builder.setAddress(getAddress(event))
			.setCategories(getCategories(event))
			.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build()); // use builder item to generate an Item -- builder pattern!!!

		}
		
		return itemList;
	}


	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		tmApi.queryAPI(37.38, -122.08); // test
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		// tmApi.queryAPI(29.682684, -95.295410);
	}

}
