package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class Searchltem
 */
@WebServlet("/search")
public class Searchltem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Searchltem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		// optional
		String userId = session.getAttribute("user_id").toString(); // 取出操作历史
		
		// get location of request
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		
		String term = request.getParameter("term"); // parse "term" parameter
        DBConnection connection = DBConnectionFactory.getConnection(); // use DBConnectionFactory to construct a DBConnection, factory pattern!!!
		try {
			List<Item> items = connection.searchItems(lat, lon, term); // 返回list of items
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId); // 判断是否已收藏
			
			JSONArray array = new JSONArray(); // 返回json array
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoritedItemIds.contains(item.getItemId())); // 若已收藏favorite = true
				array.put(obj);
				// array.put(item.toJSONObject()); // 数据写入
			}
			RpcHelper.writeJsonArray(response, array); // 输出网页
			
		} catch (Exception e) {
			e.printStackTrace();
			} finally {
			connection.close();
			}

//		
//		TicketMasterAPI tmAPI = new TicketMasterAPI();
//		List<Item> items = tmAPI.search(lat, lon, null); // moved to MySQLConnection.java
//		
//		JSONArray array = new JSONArray();
//		for (Item item : items) {
//			array.put(item.toJSONObject());
//		}
//		RpcHelper.writeJsonArray(response, array);

		
//		response.setContentType("application/json"); // 返回数据格式
//		PrintWriter out = response.getWriter();// 取response的body
//		if(request.getParameter("username") != null) {
//			String username = request.getParameter("username");
//			
//			JSONObject obj1 = new JSONObject();
//			JSONObject obj2 = new JSONObject();
//			JSONArray arr = new JSONArray();
//			try {
//				obj1.put("name", "abcd");
//				obj1.put("address", "san francisco");
//				obj1.put("time", "01/01/2017");
//				obj2.put("name", "1234");
//				obj2.put("address", "san jose");
//				obj2.put("time", "01/02/2017");
//				arr.put(obj1);
//				arr.put(obj2);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			out.println(arr);
//		}
//		else {
//			out.println("<html><body>");
//			out.println("<h1>Hello world!</h1>");
//			out.println("</body></html>");
//		}
//		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
