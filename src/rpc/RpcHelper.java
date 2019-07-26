package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;


public class RpcHelper {
	// Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
		response.setContentType("application/json"); // 告诉浏览器返回值的类型
		response.setHeader("Access-Control-Allow-Origin", "*"); // 开放资源， * = www.siteA.com 允许访问（我的）资源的地址，任意地址
		PrintWriter out = response.getWriter(); // 可添加数据的client
		out.print(array);
		out.close();
	}

              // Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {	
		response.setContentType("application/json"); // 告诉浏览器返回值的类型
		response.setHeader("Access-Control-Allow-Origin", "*"); // 开放资源， * = www.siteA.com 允许访问（我的）资源的地址，任意地址
		PrintWriter out = response.getWriter(); // 可添加数据的client
		out.print(obj); // 输出网页
		out.close();
	}
	
	// Parses a JSONObject from http request.
	public static JSONObject readJSONObject(HttpServletRequest request) {
  	   StringBuilder sBuilder = new StringBuilder();
  	   try (BufferedReader reader = request.getReader()) {
  		 String line = null;
  		 while((line = reader.readLine()) != null) {
  			 sBuilder.append(line);
  		 }
  		 return new JSONObject(sBuilder.toString());
  		
  	   } catch (Exception e) {
  		 e.printStackTrace();
  	   }
  	
  	  return new JSONObject();
	}
	
	public static JSONArray getJSONArray(List<Item> l) {
		JSONArray res = new JSONArray();
		for (int i = 0; i < l.size(); i++) {
			res.put(l.get(i).toJSONObject());
		}
		return res;
	}

}
