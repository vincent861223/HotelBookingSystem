package hotel.booking.controller;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import hotel.booking.Global;
import hotel.booking.container.Account;
import hotel.booking.container.Hotel;
import hotel.booking.container.Order;
import hotel.booking.container.Room;

@Controller
public class InfoCheckController {            
	@RequestMapping(value="/confirmation", method=RequestMethod.POST)
    public String infoCheck(@RequestParam int numofSingle, @RequestParam int numofDouble, @RequestParam int numofQuad, Model model) {
    	System.out.println("confirmation page");
        return "confirmation";                        
    }
    
    public List<Order> bookCheck(Hotel hotel, String dateIn, String dateOut, int numofSingle, int numofDouble, int numofQuad) {
    	// Given a hotel and dateIn~dateOut, and the number of room the user want to book, 
        // this function will check if the desired room are available, and create an order for you.
        // (If not all rooms are available, it will return null.)
        boolean available = true;
    	List<Room> roomlist = Global.db.getRoomsOfHotel(hotel);
    	if (Global.db.roomLeft(roomlist.get(0), dateIn, dateOut) >= numofSingle) {   //not finished yet
    		available = false;
    	}
    	if (Global.db.roomLeft(roomlist.get(1), dateIn, dateOut) >= numofDouble) {
    		available = false;
    	}
    	if (Global.db.roomLeft(roomlist.get(2), dateIn, dateOut) >= numofQuad) {
    		available = false;
    	}
    	if(available = true) {
    		List<Room> orderRoomlist = new ArrayList<>();
            List<Order> orders = new ArrayList<>();
    		Room single_room = new Room(roomlist.get(0).id, roomlist.get(0).type, roomlist.get(0).price, numofSingle);
    		Room double_room = new Room(roomlist.get(1).id, roomlist.get(1).type, roomlist.get(1).price, numofDouble);
    		Room quad_room = new Room(roomlist.get(2).id, roomlist.get(2).type, roomlist.get(2).price, numofQuad);
            orders.add(new Order(dateIn, dateOut, single_room));
            orders.add(new Order(dateIn, dateOut, double_room));
            orders.add(new Order(dateIn, dateOut, quad_room));
            return orders;
    	}else{
            return null;
        }
    }
    
    public boolean bookComplete(Account account, List<Order> orders) {
        // Place an order. Return true if success, false if failed. 
    	return Global.db.addCustomerOrder(account, orders); 
    }

}
