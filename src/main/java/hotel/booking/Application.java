package hotel.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import hotel.booking.database.Database;
import hotel.booking.container.Hotel;
import hotel.booking.controller.ResultPageController;
import hotel.booking.container.*;

import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("Init");
		
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		Global.db = new Database("database/hotel.db", "resource/HotelList.json");
		Global.db.test();

		// Get hotel list, this will return a brief list of Hotel
		// List<Hotel> hotels = Global.db.getAllHotel("2019/10/23", "2019/10/24", "台北", 4);

		// for(Hotel hotel: hotels){
		// 	System.out.println(hotel);
		// 	// Get the Room of a hotel
		// 	//System.out.println(Global.db.getRoomsOfHotel(hotel));
		// } 
		// // Add new account 
		// System.out.println("Add new account");
		System.out.println(Global.db.addAccount("Test", "test@gmail.com", "test"));
		System.out.println(Global.db.addAccount("TaipeiManager", "a@gmail.com", "a"));
		System.out.println(Global.db.addAccount("TaichungManager", "b@gmail.com", "b"));
		System.out.println(Global.db.addAccount("KaohsiungManager", "c@gmail.com", "c"));

		
		// // Login with account  
		// System.out.println("Login with account");
		Account owner1 = Global.db.verifyAccount("a@gmail.com", "a");
		Account owner2 = Global.db.verifyAccount("b@gmail.com", "b");
		Account owner3 = Global.db.verifyAccount("c@gmail.com", "c");
		List<Hotel> hotels = Global.db.getAllHotel();
		int count = 0;
		for(Hotel hotel: hotels){
			int remain = count / 500;
			if(remain == 0) Global.db.setHotelOwner(hotel, owner1);
			else if(remain == 1) Global.db.setHotelOwner(hotel, owner2);
			else if(remain == 2) Global.db.setHotelOwner(hotel, owner3);
			count++;
		}

		
		// // Set hotel owner
		// System.out.println("Set hotel owner");
		// System.out.println(Global.db.setHotelOwner(hotels.get(0), account1));
		// System.out.println(Global.db.setHotelOwner(hotels.get(1), account1));
		// System.out.println(Global.db.setHotelOwner(hotels.get(2), account1));
		// System.out.println(Global.db.setHotelOwner(hotels.get(3), account2));
		// System.out.println(Global.db.setHotelOwner(hotels.get(4), account2));
		// System.out.println(Global.db.setHotelOwner(hotels.get(5), account3));
		// System.out.println(Global.db.setHotelOwner(hotels.get(6), account3));

		
		
		
		// System.out.println(Global.db.getAccount(1));
		// System.out.println(Global.db.getRoomsOfHotel(1));
		// System.out.println(Global.db.getHotelOwner(1));
		
		// System.out.println(Global.db.getRoomsOfHotel(1).get(0));
		 
			  
		// System.out.println(Global.db.addAccount("test", "test@gmail.com", "password"));
		// Account account = Global.db.verifyAccount("test@gmail.com", "password");
		// Hotel hotel = Global.db.getHotel(1);
		// List<Room> selected_rooms = Global.db.getRoomsOfHotel(1); 

		// Order order = new Order(2, "2019/10/24", "2019/10/25", selected_rooms.get(0));
		// System.out.println(Global.db.addCustomerOrder(account, order)); 
		// order = new Order(3, "2019/10/24", "2019/10/25", selected_rooms.get(1));
		// System.out.println(Global.db.addCustomerOrder(account, order)); 
		// order = new Order(4, "2019/10/24", "2019/10/25", selected_rooms.get(2));
		// System.out.println(Global.db.addCustomerOrder(account, order)); 

		// System.out.println(Global.db.getHotelOwner(1)); 
		// System.out.println(account); 
		// Account new_account = Global.db.updateAccount(account, "abc", "test123@gmail.com", "password");
		// System.out.println(new_account);
		List<Order> orders = Global.db.getCustomerOrder(13);
		//List<Order> orders = Global.db.getOwnerOrder(1);
		System.out.println(orders);
		// //System.out.println(Global.db.cancelOrder(orders.get(0))); 

		// account = Global.db.verifyAccount("owner1@gmail.com", "password1");
		// System.out.println(Global.db.getHotelsOfOwner(account));
		// System.out.println(Global.db.addHotel(account, hotels.get(0)));
		
		// //modify hotel 
		// //Hotel modified_hotel = hotels.get(0);
		// //modified_hotel.star = 1;
		// //modified_hotel.locality = "台南";
		// //modified_hotel.street = "abc street";
		// //modified_hotel.rooms.get(0).quantity = 10;
		// //modified_hotel.rooms.get(0).price = 10;
		// //System.out.println(Global.db.modifyHotel(modified_hotel));
		
		
		// System.out.println("result list");
		// //test ResultPageController function
		// ResultPageController test = new ResultPageController();
		// test.checkin_date = "2019/03/28";
		// test.checkout_date = "2019/03/29";
		// test.location = "台北";
		// test.person = 7;
		// test.GetAllHotel();
		// test.FilteredHotel(test.GetAllHotel(), 4, 5000, 7000);
		
		// orders.get(2).dateIn = "2019/10/23"; 
		// System.out.println(orders);
		// System.out.println(Global.db.getOrder(4));
		// System.out.println(Global.db.modifyOrder(orders.get(2)));
		
	}  

}
