package hotel.booking.container;
import hotel.booking.container.Room;
import hotel.booking.container.Hotel;
import java.util.List;

public class Order {
	public int id;
	public int quantity;
	public String dateIn;
	public String dateOut;
	public Room room;

	public Order(){
		//Default constructor
		this.id = -1;
		this.quantity = 0;
		this.dateIn = "";
		this.dateOut = "";
	}
	

	public Order(int quantity, String dateIn, String dateOut, Room room){
		// Parameter constructor
		this.id = -1;
		this.quantity = quantity;
		this.dateIn = dateIn;
		this.dateOut = dateOut;
		this.room = room;
	}

	public Order(int id, int quantity, String dateIn, String dateOut, Room room){
		// Parameter constructor
		this.id = id;
		this.quantity = quantity;
		this.dateIn = dateIn;
		this.dateOut = dateOut;
		this.room = room;
	}

	@Override 
	public String toString() {
		// return String representation of the class
        return "-----Order-----\n" + 
        	   "id: " + this.id + "\n" + 
        	   "quantity: " + this.quantity + "\n" +
        	   "dateIn: " + this.dateIn + "\n" + 
        	   "dateOut: " +  this.dateOut + "\n" + 
        	   this.room;
    }
}
