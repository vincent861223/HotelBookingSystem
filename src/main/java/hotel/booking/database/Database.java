package hotel.booking.database;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.*;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import hotel.booking.container.Hotel;
import hotel.booking.container.Room;
import hotel.booking.container.Order;
import hotel.booking.container.Account;
import java.security.MessageDigest;

public class Database {
	private String dbPath;
	private String jsonPath;
	private String initSQLPath;
	public Database(String dbPath, String jsonPath){
		// Pass in "" as jsonPath if the database has already established.
		// Pass in the jsonPath to clear the database and init the database by jsonPath.
		this.dbPath = dbPath;
		this.jsonPath = jsonPath;
		this.initSQLPath = "Database/init_table.sql";
		try{
			File f = new File(this.dbPath);
			if(!f.exists()){
				System.out.println("DB file not exist, establishing DB...");
				initDB();
				establishDB();
			}
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void test() {
		System.out.println("db is working!");
	}

	public List<Hotel> getAllHotel(){
		// Return List of Hotel
		// Note: To save query time, rooms information will not be included(i.e, Hotel.rooms = null).
		List<Hotel> hotels = new ArrayList<Hotel>();
		List<HashMap<String, String>> results = this.selectAll("Hotel");
		for(HashMap<String, String> result: results){
			// HashMap<String, String> attr = new HashMap<>();
			// attr.put("hotel_id", result.get("id"));
			// List<HashMap<String, String>> roomResults = this.select("Room", attr);
			// List<Room> rooms = new ArrayList<Room>();
			// for(HashMap<String, String> roomResult: roomResults){
			// 	Room room = new Room(roomResult.get("type"), Integer.parseInt(roomResult.get("price")), Integer.parseInt(roomResult.get("quantity")));
			// 	rooms.add(room);
			// }
			//Hotel hotel = new Hotel(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("star")), result.get("locality"), result.get("street_address"), rooms);
			List<Room> rooms = getRoomsOfHotel(Integer.parseInt(result.get("id")));
			Hotel hotel = new Hotel(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("star")), result.get("locality"), result.get("street_address"), rooms);
			hotels.add(hotel);
		}
		return hotels;
	}

	public List<Hotel> getAllHotel(String dateIn, String dateOut, String locality, int persons){
		// Return Hotel of specified hotel id. The returned Hotel will include rooms information.
		// Return null if no such hotel found. 
		List<Hotel> hotels = getAllHotel(locality);
		for(Hotel hotel: hotels) {
			int i = 0, size = hotel.rooms.size();
			for(i = 0; i < size; i++) {
				if(!roomAvailable(hotel.rooms.get(i), dateIn, dateOut)){
					hotel.rooms.remove(i);
					i--;
					size--;
				}
			}
		}
		return hotels;
	}
	
	public List<Hotel> getAllHotel(String locality){
		// Return Hotel of specified hotel id. The returned Hotel will include rooms information.
		// Return null if no such hotel found. 
		List<Hotel> hotels = new ArrayList<Hotel>();
		HashMap<String, String> hotel_attr = new HashMap<>();
		hotel_attr.put("locality", locality);
		List<HashMap<String, String>> results = this.select("Hotel", hotel_attr);
		for(HashMap<String, String> result: results){
			List<Room> rooms = getRoomsOfHotel(Integer.parseInt(result.get("id")));
			Hotel hotel = new Hotel(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("star")), result.get("locality"), result.get("street_address"), rooms);
			hotels.add(hotel);
		}
		return hotels;
	}

	public Hotel getHotel(int hotel_id){
		// Return Hotel of specified hotel id. The returned Hotel will include rooms information.
		// Return null if no such hotel found. 
		List<Hotel> hotels = new ArrayList<Hotel>();
		HashMap<String, String> hotel_attr = new HashMap<>();
		hotel_attr.put("id", Integer.toString(hotel_id));
		List<HashMap<String, String>> results = this.select("Hotel", hotel_attr);
		for(HashMap<String, String> result: results){
			List<Room> rooms = getRoomsOfHotel(hotel_id);
			Hotel hotel = new Hotel(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("star")), result.get("locality"), result.get("street_address"), rooms);
			hotels.add(hotel);
		}
		if(hotels.size() < 1){
			System.out.println("Get hotel error! hotels.size() < 1");
			return null;
		}else{
			return hotels.get(0);
		}
	}
	
	public List<Hotel> getHotelsOfOwner(Account account){
		List<Hotel> hotels = new ArrayList<Hotel>();
		HashMap<String, String> attr = new HashMap<>();
		attr.put("owner_id", Integer.toString(account.id));
		List<HashMap<String, String>> results = this.select("Hotel", attr);
		for(HashMap<String, String> result: results){
			List<Room> rooms = getRoomsOfHotel(Integer.parseInt(result.get("id")));
			Hotel hotel = new Hotel(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("star")), result.get("locality"), result.get("street_address"), rooms);
			hotels.add(hotel);
		}
		return hotels;
	}
	
	public boolean addHotel(Account account, Hotel hotel) {
		HashMap<String, String> attr = new HashMap<>();
		attr.put("star", Integer.toString(hotel.star));
		attr.put("locality", hotel.locality);
		attr.put("street_address", hotel.street);
		attr.put("owner_id", Integer.toString(account.id));
		if(this.insert("Hotel", attr)){
			hotel.id = this.maxID("Hotel");
			for(Room room: hotel.rooms){
				this.addRoom(room, hotel);
			}
			return true;
		}
		else return false;
	}

	public boolean modifyHotel(Hotel hotel){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("star", Integer.toString(hotel.star));
		attr.put("locality", hotel.locality);
		attr.put("street_address", hotel.street);
		HashMap<String, String> cond_attr = new HashMap<>();
		cond_attr.put("id", Integer.toString(hotel.id));
		if(this.update("Hotel", attr, cond_attr)){
			for(Room room: hotel.rooms){
				modifyRoom(room);
			}
			return true;
		}
		else return false;
	}
	
	
	public List<Room> getRoomsOfHotel(Hotel hotel){
		return getRoomsOfHotel(hotel.id);
	}

	public List<Room> getRoomsOfHotel(int hotel_id){
		// Return Hotel of specified hotel id. The returned Hotel will include rooms information.
		// Return null if no such hotel found. 
		HashMap<String, String> attr = new HashMap<>();
		attr.put("hotel_id", Integer.toString(hotel_id));
		List<HashMap<String, String>> roomResults = this.select("Room", attr);
		List<Room> rooms = new ArrayList<Room>();
		for(HashMap<String, String> roomResult: roomResults){
			Room room = new Room(Integer.parseInt(roomResult.get("id")), roomResult.get("type"), Integer.parseInt(roomResult.get("price")), Integer.parseInt(roomResult.get("quantity")));
			rooms.add(room); 
		}
		return rooms;
	}

	public Room getRoom(int room_id){
		List<Room> rooms = new ArrayList<Room>();
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(room_id));
		List<HashMap<String, String>> results = this.select("Room", attr);
		for(HashMap<String, String> result: results){
			Room room = new Room(Integer.parseInt(result.get("id")), result.get("type"), Integer.parseInt(result.get("price")), Integer.parseInt(result.get("quantity")));
			rooms.add(room);
		}
		if(rooms.size() == 0){
			return null;
		}else{
			return rooms.get(0);
		}
	}

	public boolean modifyRoom(Room room){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("price", Integer.toString(room.price));
		attr.put("quantity", Integer.toString(room.quantity));
		HashMap<String, String> cond_attr = new HashMap<>();
		cond_attr.put("id", Integer.toString(room.id));
		if(this.update("Room", attr, cond_attr)) return true;
		else return false;
	}

	public boolean addRoom(Room room, Hotel hotel){
		HashMap<String, String> attr = new HashMap<String, String>();
		attr.put("type", room.type);
		attr.put("price", Integer.toString(room.price));
		attr.put("quantity", Integer.toString(room.quantity));
		attr.put("hotel_id", Integer.toString(hotel.id));
		return this.insert("Room", attr);
	}

	public Boolean setHotelOwner(Hotel hotel, Account account) {
		return setHotelOwner(hotel.id, account.id);
	}
	
	public Boolean setHotelOwner(int hotel_id, int account_id){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("owner_id", Integer.toString(account_id));
		HashMap<String, String> cond_attr = new HashMap<>();
		cond_attr.put("id", Integer.toString(hotel_id));
		return this.update("Hotel", attr, cond_attr);
	}

	public Account getHotelOwner(Hotel hotel) {
		return getHotelOwner(hotel.id);
	}
	
	public Account getHotelOwner(int hotel_id){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(hotel_id));
		List<HashMap<String, String>> results = this.select("Hotel", attr);
		if(results.size() == 0) return null;
		else{
			HashMap<String, String> result = results.get(0);
			if(result.get("owner_id") == null) return null;
			Account account = this.getAccount(Integer.parseInt(result.get("owner_id")));
			return account;
		}
	}

	public Account getRoomOwner(Room room){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(room.id));
		List<HashMap<String, String>> results = this.select("Room", attr);
		if(results == null) return null;
		int hotel_id = Integer.parseInt(results.get(0).get("hotel_id"));
		return getHotelOwner(hotel_id);
	}
	
	public Account addAccount(String name, String email, String password){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("name", name);
		attr.put("email", email);
		attr.put("password", hashPassword(password));
		if(this.insert("Account", attr)) return getAccount(maxID("Account"));
		else return null;
	}
	
	public Account updateAccount(Account account, String name, String email, String password){
		// addAccount will fail if account with same email has existed.
		// todo: return Account 
		HashMap<String, String> attr = new HashMap<>();
		attr.put("name", name);
		attr.put("email", email);
		attr.put("password", hashPassword(password));
		HashMap<String, String> cond_attr = new HashMap<>();
		cond_attr.put("email", account.email);
		if(this.update("Account", attr, cond_attr)) return getAccount(account.id);
		else return null;
	}

	public Account getAccount(int account_id){
		List<Account> accounts = new ArrayList<Account>();
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(account_id));
		List<HashMap<String, String>> results = this.select("Account", attr);
		for(HashMap<String, String> result: results){
			Account account = new Account(Integer.parseInt(result.get("id")), result.get("email"), result.get("name"));
			accounts.add(account);
		}
		if(accounts.size() == 0){
			return null;
		}else{
			return accounts.get(0);
		}
	}

	public Account verifyAccount(String email, String password){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("email", email);
		attr.put("password", hashPassword(password));
		List<HashMap<String, String>> account = this.select("Account", attr);
		if(account.size() > 0) return getAccount(Integer.parseInt(account.get(0).get("id")));
		else return null;
	}
	
	
	public Boolean addCustomerOrder(Account account, Order order){
		// Check if the room is available during the date
		if(order.quantity > this.roomLeft(order.room, order.dateIn, order.dateOut)) return false;
		// Insert an order
		HashMap<String, String> attr = new HashMap<>();
		Account roomOwner = this.getRoomOwner(order.room);
		attr.put("account_id", Integer.toString(account.id));
		attr.put("room_id", Integer.toString(order.room.id));
		attr.put("quantity", Integer.toString(order.quantity));
		attr.put("dateIn", order.dateIn);
		attr.put("dateOut", order.dateOut);
		if(roomOwner != null) attr.put("owner_id", Integer.toString(roomOwner.id));
		this.insert("Order", attr);
		return true;
	}
	public Boolean addCustomerOrder(Account account, List<Order> orders){
		boolean result = true;
		for(Order order: orders){
			result &= this.addCustomerOrder(account, order); 
		}
		return result;
	}

	public List<Order> getCustomerOrder(int account_id){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("account_id", Integer.toString(account_id));
		List<HashMap<String, String>> results = this.select("Order", attr);
		List<Order> orders = new ArrayList<Order>();
		for(HashMap<String, String> result: results){
			Room room = getRoom(Integer.parseInt(result.get("room_id")));
			Order order = new Order(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("quantity")), result.get("dateIn"), result.get("dateOut"), room);
			orders.add(order);
		}
		return orders;
	}

	public List<Order> getOwnerOrder(int owner_id){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("owner_id", Integer.toString(owner_id));
		List<HashMap<String, String>> results = this.select("Order", attr);
		List<Order> orders = new ArrayList<Order>();
		for(HashMap<String, String> result: results){
			Room room = getRoom(Integer.parseInt(result.get("room_id")));
			Order order = new Order(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("quantity")), result.get("dateIn"), result.get("dateOut"), room);
			orders.add(order);
		}
		return orders;
	}

	public Order getOrder(int order_id){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(order_id));
		List<HashMap<String, String>> results = this.select("Order", attr);
		if(results.size() == 0) return null;
		HashMap<String, String> result = results.get(0);
		Room room = this.getRoom(Integer.parseInt(result.get("room_id")));
		Order order = new Order(Integer.parseInt(result.get("id")), Integer.parseInt(result.get("quantity")), result.get("dateIn"), result.get("dateOut"), room);
		return order;
	}

	public boolean cancelOrder(Order order){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("id", Integer.toString(order.id));
		if(this.delete("Order", attr)) return true;
		else return false;
	}

	public boolean modifyOrder(Order order){
		// Check if the room is available during the date
		Order old_order = this.getOrder(order.id);
		//System.out.println("check");
		if(old_order == null) return false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date dateIn_old = null;
		try {
			dateIn_old = sdf.parse(old_order.dateIn);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Date dateIn = null;
		try {
			dateIn = sdf.parse(order.dateIn);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Date dateOut_old = null;
		try {
			dateOut_old = sdf.parse(old_order.dateOut);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Date dateOut = null;
		try {
			dateOut = sdf.parse(order.dateOut);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(old_order.dateIn.equals(order.dateIn) && old_order.dateOut.equals(old_order.dateOut) && old_order.quantity == order.quantity) return true;
		int room_left = this.roomLeft(order.room, order.dateIn, order.dateOut);
		//System.out.println(old_order);
		//System.out.println(order);
		if(dateIn_old.compareTo(dateIn) <= 0 && dateOut_old.compareTo(dateOut) >= 0) room_left += old_order.quantity;
		else return false;
		//System.out.println("check");
		//System.out.println(room_left);
		if(order.quantity > room_left) return false;
		// Insert an order
		HashMap<String, String> attr = new HashMap<>();
		HashMap<String, String> cond_attr = new HashMap<String, String>();
		attr.put("quantity", Integer.toString(order.quantity));
		attr.put("dateIn", order.dateIn);
		attr.put("dateOut", order.dateOut);
		cond_attr.put("id", Integer.toString(order.id));
		//System.out.println("check");
		return this.update("Order", attr, cond_attr); 
	}

	public int roomOccupied(Room room, String dateIn, String dateOut){
		// Return number of room occupied during dateIn~dataOut
		HashMap<String, String> attr = new HashMap<>();
		attr.put("dateIn < ", dateOut);
		attr.put("dateOut > ", dateIn);
		attr.put("room_id = ", Integer.toString(room.id));
		List<HashMap<String, String>> results = this.selectCondition("Order", attr);
		int sum = 0; 
		for(HashMap<String, String> result: results) {
			sum += Integer.parseInt(result.get("quantity"));
		}
		return sum;
	}
	
	public int roomLeft(Room room, String dateIn, String dateOut) {
		// Return number of room left during dateIn~dataOut
		Room queryRoom = this.getRoom(room.id);
		return queryRoom.quantity - this.roomOccupied(room, dateIn, dateOut);
	}

	public Boolean roomAvailable(Room room, String dateIn, String dateOut){
		// return whether the room has at least one room left
		if(roomLeft(room, dateIn, dateOut) > 0) return true;
		else return false;
	}

	private void initDB(){
	    try{	
	    	File f = new File(this.initSQLPath);
	    	Scanner s = new Scanner(f).useDelimiter("(;(\r)?\n)|((\r)?\n)?(--)?.*(--(\r)?\n)");
	    	Statement st = null;
	        st = this.connect().createStatement();
	        while (s.hasNext()){
	            String line = s.next();
	            if (line.startsWith("/*!") && line.endsWith("*/")){
	                int i = line.indexOf(' ');
	                line = line.substring(i + 1, line.length() - " */".length());
	            }

	            if (line.trim().length() > 0){
	                st.execute(line);
	            }
	        }
	        st.close();
	    }catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void establishDB(){
		JsonHotel[] hotels = read_hotel_list();
		if(hotels == null){
			System.out.println("No hotel loaded!\n");
			return;
		}
		int count = 1;
		for(JsonHotel hotel: hotels){
			HashMap<String, String> hotel_attr = new HashMap<>();
			hotel_attr.put("star", Integer.toString(hotel.HotelStar));
			hotel_attr.put("locality", hotel.Locality);
			hotel_attr.put("street_address", hotel.Street_Address);
			insert("Hotel", hotel_attr);
			for(JsonRoom room: hotel.Rooms){
				HashMap<String, String> room_attr = new HashMap<>();
				room_attr.put("type", room.RoomType);
				room_attr.put("price", Integer.toString(room.RoomPrice));
				room_attr.put("quantity", Integer.toString(room.Number));
				room_attr.put("hotel_id", Integer.toString(count));
				insert("Room", room_attr);
			}
			count++;
		}
	}

	private Connection connect(){

		Connection conn = null;
		
		try {
			Class.forName("org.sqlite.JDBC"); 
			String url = "jdbc:sqlite:" + this.dbPath;
			conn = DriverManager.getConnection(url);
			//System.out.println("Connection to SQLite has been established.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}
	
	private int maxID(String table) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";

		String sql = "SELECT MAX(id) FROM " + "`" + table + "`" ;
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			Statement stmt = conn.createStatement();
			// pstmt.executeUpdate();
			ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
            	return rs.getInt("MAX(id)");
            }
            conn.close();
            return -1;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}
	
	private int len(String table) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";

		String sql = "SELECT * FROM " + "`" + table + "`" ;
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			Statement stmt = conn.createStatement();
			// pstmt.executeUpdate();
			ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            // loop through the result set
            while (rs.next()) {
            	count++;
            }
            conn.close();
            return count;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}

	private Boolean insert(String table,  HashMap<String, String> attr) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";
		String columns = "", values = "";
		for(String key: attr.keySet()){
			String value = attr.get(key);
			columns += (key + ", ");
			values += ("'" + value + "'" + ", ");
		}
		columns = columns.substring(0, columns.length()-2);
		values = values.substring(0, values.length()-2);

		String sql = "INSERT INTO " + "`" + table + "`"  + " ( " + columns + " ) " + " VALUES " + " ( " + values + " ) ;";
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			conn.close();
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	private Boolean update(String table,  HashMap<String, String> attr, HashMap<String, String> cond_attr) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";
		String set_values = "";
		for(String key: attr.keySet()){
			String value = attr.get(key);
			set_values += (key + "=" + "'" + value + "'") + " , ";
		}
		set_values = set_values.substring(0, set_values.length()-3);

		String conditions = "";
		for(String key: cond_attr.keySet()){
			String value = cond_attr.get(key);
			conditions += key + "=" + "'" + value + "'" + " and ";
		}
		conditions = conditions.substring(0, conditions.length()-5);

		String sql = "UPDATE " + "`" + table + "`" + " SET " + set_values + " WHERE " + conditions + " ;";
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			conn.close();
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	private Boolean delete(String table,  HashMap<String, String> attr) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";
		String conditions = "";
		for(String key: attr.keySet()){
			String value = attr.get(key);
			conditions += key + "=" + "'" + value + "'" + " and ";
		}
		conditions = conditions.substring(0, conditions.length()-5);

		String sql = "DELETE FROM " + "`" + table + "`" + " WHERE " + conditions + " ;";
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			conn.close();
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	private List<HashMap<String, String>> select(String table,  HashMap<String, String> attr) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";
		String conditions = "";
		for(String key: attr.keySet()){
			String value = attr.get(key);
			conditions += key + "=" + "'" + value + "'" + " and ";
		}
		conditions = conditions.substring(0, conditions.length()-5);

		String sql = "SELECT * FROM " + "`" + table + "`" + " WHERE " + conditions + ";" ;
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			Statement stmt = conn.createStatement();
			// pstmt.executeUpdate();
			ResultSet rs = stmt.executeQuery(sql); 
            
            List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
            // loop through the result set
            while (rs.next()) {
            	ResultSetMetaData rsmd = rs.getMetaData();
            	int n_column = rsmd.getColumnCount();
            	HashMap<String, String> result = new HashMap<>();
            	for (int i = 1; i <= n_column; i++) 
            		result.put(rsmd.getColumnName(i), rs.getString(i));
            	results.add(result);
            	//System.out.println(rs.getString(1) + rs.getString(2) + rs.getString(3));
                // System.out.println(rs.getInt("id") +  "\t" + 
                //                    rs.getString("name") + "\t" +
                //                    rs.getDouble("capacity"));
            }
            conn.close();
            return results;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private List<HashMap<String, String>> selectCondition(String table,  HashMap<String, String> attr) {
		//String sql = "INSERT INTO JsonHotel (star, locality, street_address) VALUES (1, 'Taipei', 'abc street');";
		String conditions = "";
		for(String key: attr.keySet()){
			String value = attr.get(key);
			conditions += key + "'" + value + "'" + " and ";
		}
		conditions = conditions.substring(0, conditions.length()-5);

		String sql = "SELECT * FROM " + "`" + table + "`" + " WHERE " + conditions + ";" ;
		//System.out.println(sql);
		//String sql = "INSERT INTO " + table + " (star, locality, street_address) " + " VALUES " + "(1, 'Taipei', 'abc street');";

		try(Connection conn = this.connect()) {
			Statement stmt = conn.createStatement();
			// pstmt.executeUpdate();
			ResultSet rs = stmt.executeQuery(sql);
            
            List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
            // loop through the result set
            while (rs.next()) {
            	ResultSetMetaData rsmd = rs.getMetaData();
            	int n_column = rsmd.getColumnCount();
            	HashMap<String, String> result = new HashMap<>();
            	for (int i = 1; i <= n_column; i++) 
            		result.put(rsmd.getColumnName(i), rs.getString(i));
            	results.add(result);
            	//System.out.println(rs.getString(1) + rs.getString(2) + rs.getString(3));
                // System.out.println(rs.getInt("id") +  "\t" + 
                //                    rs.getString("name") + "\t" +
                //                    rs.getDouble("capacity"));
            }
            conn.close();
            return results;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private List<HashMap<String, String>> selectAll(String table){
		HashMap<String, String> attr = new HashMap<>();
		attr.put("'1'", "1");
		return this.select(table, attr);
	}

	private JsonHotel[] read_hotel_list(){
		Gson gson = new Gson();
		try {
			File f = new File(this.jsonPath);
			InputStreamReader read = new InputStreamReader(new FileInputStream(f),"big5"); 
			BufferedReader br = new BufferedReader(read);
			JsonHotel[] hotels = gson.fromJson(br, JsonHotel[].class);
			return hotels;
			//
		}catch (IOException e) {
			System.out.println("File not exist!");
			return null;
		}
	}

	private String hashPassword(String password){
		String encryptedString = "";
		try{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			encryptedString = new String(messageDigest.digest());
			encryptedString = Base64.getEncoder().encodeToString(encryptedString.getBytes());
		}catch(Exception e){}
		return encryptedString;
	}
}

//This class is for reading json file only, please ignore this class. 
class JsonHotel{
	public int HotelID; 
	public int HotelStar; 
	public String Locality; 
	@SerializedName("Street-Address")
	public String Street_Address; 
	public JsonRoom[] Rooms; 
	// void JsonHotel(){
	// 	rooms = new JsonRoom[3];
	// }
}

//This class is for reading json file only, please ignore this class. 
class JsonRoom{
	public String RoomType;
	public int RoomPrice;
	public int Number;
}
